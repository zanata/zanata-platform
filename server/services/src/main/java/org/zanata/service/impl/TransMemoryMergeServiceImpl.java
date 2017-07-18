/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.events.TransMemoryMergeEvent;
import org.zanata.events.TransMemoryMergeProgressEvent;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.ModelEntityBase;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.type.EntityType;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LocaleService;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.service.TranslationService;
import org.zanata.service.VersionStateCache;
import org.zanata.transaction.TransactionUtil;
import org.zanata.util.TransMemoryMergeStatusResolver;
import org.zanata.util.TranslationUtil;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.dto.HasTMMergeCriteria;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("transMemoryMergeServiceImpl")
@RequestScoped
@Transactional
public class TransMemoryMergeServiceImpl implements TransMemoryMergeService {
    private static final Logger log = LoggerFactory
            .getLogger(TransMemoryMergeServiceImpl.class);
    private static final String commentPrefix =
            "auto translated by TM merge from";
    @VisibleForTesting
    protected static final int BATCH_SIZE = 50;
    private static final FilterConstraints
            UNTRANSLATED_FILTER = FilterConstraints.builder().keepAll().excludeRejected()
                    .excludeApproved().excludeTranslated()
                    .excludeFuzzy().build();

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private TransMemoryUnitDAO transMemoryUnitDAO;
    @Inject
    private TranslationMemoryService translationMemoryServiceImpl;
    @Inject
    private TranslationService translationServiceImpl;
    @Inject
    private Event<TextFlowTargetUpdateContextEvent> textFlowTargetUpdateContextEvent;

    @Inject
    private Event<TransMemoryMergeEvent> transMemoryMergeEvent;
    @Inject
    private Event<TransMemoryMergeProgressEvent> transMemoryMergeProgressEvent;
    @Inject
    private TransactionUtil transactionUtil;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private VersionStateCache versionStateCacheImpl;

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;


    @Override
    public List<TranslationService.TranslationResult> executeMerge(
            TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle) {
        List<TranslationService.TranslationResult> finalResult = Lists.newLinkedList();
        final WorkspaceId workspaceId =
                new WorkspaceId(request.projectIterationId, request.localeId);

        Date startTime = new Date();
        int index = 0;
        long total = 0;
        try {
            HLocale targetLocale =
                    localeServiceImpl.getByLocaleId(request.localeId);

            total = textFlowDAO
                    .getUntranslatedTextFlowCount(request.documentId, targetLocale);

            transMemoryMergeEvent.fire(TransMemoryMergeEvent.start(workspaceId,
                    startTime, authenticatedAccount.getUsername(),
                    request.editorClientId, request.documentId, total));

            asyncTaskHandle.setMaxProgress(total);
            asyncTaskHandle.setTMMergeTarget(request.projectIterationId,
                    request.documentId, request.localeId);

            while (index < total) {
                if (asyncTaskHandle.isCancelled()) {
                    break;
                }
                // we can't use streaming result set here as following will occur:
                // Streaming result set com.mysql.jdbc.RowDataDynamic@5dbc0e06 is
                // still active. No statements may be issued when any streaming
                // result sets are open and in use on a given connection.
                List<HTextFlow> textFlowsBatch = textFlowDAO
                        .getTextFlowByDocumentIdWithConstraints(request.documentId,
                                targetLocale,
                                UNTRANSLATED_FILTER, index, BATCH_SIZE);
                int processedSize = textFlowsBatch.size();
                index = index + processedSize;
                asyncTaskHandle.increaseProgress(processedSize);

                Consumer<TransUnitUpdateRequest>
                        callback =
                         updateRequest -> textFlowTargetUpdateContextEvent
                                 .fire(new TextFlowTargetUpdateContextEvent(
                                         updateRequest.getTransUnitId(),
                                         request.localeId,
                                         request.editorClientId,
                                         TransUnitUpdated.UpdateType.NonEditorSave));
                List<TranslationService.TranslationResult> batchResult =
                        translateInBatch(request, textFlowsBatch, targetLocale,
                                Collections.emptyList(), Optional.of(callback));
                finalResult.addAll(batchResult);
                log.debug("TM merge handle: {}", asyncTaskHandle);
                transMemoryMergeProgressEvent
                        .fire(new TransMemoryMergeProgressEvent(workspaceId, total,
                                asyncTaskHandle.getCurrentProgress(),
                                request.editorClientId, request.documentId));
            }
        } catch (Exception e) {
            log.error("exception happen in TM merge", e);
            transMemoryMergeEvent.fire(TransMemoryMergeEvent.end(workspaceId,
                    startTime, authenticatedAccount.getUsername(),
                    request.editorClientId, request.documentId, total));
            // TODO pahuang need to publish a notification to tell the user something went wrong
        } finally {
            transMemoryMergeEvent.fire(TransMemoryMergeEvent.end(workspaceId,
                    startTime, authenticatedAccount.getUsername(),
                    request.editorClientId, request.documentId, total));
        }

        return finalResult;
    }

