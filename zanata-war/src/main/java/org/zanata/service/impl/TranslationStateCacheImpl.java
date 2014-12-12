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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.sf.ehcache.CacheManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.cache.CacheWrapper;
import org.zanata.cache.EhcacheWrapper;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.ValidationFactoryProvider;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.ServiceLocator;
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
@Name("translationStateCacheImpl")
// TODO split into APPLICATION and STATELESS beans
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class TranslationStateCacheImpl implements TranslationStateCache {
    private static final String BASE = TranslationStateCacheImpl.class.getName();

    private static final String DOC_STATISTIC_CACHE_NAME = BASE
            + ".documentStatisticCache";

    private static final String DOC_STATUS_CACHE_NAME = BASE
            + ".docStatusCache";

    private static final String TFT_VALIDATION_CACHE_NAME = BASE
            + ".targetValidationCache";

    private CacheManager cacheManager;

    private CacheWrapper<DocumentLocaleKey, WordStatistic> documentStatisticCache;
    private CacheLoader<DocumentLocaleKey, WordStatistic> documentStatisticLoader;

    private CacheWrapper<DocumentLocaleKey, DocumentStatus> docStatusCache;
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;

    private CacheWrapper<Long, Map<ValidationId, Boolean>> targetValidationCache;
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

    @In
    private ServiceLocator serviceLocator;

    // constructor for Seam
    public TranslationStateCacheImpl() {
        this(new DocumentStatisticLoader(), new HTextFlowTargetIdLoader(),
                new HTextFlowTargetValidationLoader());
    }

    // Constructor for testing
    public TranslationStateCacheImpl(
        CacheLoader<DocumentLocaleKey, WordStatistic> documentStatisticLoader,
        CacheLoader<DocumentLocaleKey, DocumentStatus> docStatsLoader,
        CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader) {
        this.documentStatisticLoader = documentStatisticLoader;
        this.docStatusLoader = docStatsLoader;
        this.targetValidationLoader = targetValidationLoader;
    }

    @Create
    public void create() {
        cacheManager = CacheManager.create();
        documentStatisticCache =
                EhcacheWrapper.create(DOC_STATISTIC_CACHE_NAME,
                        cacheManager, documentStatisticLoader);

        docStatusCache =
                EhcacheWrapper.create(DOC_STATUS_CACHE_NAME, cacheManager,
                        docStatusLoader);
        targetValidationCache =
                EhcacheWrapper.create(TFT_VALIDATION_CACHE_NAME, cacheManager,
                        targetValidationLoader);
    }

    @Destroy
    public void destroy() {
        cacheManager.shutdown();
    }

    @Override
    public WordStatistic getDocumentStatistics(Long documentId,
            LocaleId localeId) {
        return documentStatisticCache.getWithLoader(new DocumentLocaleKey(
                documentId, localeId));
    }

    @Override
    public void clearDocumentStatistics(Long documentId) {
        LocaleDAO localeDAO = serviceLocator.getInstance(LocaleDAO.class);
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

    // TODO why not @Observe the event directly?
    @Override
    public void textFlowStateUpdated(TextFlowTargetStateEvent event) {
        DocumentLocaleKey key =
                new DocumentLocaleKey(event.getDocumentId(),
                        event.getLocaleId());

        WordStatistic stats = documentStatisticCache.get(key);

        if (stats != null) {
            HTextFlow textFlow = getTextFlowDAO().findById(
                    event.getTextFlowId());

            stats.decrement(event.getPreviousState(),
                    textFlow.getWordCount().intValue());
            stats.increment(event.getNewState(),
                    textFlow.getWordCount().intValue());
            documentStatisticCache.put(key, stats);
        }

        // update document status information
        updateDocStatusCache(key, event.getTextFlowTargetId());

        // invalidate target validation
        targetValidationCache.remove(event.getTextFlowTargetId());
    }

    private void updateDocStatusCache(DocumentLocaleKey key,
            Long updatedTargetId) {
        DocumentStatus documentStatus = docStatusCache.get(key);
        if(documentStatus != null) {
            HTextFlowTarget target =
                getTextFlowTargetDAO().findById(updatedTargetId, false);
            updateDocumentStatus(getDocumentDAO(), documentStatus,
                key.getDocumentId(), target);
        }
    }

    private Boolean loadTargetValidation(Long textFlowTargetId,
            ValidationId validationId) {
        HTextFlowTarget tft =
                getTextFlowTargetDAO().findById(textFlowTargetId, false);
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

    DocumentDAO getDocumentDAO() {
        return serviceLocator.getInstance(DocumentDAO.class);
    }

    TextFlowTargetDAO getTextFlowTargetDAO() {
        return serviceLocator.getInstance(TextFlowTargetDAO.class);
    }

    TextFlowDAO getTextFlowDAO() {
        return serviceLocator.getInstance(TextFlowDAO.class);
    }

    private static class DocumentStatisticLoader extends
            CacheLoader<DocumentLocaleKey, WordStatistic> {

        private DocumentDAO getDocumentDAO() {
            return ServiceLocator.instance().getInstance(DocumentDAO.class);
        }

        @Override
        public WordStatistic load(DocumentLocaleKey key) throws Exception {
            WordStatistic wordStatistic = getDocumentDAO().getWordStatistics(
                    key.getDocumentId(), key.getLocaleId());
            return wordStatistic;
        }
    }

    private static class HTextFlowTargetIdLoader extends
            CacheLoader<DocumentLocaleKey, DocumentStatus> {
        DocumentDAO getDocumentDAO() {
            return ServiceLocator.instance().getInstance(DocumentDAO.class);
        }

        @Override
        public DocumentStatus load(DocumentLocaleKey key) throws Exception {
            DocumentDAO documentDAO = getDocumentDAO();
            HTextFlowTarget target =
                    documentDAO.getLastTranslatedTarget(key.getDocumentId(),
                            key.getLocaleId());
            DocumentStatus documentStatus = new DocumentStatus();

            return updateDocumentStatus(documentDAO, documentStatus,
                    key.getDocumentId(), target);
        }
    }

    private static class HTextFlowTargetValidationLoader extends
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

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    public static final class DocumentLocaleKey implements Serializable {
        private Long documentId;
        private LocaleId localeId;
    }
}
