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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.config.MTServiceToken;
import org.zanata.config.MTServiceURL;
import org.zanata.config.MTServiceUser;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.TranslationSourceType;
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

import static java.util.stream.Collectors.toList;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class MachineTranslationServiceImpl implements
        MachineTranslationService {
    private static final int SAVE_BATCH_SIZE = 100;
    private static final int REQUEST_BATCH_SIZE = 1000;
    private static final Logger log =
            LoggerFactory.getLogger(MachineTranslationServiceImpl.class);

    // this is org.zanata.magpie.model.BackendID#GOOGLE
    private static final String BACKEND_GOOGLE = "GOOGLE";
    private static final String BACKEND_DEV = "DEV";

    private URI mtServiceURL;
    private String mtUser;
    private String mtToken;
    private TextFlowsToMTDoc textFlowsToMTDoc;
    private TextFlowDAO textFlowDAO;
    private TextFlowTargetDAO textFlowTargetDAO;
    private ProjectIterationDAO projectIterationDAO;
    private LocaleService localeService;
    private EntityManager entityManager;
    private TransactionUtil transactionUtil;
    private TranslationService translationService;
    private VersionStateCache versionStateCache;
    private AttributionService attributionService;

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
            ProjectIterationDAO projectIterationDAO,
            LocaleService localeService,
            EntityManager entityManager,
            TransactionUtil transactionUtil,
            TranslationService translationService,
            VersionStateCache versionStateCache,
            AttributionService attributionService) {
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
        this.attributionService = attributionService;
        this.projectIterationDAO = projectIterationDAO;
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
                versionSlug, docId, fromLocale, textFlow, BACKEND_GOOGLE);

        try {
            MTDocument result = getTranslationFromMT(doc, toLocale);
            return result.getContents().stream().map(TypeString::getValue)
                    .collect(toList());
        } catch (ZanataServiceException e) {
            log.error("failed to get translations from machine translation");
            return Collections.emptyList();
        }

    }

    private MTDocument getTranslationFromMT(MTDocument request,
            LocaleId toLocale) throws ZanataServiceException {

        try {
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

            int status = response.getStatus();
            if (status == 200) {
                MTDocument result = response.readEntity(MTDocument.class);
                if (!result.getWarnings().isEmpty()) {
                    log.warn("MT returned warnings: {}", result.getWarnings());
                }
                return result;
            } else {
                String entity = response.readEntity(String.class);
                log.error("MT returned status: {}, header: {}, body: {}",
                        status, response.getHeaders(), entity);
                throw new ZanataServiceException("Error code " + status +
                        " returned from MT: " + entity);
            }
        } catch (ProcessingException e) {
            throw new ZanataServiceException("Exception while talking to MT", 502, e);
        }
    }

    @Async
    @Override
    public Future<Void> prefillProjectVersionWithMachineTranslation(
            long versionId, MachineTranslationPrefill options,
            @Nonnull MachineTranslationPrefillTaskHandle taskHandle) {
        // need to reload all entities
        HProjectIteration version = entityManager.find(HProjectIteration.class, versionId);
        Map<String, HDocument> documents = version.getDocuments();
        if (documents.isEmpty()) {
            log.info("No documents in {}", version.userFriendlyToString());
            return AsyncTaskResult.completed();
        }
        // Set taskHandle to count the textflows of all documents
        taskHandle.setMaxProgress(projectIterationDAO
                        .getTotalMessageCountForIteration(versionId));
        HLocale targetLocale = localeService.getByLocaleId(options.getToLocale());
        Stopwatch overallStopwatch = Stopwatch.createStarted();
        Long targetVersionId = version.getId();
        String projectSlug = version.getProject().getSlug();
        String versionSlug = version.getSlug();

        log.info("Sending {} document(s) to MT", documents.size());

        // We clear the cache because we apparently don't trust the incremental
        // stats calculations. By clearing first, we save the cache from
        // trying to keep stats up to date during the merge.
        versionStateCache.clearVersionStatsCache(targetVersionId);

        boolean cancelled = false;
        for (Iterator<HDocument> iterator = documents.values().iterator();
                iterator.hasNext() && !(cancelled = taskHandle.isCancelled()); ) {
            HDocument doc = iterator.next();
            Long docId = doc.getId();
            if (!attributionService.supportsAttribution(doc)) {
                log.warn("Attribution not supported for {}; skipping MT", doc);
                taskHandle.increaseProgress(doc.getTextFlows().size());
                continue;
            }
            String requestedBackend = BACKEND_GOOGLE;
            String backendId = addMachineTranslationsToDoc(doc, targetLocale,
                    projectSlug, versionSlug, options.getSaveState(),
                    options.getOverwriteFuzzy(), requestedBackend, taskHandle);
            if (backendId != null) {
                try {
                    transactionUtil.run(() -> {
                        // Refresh HDocument after being cleared in processing
                        HDocument refreshedDoc = entityManager.find(HDocument.class, docId);
                        attributionService.addAttribution(refreshedDoc, targetLocale, backendId);
                        entityManager.merge(refreshedDoc);
                    });
                } catch (Exception e) {
                    throw new RuntimeException("error adding attribution for machine translation", e);
                }
            }
        }
        // Clear the cache again to force recalculation (just in case of
        // concurrent activity):
        versionStateCache.clearVersionStatsCache(targetVersionId);

        log.info("{} prefill with MT for {}, {}",
                cancelled ? "CANCELLED" : "COMPLETED",
                version.userFriendlyToString(), overallStopwatch);
        return AsyncTaskResult.completed();
    }

    private @Nullable String addMachineTranslationsToDoc(HDocument doc,
                                                         HLocale targetLocale,
                                                         String projectSlug,
                                                         String versionSlug,
                                                         ContentState saveState,
                                                         boolean overwriteFuzzy,
                                                         String backendId,
                                                         AsyncTaskHandle taskHandle) {
        DocumentId documentId = new DocumentId(doc.getId(),
                doc.getDocId());
        entityManager.clear();
        List<HTextFlow> textFlowsToTranslate =
                getTextFlowsByDocumentIdWithConstraints(targetLocale,
                        documentId, overwriteFuzzy);
        // Increase progress for non-translated items
        int textFlowsToSkip = entityManager
                .find(HDocument.class, doc.getId()).getTextFlows().size()
                - textFlowsToTranslate.size();
        taskHandle.increaseProgress(textFlowsToSkip);
        if (textFlowsToTranslate.isEmpty()) {
            log.info("No eligible text flows in document {}", doc.getQualifiedDocId());
            return null;
        }
        int startBatch = 0;
        String backendIdConfirmation = null;
        while (startBatch < textFlowsToTranslate.size()) {
            int batchEnd = Math.min(
                    startBatch + REQUEST_BATCH_SIZE, textFlowsToTranslate.size());
            log.debug("[PERF] Starting batch {} - {}", startBatch, batchEnd);
            List<HTextFlow> textFlowBatch =
                    textFlowsToTranslate.subList(startBatch, batchEnd);
            MTDocument mtDocument = textFlowsToMTDoc
                    .fromTextFlows(projectSlug, versionSlug,
                            doc.getDocId(), doc.getSourceLocaleId(),
                            textFlowBatch,
                            TextFlowsToMTDoc::extractPluralIfPresent,
                            backendId);

            log.debug("[PERF] Sending batch {} - {}", startBatch, batchEnd);
            Stopwatch mtProviderStopwatch = Stopwatch.createStarted();
            MTDocument result = getTranslationFromMT(mtDocument,
                    targetLocale.getLocaleId());
            log.debug("[PERF] Received response [{} contents] ({}ms)",
                    result.getContents().size(), mtProviderStopwatch);
            saveTranslationsInBatches(textFlowBatch, result, targetLocale, saveState);
            // TODO we only return the backendId from the final batch
            backendIdConfirmation = result.getBackendId();
            startBatch = batchEnd;
            taskHandle.increaseProgress(textFlowBatch.size());
        }
        if (backendIdConfirmation == null) {
            log.warn("Error getting confirmation backend ID for {}", doc.getDocId());
        }
        return backendIdConfirmation;
    }

    private List<HTextFlow> getTextFlowsByDocumentIdWithConstraints(
            HLocale targetLocale, DocumentId documentId, boolean overwriteFuzzy) {
        FilterConstraints.Builder constraints = FilterConstraints.builder()
                .keepNone()
                .includeNew();
        if (overwriteFuzzy) {
            constraints.includeFuzzy();
        }
        return textFlowDAO.getAllTextFlowByDocumentIdWithConstraints(
                documentId, targetLocale, constraints.build());
    }

    private void saveTranslationsInBatches(List<HTextFlow> textFlows,
            MTDocument transDoc,
            HLocale targetLocale, ContentState saveState) {
        int batchStart = 0;
        log.debug("[PERF] Saving {} translations in batches", textFlows.size());
        Stopwatch saveAllTimer = Stopwatch.createStarted();
        while (batchStart < textFlows.size()) {
            // work out upper bound of index for each batch
            int batchEnd = Math.min(batchStart + SAVE_BATCH_SIZE, textFlows.size());
            List<HTextFlow> sourceBatch = textFlows.subList(batchStart, batchEnd);
            List<TypeString> transContentBatch =
                    transDoc.getContents().subList(batchStart, batchEnd);
            log.debug("[PERF] Batch save transaction {} - {}", batchStart, batchEnd);
            Stopwatch transactionTime = Stopwatch.createStarted();
            try {
                transactionUtil.run(() -> {
                    List<TransUnitUpdateRequest> updateRequests =
                            makeUpdateRequestsForBatch(targetLocale,
                                    transDoc.getBackendId(),
                                    saveState,
                                    sourceBatch,
                                    transContentBatch);
                    Stopwatch storeTranslations = Stopwatch.createStarted();
                    translationService.translate(targetLocale.getLocaleId(), updateRequests);
                    log.debug("[PERF] Commit translations to database ({}ms)", storeTranslations);

                });
            } catch (Exception e) {
                log.error("Error filling target with machine translation", e);
            }

            // Clear here to reduce the entityManager cache
            entityManager.clear();
            batchStart = batchEnd;
            log.debug("[PERF] Transaction complete {} - {} ({}ms)", batchStart,
                    batchEnd, transactionTime);
        }
        log.debug("[PERF] Batches complete ({}ms)", saveAllTimer);
    }

    /**
     * for each batch of text flows, we get the matching translation from MT result.
     */
    private List<TransUnitUpdateRequest> makeUpdateRequestsForBatch(
            HLocale targetLocale,
            String backendId,
            ContentState requestedSaveState,
            List<HTextFlow> sourceBatch,
            List<TypeString> transContentBatch) {
        List<TransUnitUpdateRequest> updateRequests = Lists.newArrayList();
        log.debug("[PERF] Creating {} save requests", sourceBatch.size());
        Stopwatch createRequestsTimer = Stopwatch.createStarted();
        for (int i = 0; i < sourceBatch.size(); i++) {
            HTextFlow textFlow = sourceBatch.get(i);
            ContentState saveState =
                    getActualSaveState(requestedSaveState, textFlow);
            // Note that org.zanata.service.impl.TranslationServiceImpl.saveBatch
            // will skip the save for any TFT where validation fails.
            // TODO it would generally be better to save as Fuzzy, unless there
            // is an existing translation (which passes validation) with state
            // Translated or better.
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
        log.debug("[PERF] Created {} requests ({}ms)", updateRequests.size(),
                createRequestsTimer);
        return updateRequests;
    }

    private ContentState getActualSaveState(ContentState requestedSaveState,
            HTextFlow textFlow) {
        // If text flow has plurals, there's a higher chance it contains
        // variables, eg "%d files were saved."
        // So we save its machine translation as NeedReview
        // because MT probably won't handle cases like this well.
        // TODO perhaps we should just decide this based on validation in TranslationService
        return hasPluralForm(textFlow) ? ContentState.NeedReview : requestedSaveState;
    }

    private boolean hasPluralForm(HTextFlow it) {
        return it.getContents().size() > 1;
    }

    private String getRevisionComment(String backendId) {
        return attributionService.getAttributionMessage(backendId);
    }

}