    @Async
    @Override
    public Future<List<TranslationService.TranslationResult>> executeMergeAsync(TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle) {
        List<TranslationService.TranslationResult> translationResults =
                executeMerge(request, asyncTaskHandle);
        return AsyncTaskResult.completed(translationResults);
    }

    @Async
    @Override
    public Future<Void> startMergeTranslations(Long targetVersionId,
            VersionTMMerge mergeRequest,
            MergeTranslationsTaskHandle handle) {
        // since this is async we need to reload entities
        HProjectIteration targetVersion =
                projectIterationDAO.findById(targetVersionId);
        HLocale targetLocale =
                localeServiceImpl.getByLocaleId(mergeRequest.getLocaleId());

        List<HLocale> localesInTargetVersion = localeServiceImpl
                .getSupportedLanguageByProjectIteration(targetVersion);
        if (!localesInTargetVersion.contains(targetLocale)) {
            log.error("No locales enabled in target version of [{}]",
                    targetVersion.userFriendlyToString());
            return AsyncTaskResult.completed();
        }

        List<Long> fromVersionIds = mergeRequest.getFromProjectVersions().stream()
                .map(projectIterationId -> projectIterationDAO.getBySlug(
                        projectIterationId.getProjectSlug(),
                        projectIterationId.getIterationSlug()))
                .filter(ver -> ver != null
                        && ver.getStatus() != EntityStatus.OBSOLETE
                        && localeServiceImpl
                        .getSupportedLanguageByProjectIteration(ver)
                        .contains(targetLocale))
                .map(ModelEntityBase::getId).collect(Collectors.toList());


        long mergeTargetCount = textFlowDAO.getUntranslatedOrFuzzyTextFlowCountInVersion(
                targetVersion.getId(), targetLocale);

        Optional<MergeTranslationsTaskHandle>
                taskHandleOpt = Optional.ofNullable(handle);
        if (taskHandleOpt.isPresent()) {
            MergeTranslationsTaskHandle handle1 = taskHandleOpt.get();
            handle1.setTriggeredBy(identity.getAccountUsername());
            handle1.setMaxProgress((int) mergeTargetCount);
            handle1.setTotalTranslations(mergeTargetCount);
        }
        Stopwatch overallStopwatch = Stopwatch.createStarted();
        log.info("merge translations from TM start: from {} to {}",
                fromVersionIds,
                targetVersion.userFriendlyToString());
        int startCount = 0;

        while (startCount < mergeTargetCount) {
            List<HTextFlow> batch = textFlowDAO
                    .getUntranslatedOrFuzzyTextFlowsInVersion(
                            targetVersion.getId(), targetLocale, startCount,
                            BATCH_SIZE);
            translateInBatch(mergeRequest, batch,
                    targetLocale, fromVersionIds, Optional.empty());

            taskHandleOpt.ifPresent(
                    mergeTranslationsTaskHandle -> mergeTranslationsTaskHandle
                            .increaseProgress(batch.size()));
            startCount += BATCH_SIZE;
        }
        versionStateCacheImpl.clearVersionStatsCache(targetVersion.getId());
        log.info("merge translation from TM end: from {} to {}, {}",
                mergeRequest.getFromProjectVersions(),
                targetVersion.userFriendlyToString(), overallStopwatch);
        return AsyncTaskResult.completed();
    }

