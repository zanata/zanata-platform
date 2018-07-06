/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.config.MTServiceToken;
import org.zanata.config.MTServiceURL;
import org.zanata.config.MTServiceUser;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.dto.MachineTranslationPrefill;
import org.zanata.service.LocaleService;
import org.zanata.service.MachineTranslationService;
import org.zanata.service.TranslationService;
import org.zanata.service.VersionStateCache;
import org.zanata.service.mt.TextFlowsToMTDoc;
import org.zanata.service.mt.dto.MTDocument;
import org.zanata.service.mt.dto.TypeString;
import org.zanata.transaction.TransactionUtil;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class MachineTranslationServiceImpl implements
        MachineTranslationService {
    private static final int BATCH_SIZE = 100;
    private static final Logger log =
            LoggerFactory.getLogger(MachineTranslationServiceImpl.class);

    // this is org.zanata.magpie.model.BackendID#GOOGLE
    private static final String BACKEND_GOOGLE = "GOOGLE";

    private URI mtServiceURL;
    private String mtUser;
    private String mtToken;
    private TextFlowsToMTDoc textFlowsToMTDoc;
    private TextFlowDAO textFlowDAO;
    private TextFlowTargetDAO textFlowTargetDAO;
    private LocaleService localeService;
    private EntityManager entityManager;
    private TransactionUtil transactionUtil;
    private TranslationService translationService;
    private VersionStateCache versionStateCache;

    @SuppressWarnings("unused")
    MachineTranslationServiceImpl() {
    }

    @Inject
    public MachineTranslationServiceImpl(@MTServiceURL URI mtServiceURL,
            @MTServiceUser String mtUser,
            @MTServiceToken String mtToken,
            TextFlowsToMTDoc textFlowsToMTDoc,
            TextFlowDAO textFlowDAO,
            TextFlowTargetDAO textFlowTargetDAO,
            LocaleService localeService,
            EntityManager entityManager,
            TransactionUtil transactionUtil,
            TranslationService translationService,
            VersionStateCache versionStateCache) {
        this.mtServiceURL = mtServiceURL;
        this.mtUser = mtUser;
        this.mtToken = mtToken;
        this.textFlowsToMTDoc = textFlowsToMTDoc;
        this.textFlowDAO = textFlowDAO;
        this.textFlowTargetDAO = textFlowTargetDAO;
        this.localeService = localeService;
        this.entityManager = entityManager;
        this.transactionUtil = transactionUtil;
        this.translationService = translationService;
        this.versionStateCache = versionStateCache;
    }

    @Override
    public List<String> getSuggestion(@Nonnull HTextFlow textFlow,
            @Nonnull LocaleId fromLocale,
            @Nonnull LocaleId toLocale) {

        HDocument document = textFlow.getDocument();
        String docId = document.getDocId();
        String versionSlug = document.getProjectIteration().getSlug();
        String projectSlug = document.getProjectIteration().getProject().getSlug();

        MTDocument doc = textFlowsToMTDoc.fromSingleTextFlow(projectSlug,
                versionSlug, docId, fromLocale, textFlow);

        try {
            MTDocument result = getTranslationFromMT(doc, toLocale);
            return result.getContents().stream().map(TypeString::getValue)
                    .collect(Collectors.toList());
        } catch (ZanataServiceException e) {
            log.error("failed to get translations from machine translation");
            return Collections.emptyList();
        }

    }

    private MTDocument getTranslationFromMT(MTDocument request,
            LocaleId toLocale) throws ZanataServiceException {
        ResteasyClient client = new ResteasyClientBuilder().build();

        ResteasyWebTarget webTarget = client.target(mtServiceURL).path("api")
                .path("document").path("translate")
                .queryParam("toLocaleCode", toLocale.getId());
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Auth-User", mtUser)
                .header("X-Auth-Token", mtToken)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Accept", MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        if (response.getStatus() == 200) {
            MTDocument result = response.readEntity(MTDocument.class);
            if (!result.getWarnings().isEmpty()) {
                log.warn("Machine translation returns warning: {}", result.getWarnings());
            }
            return result;
        } else {
            log.warn("Machine translation return status: {}, header: {}, body: {}",
                    response.getStatus(), response.getHeaders(),
                    response.readEntity(String.class));
            throw new ZanataServiceException("failed to get translations from machine translation");
        }
    }

    @Async
    @Override
    public Future<Void> prefillProjectVersionWithMachineTranslation(
            long versionId, MachineTranslationPrefill prefillRequest,
            @Nonnull MachineTranslationPrefillTaskHandle taskHandle) {
        // need to reload all entities
        HProjectIteration version = entityManager.find(HProjectIteration.class, versionId);
        Map<String, HDocument> documents = version.getDocuments();
        if (documents.isEmpty()) {
            log.warn("no document in this version {}", version.userFriendlyToString());
            return AsyncTaskResult.completed();
        }
        taskHandle.setMaxProgress(documents.size());
        HLocale targetLocale = localeService.getByLocaleId(prefillRequest.getToLocale());

        Stopwatch overallStopwatch = Stopwatch.createStarted();
        Long targetVersionId = version.getId();
        String projectSlug = version.getProject().getSlug();
        String versionSlug = version.getSlug();

        log.info("prepare to send {} of documents to MT", documents.size());
        // We clear the cache because we apparently don't trust the incremental
        // stats calculations. By clearing first, we save the cache from
        // trying to keep stats up to date during the merge.
        versionStateCache.clearVersionStatsCache(targetVersionId);

        boolean cancelled = false;
        for (Iterator<HDocument> iterator = documents.values().iterator();
                iterator.hasNext() && !(cancelled = taskHandle.isCancelled()); ) {
            HDocument doc = iterator.next();
            addMachineTranslationsToDoc(doc, targetLocale, projectSlug,
                    versionSlug, prefillRequest.getSaveState());
            taskHandle.increaseProgress(1);
        }
        // Clear the cache again to force recalculation (just in case of
        // concurrent activity):
        versionStateCache.clearVersionStatsCache(targetVersionId);

        log.info("{} prefill translation with machine translations for version {}, {}",
                cancelled ? "CANCELLED" : "COMPLETED",
                version.userFriendlyToString(), overallStopwatch);
        return AsyncTaskResult.completed();
    }

    private void addMachineTranslationsToDoc(HDocument doc,
            HLocale targetLocale, String projectSlug,
            String versionSlug, ContentState saveState) {
        DocumentId documentId = new DocumentId(doc.getId(),
                doc.getDocId());
        List<HTextFlow> untranslatedTextFlows =
                getTextFlowsByDocumentIdWithConstraints(targetLocale,
                        documentId);
        MTDocument mtDocument = textFlowsToMTDoc
                .fromTextFlows(projectSlug, versionSlug,
                        doc.getDocId(), doc.getSourceLocaleId(),
                        untranslatedTextFlows);
        MTDocument result = getTranslationFromMT(mtDocument,
                targetLocale.getLocaleId());
        translateInBatch(untranslatedTextFlows, result, targetLocale, saveState);
    }

    @NotNull
    private LocaleId getSourceLocale(Map<String, HDocument> documents) {
        assert !documents.isEmpty();
        // TODO this assumes all documents using same source locale
        Iterator<HDocument> iterator = documents.values().iterator();
        return iterator.next().getSourceLocaleId();
    }

    private List<HTextFlow> getTextFlowsByDocumentIdWithConstraints(
            HLocale targetLocale, DocumentId documentId) {
        // right now we only target untranslated. We might target more in the future
        return textFlowDAO
                .getAllTextFlowByDocumentIdWithConstraints(
                        documentId, targetLocale,
                        FilterConstraints.builder()
                                .keepNone().includeNew()
                                .build());
    }

    private void translateInBatch(List<HTextFlow> textFlows,
            MTDocument transDoc,
            HLocale targetLocale, ContentState saveState) {
        int index = 0;
        while (index < textFlows.size()) {
            // work out upper bound of index for each batch
            int bound = Math.min(index + BATCH_SIZE, textFlows.size());
            List<HTextFlow> sourceBatch = textFlows.subList(index, bound);
            List<TypeString> transContentBatch =
                    transDoc.getContents().subList(index, bound);

            index += bound;

            try {
                transactionUtil.run(() -> {
                    List<TransUnitUpdateRequest> updateRequests =
                            makeUpdateRequestsForBatch(targetLocale,
                                    transDoc.getBackendId(),
                                    saveState,
                                    sourceBatch,
                                    transContentBatch);
                    translationService.translate(targetLocale.getLocaleId(), updateRequests);
                });
            } catch (Exception e) {
                log.error("error prefilling translation with machine translation", e);
            }
        }
    }

    /**
     * for each batch of text flows, we get the matching translation from MT result.
     */
    private List<TransUnitUpdateRequest> makeUpdateRequestsForBatch(
            HLocale targetLocale,
            String backendId,
            ContentState saveState,
            List<HTextFlow> sourceBatch,
            List<TypeString> transContentBatch) {
        List<TransUnitUpdateRequest> updateRequests = Lists.newArrayList();

        for (int i = 0; i < sourceBatch.size(); i++) {
            HTextFlow textFlow = sourceBatch.get(i);
            TypeString matchingTranslationFromMT = transContentBatch.get(i);

            HTextFlowTarget maybeTarget = textFlowTargetDAO.getTextFlowTarget(textFlow, targetLocale);
            int baseRevision = maybeTarget == null ? 0 : maybeTarget.getVersionNum();
            List<String> translation = Lists.newArrayList(
                    matchingTranslationFromMT.getValue());
            // TODO store backendID/provider in a new DB column (perhaps JSON)
            TransUnitUpdateRequest updateRequest =
                    new TransUnitUpdateRequest(
                            new TransUnitId(textFlow.getId()),
                            translation,
                            saveState,
                            baseRevision,
                            TranslationSourceType.MACHINE_TRANS.getAbbr());
            updateRequest.addRevisionComment(getRevisionComment(backendId));
            updateRequests.add(updateRequest);
        }
        return updateRequests;
    }

    private String getRevisionComment(String backendId) {
        if (BACKEND_GOOGLE.equals(backendId)) {
            return "Translated by Google";
        } else {
            log.warn("Unexpected MT backendId: {}", backendId);
            return "Translated by MT backendId: " + backendId;
        }
    }

}
