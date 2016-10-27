/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import org.infinispan.manager.CacheContainer;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.zanata.cache.CacheWrapper;
import org.zanata.cache.InfinispanCacheWrapper;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.ValidationFactoryProvider;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.Zanata;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.cache.CacheLoader;

/**
 * Default Implementation of the Translation State Cache.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
// TODO split into APPLICATION and STATELESS beans
@javax.enterprise.context.ApplicationScoped

public class TranslationStateCacheImpl implements TranslationStateCache {
    private static final String BASE = TranslationStateCacheImpl.class.getName();

    private static final String DOC_STATISTIC_CACHE_NAME = BASE
            + ".documentStatisticCache";

    private static final String DOC_STATUS_CACHE_NAME = BASE
            + ".docStatusCache";

    private static final String TFT_VALIDATION_CACHE_NAME = BASE
            + ".targetValidationCache";

    private CacheWrapper<DocumentLocaleKey, WordStatistic> documentStatisticCache;
    private final CacheLoader<DocumentLocaleKey, WordStatistic> documentStatisticLoader;

    private CacheWrapper<DocumentLocaleKey, DocumentStatus> docStatusCache;
    private final CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;

    private CacheWrapper<Long, Map<ValidationId, Boolean>> targetValidationCache;
    private final CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

    @Zanata
    private final CacheContainer cacheContainer;
    private final TextFlowDAO textFlowDAO;
    private final TextFlowTargetDAO textFlowTargetDAO;
    private final DocumentDAO documentDAO;
    private final LocaleDAO localeDAO;

    // constructor for CDI
    public TranslationStateCacheImpl() {
        this(null, null, null, null, null, null, null, null);
    }

    // Constructor for testing
    @Inject
    public TranslationStateCacheImpl(
        CacheLoader<DocumentLocaleKey, WordStatistic> documentStatisticLoader,
        CacheLoader<DocumentLocaleKey, DocumentStatus> docStatsLoader,
        CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader,
        @Zanata CacheContainer cacheContainer,
        TextFlowDAO textFlowDAO,
        TextFlowTargetDAO textFlowTargetDAO,
        DocumentDAO documentDAO,
        LocaleDAO localeDAO) {
        this.documentStatisticLoader = documentStatisticLoader;
        this.docStatusLoader = docStatsLoader;
        this.targetValidationLoader = targetValidationLoader;
        this.cacheContainer = cacheContainer;
        this.textFlowDAO = textFlowDAO;
        this.textFlowTargetDAO = textFlowTargetDAO;
        this.documentDAO = documentDAO;
        this.localeDAO = localeDAO;
    }

    @PostConstruct
    public void create() {
        documentStatisticCache =
                InfinispanCacheWrapper.create(DOC_STATISTIC_CACHE_NAME,
                        cacheContainer, documentStatisticLoader);

        docStatusCache =
                InfinispanCacheWrapper.create(DOC_STATUS_CACHE_NAME,
                        cacheContainer,
                        docStatusLoader);
        targetValidationCache =
                InfinispanCacheWrapper.create(TFT_VALIDATION_CACHE_NAME,
                        cacheContainer,
                        targetValidationLoader);
    }

    @Override
    public WordStatistic getDocumentStatistics(Long documentId,
            LocaleId localeId) {
        return documentStatisticCache.getWithLoader(new DocumentLocaleKey(
                documentId, localeId));
    }

    public void clearDocumentStatistics(Long documentId) {
        for (HLocale locale : localeDAO.findAll()) {
            DocumentLocaleKey key =
                    new DocumentLocaleKey(documentId, locale.getLocaleId());
            documentStatisticCache.remove(key);
        }
    }

    @Override
    public void clearDocumentStatistics(Long documentId, LocaleId localeId) {
        documentStatisticCache.remove(new DocumentLocaleKey(documentId,
                localeId));
    }


    public DocumentStatus getDocumentStatus(Long documentId, LocaleId localeId) {
        return docStatusCache.getWithLoader(new DocumentLocaleKey(
                documentId, localeId));
    }

    @Override
    public Boolean textFlowTargetHasWarningOrError(Long targetId,
            ValidationId validationId) {
        Map<ValidationId, Boolean> cacheEntry =
                targetValidationCache.getWithLoader(targetId);
        synchronized (cacheEntry) {
            if (!cacheEntry.containsKey(validationId)) {
                Boolean result = loadTargetValidation(targetId, validationId);
                cacheEntry.put(validationId, result);
            }
            return cacheEntry.get(validationId);
        }
    }

    /**
     * This method contains all logic to be run immediately after a Text Flow
     * Target has been successfully translated.
     */
    @Override
    public void textFlowStateUpdated(
        @Observes(during = TransactionPhase.AFTER_SUCCESS)
            TextFlowTargetStateEvent event) {
        for (TextFlowTargetStateEvent.TextFlowTargetStateChange state : event
            .getStates()) {
            // invalidate target validation
            targetValidationCache.remove(state.getTextFlowTargetId());
        }
    }

    public void docStatsUpdated(
        @Observes(during = TransactionPhase.AFTER_SUCCESS)
            DocStatsEvent event) {
        // invalidate document statistic cache
        clearDocumentStatistics(event.getKey().getDocumentId(),
            event.getKey().getLocaleId());

        // update document status information
        updateDocStatusCache(event.getKey(),
            event.getLastModifiedTargetId());
    }

    private void updateDocStatusCache(DocumentLocaleKey key,
            Long updatedTargetId) {
        DocumentStatus documentStatus = docStatusCache.get(key);
        if (documentStatus != null) {
            HTextFlowTarget target =
                textFlowTargetDAO.findById(updatedTargetId, false);
            updateDocumentStatus(documentDAO, documentStatus,
                key.getDocumentId(), target);
        }
    }

    private Boolean loadTargetValidation(Long textFlowTargetId,
            ValidationId validationId) {
        HTextFlowTarget tft =
                textFlowTargetDAO.findById(textFlowTargetId, false);
        if (tft != null) {
            ValidationAction action =
                    ValidationFactoryProvider.getFactoryInstance()
                            .getValidationAction(validationId);
            List<String> errorList =
                    action.validate(tft.getTextFlow().getContents().get(0), tft
                            .getContents().get(0));
            return !errorList.isEmpty();
        }
        return null;
    }

    public static class DocumentStatisticLoader extends
            CacheLoader<DocumentLocaleKey, WordStatistic> {

        @Inject
        private DocumentDAO documentDAO;

        @Override
        public WordStatistic load(DocumentLocaleKey key) throws Exception {
            WordStatistic wordStatistic = documentDAO.getWordStatistics(
                    key.getDocumentId(), key.getLocaleId());
            return wordStatistic;
        }
    }

    public static class HTextFlowTargetIdLoader extends
            CacheLoader<DocumentLocaleKey, DocumentStatus> {

        @Inject
        private DocumentDAO documentDAO;

        @Override
        public DocumentStatus load(DocumentLocaleKey key) throws Exception {
            HTextFlowTarget target =
                    documentDAO.getLastTranslatedTarget(key.getDocumentId(),
                            key.getLocaleId());
            DocumentStatus documentStatus = new DocumentStatus();

            return updateDocumentStatus(documentDAO, documentStatus,
                    key.getDocumentId(), target);
        }
    }

    public static class HTextFlowTargetValidationLoader extends
            CacheLoader<Long, Map<ValidationId, Boolean>> {
        @Override
        public Map<ValidationId, Boolean> load(Long key) throws Exception {
            return new HashMap<ValidationId, Boolean>();
        }
    }

    private static DocumentStatus updateDocumentStatus(DocumentDAO documentDAO,
            DocumentStatus documentStatus, Long documentId,
            HTextFlowTarget target) {

        Date lastTranslatedDate = null;
        String lastTranslatedBy = "";

        if (target != null) {
            lastTranslatedDate = target.getLastChanged();

            if (target.getLastModifiedBy() != null) {
                lastTranslatedBy =
                        target.getLastModifiedBy().getAccount().getUsername();
            }
        }
        HDocument document = documentDAO.findById(documentId, false);
        documentStatus.update(
                new DocumentId(document.getId(), document.getDocId()),
                lastTranslatedDate, lastTranslatedBy);
        return documentStatus;
    }
}