    /**
     * This method will run in transaction and manages its own transaction.
     *
     * @param request
     *            TM merge request criteria
     * @param textFlows
     *            the text flows to be filled
     * @param targetLocale
     *            target locale
     * @param fromVersionIds
     *            source version ids
     * @param callbackOnUpdate
     *            an optional callback to call when we have a
     *            TransUnitUpdateRequest ready
     * @return translation results
     */
    private List<TranslationService.TranslationResult> translateInBatch(
            HasTMMergeCriteria request, List<HTextFlow> textFlows,
            HLocale targetLocale,
            List<Long> fromVersionIds,
            Optional<Consumer<TransUnitUpdateRequest>> callbackOnUpdate) {

        if (textFlows.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return transactionUtil.call(() -> {
                boolean checkContext =
                        request.getDifferentContextRule() == MergeRule.REJECT;
                boolean checkDocument =
                        request.getDifferentDocumentRule() == MergeRule.REJECT;
                boolean checkProject =
                        request.getDifferentProjectRule() == MergeRule.REJECT;

                List<TransUnitUpdateRequest> updateRequests = Lists.newLinkedList();
                for (HTextFlow hTextFlow : textFlows) {
                    HTextFlowTarget hTextFlowTarget =
                            hTextFlow.getTargets().get(targetLocale.getId());
                    Optional<TransMemoryResultItem> tmResult =
                            translationMemoryServiceImpl.searchBestMatchTransMemory(
                                    hTextFlow, targetLocale.getLocaleId(),
                                    hTextFlow.getDocument().getLocale().getLocaleId(),
                                    checkContext, checkDocument, checkProject,
                                    request.getThresholdPercent(),
                                    fromVersionIds);
                    if (tmResult.isPresent()) {
                        TransUnitUpdateRequest updateRequest =
                                createRequest(request, targetLocale,
                                        hTextFlow, tmResult.get(), hTextFlowTarget);

                        if (updateRequest != null) {
                            updateRequests.add(updateRequest);
                            callbackOnUpdate.ifPresent(c -> c.accept(updateRequest));
                        }
                    }
                }
                return translationServiceImpl.translate(
                        targetLocale.getLocaleId(), updateRequests);
            });
        } catch (Exception e) {
            log.error("exception during TM merge", e);
            return Collections.emptyList();
        }
    }

    private TransUnitUpdateRequest createRequest(HasTMMergeCriteria action,
            HLocale hLocale,
            HTextFlow hTextFlowToBeFilled, TransMemoryResultItem tmResult,
            HTextFlowTarget oldTarget) {
        Long tmSourceId = tmResult.getSourceIdList().get(0);
        ContentState statusToSet;
        String comment;
        String revisionComment;
        String entityType;
        Long entityId;
        if (tmResult
                .getMatchType() == TransMemoryResultItem.MatchType.Imported) {
            TransMemoryUnit tu = transMemoryUnitDAO.findById(tmSourceId);
            statusToSet = TransMemoryMergeStatusResolver.newInstance()
                    .decideStatus(action, tmResult, oldTarget);
            comment = buildTargetComment(tu);
            revisionComment = TranslationUtil.getTMMergeMessage(tu);
            entityId = tu.getId();
            entityType = EntityType.TMX.getAbbr();
        } else {
            HTextFlow tmSource = textFlowDAO.findById(tmSourceId, false);
            TransMemoryDetails tmDetail = translationMemoryServiceImpl
                    .getTransMemoryDetail(hLocale, tmSource);
            statusToSet = TransMemoryMergeStatusResolver.newInstance()
                    .decideStatus(action, hTextFlowToBeFilled, tmDetail,
                            tmResult, oldTarget);
            comment = buildTargetComment(tmDetail);
            revisionComment = TranslationUtil.getTMMergeMessage(tmDetail);
            HTextFlowTarget target = tmSource.getTargets().get(hLocale.getId());
            entityId = TranslationUtil.getCopiedEntityId(target);
            entityType = TranslationUtil.getCopiedEntityType(target).getAbbr();
        }
        if (statusToSet != null) {

            TransUnitUpdateRequest request =
                    new TransUnitUpdateRequest(new TransUnitId(hTextFlowToBeFilled.getId()),
                            tmResult.getTargetContents(), statusToSet,
                            oldTarget == null ? 0 : oldTarget.getVersionNum(),
                            revisionComment, entityId, entityType,
                            TranslationSourceType.TM_MERGE.getAbbr());
            request.addTargetComment(comment);
            log.debug("auto translate from translation memory {}", request);
            return request;
        }
        return null;
    }

    private static String buildTargetComment(TransMemoryDetails tmDetail) {
        return new StringBuilder(commentPrefix).append(" project: ")
                .append(tmDetail.getProjectName()).append(", version: ")
                .append(tmDetail.getIterationName()).append(", DocId: ")
                .append(tmDetail.getDocId()).toString();
    }

    private static String buildTargetComment(TransMemoryUnit tu) {
        return new StringBuilder(commentPrefix).append(" translation memory: ")
                .append(tu.getTranslationMemory().getSlug())
                .append(", unique id: ").append(tu.getUniqueId()).toString();
    }
}
