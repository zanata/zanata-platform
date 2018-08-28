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
package org.zanata.service.tm.merge;

import static java.util.Collections.singletonList;
import static org.zanata.webtrans.shared.rest.dto.InternalTMSource.InternalTMChoice.SelectSome;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.mail.internet.InternetAddress;

import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.HasContents;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TransMemoryUnitDAO;
import org.zanata.email.Addresses;
import org.zanata.email.HtmlEmailSender;
import org.zanata.email.TMMergeEmailContext;
import org.zanata.email.ProjectInfo;
import org.zanata.email.TMMergeEmailStrategy;
import org.zanata.email.VersionInfo;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.events.TransMemoryMergeEvent;
import org.zanata.events.TransMemoryMergeProgressEvent;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.type.EntityType;
import org.zanata.rest.dto.TranslationSourceType;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LocaleService;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.service.VersionStateCache;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.transaction.TransactionUtil;
import org.zanata.util.TransMemoryMergeStatusResolver;
import org.zanata.util.TranslationUtil;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.dto.HasTMMergeCriteria;
import org.zanata.webtrans.shared.rest.dto.InternalTMSource;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import kotlin.Pair;
import kotlin.ranges.IntRange;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class TransMemoryMergeServiceImpl implements TransMemoryMergeService {
    private static final Consumer<TransUnitUpdateRequest> NOOP = it -> {};

    private static final Logger log = LoggerFactory
            .getLogger(TransMemoryMergeServiceImpl.class);
    private static final String commentPrefix =
            "auto translated by TM merge from";
    private static final FilterConstraints
            UNTRANSLATED_FILTER = FilterConstraints.builder().keepAll().excludeRejected()
                    .excludeApproved().excludeTranslated()
                    .excludeFuzzy().build();
    private static final long serialVersionUID = -6049179312875626300L;

    private LocaleService localeServiceImpl;
    private TextFlowDAO textFlowDAO;
    private TransMemoryUnitDAO transMemoryUnitDAO;
    private TranslationMemoryService translationMemoryServiceImpl;
    private TranslationService translationServiceImpl;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private Event<TextFlowTargetUpdateContextEvent> textFlowTargetUpdateContextEvent;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private Event<TransMemoryMergeEvent> transMemoryMergeEvent;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private Event<TransMemoryMergeProgressEvent> transMemoryMergeProgressEvent;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private TransactionUtil transactionUtil;

    private ProjectIterationDAO projectIterationDAO;

    private VersionStateCache versionStateCacheImpl;

    private HAccount authenticatedAccount;
    private String serverPath;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Serializable collection (plus TransMemoryMergeServiceImpl needn't be Serializable)")
    private Map<ContentState, List<IntRange>> tmBands;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "CDI proxy")
    private HtmlEmailSender emailSender;

    @Inject
    public TransMemoryMergeServiceImpl(
            LocaleService localeServiceImpl, TextFlowDAO textFlowDAO,
            TransMemoryUnitDAO transMemoryUnitDAO,
            TranslationMemoryService translationMemoryServiceImpl,
            TranslationService translationServiceImpl,
            Event<TextFlowTargetUpdateContextEvent> textFlowTargetUpdateContextEvent,
            Event<TransMemoryMergeEvent> transMemoryMergeEvent,
            Event<TransMemoryMergeProgressEvent> transMemoryMergeProgressEvent,
            TransactionUtil transactionUtil,
            ProjectIterationDAO projectIterationDAO,
            VersionStateCache versionStateCacheImpl,
            @Authenticated HAccount authenticatedAccount,
            @ServerPath String serverPath,
            TMBandDefs tmBands,
            HtmlEmailSender emailSender) {
        this.localeServiceImpl = localeServiceImpl;
        this.textFlowDAO = textFlowDAO;
        this.transMemoryUnitDAO = transMemoryUnitDAO;
        this.translationMemoryServiceImpl = translationMemoryServiceImpl;
        this.translationServiceImpl = translationServiceImpl;
        this.textFlowTargetUpdateContextEvent =
                textFlowTargetUpdateContextEvent;
        this.transMemoryMergeEvent = transMemoryMergeEvent;
        this.transMemoryMergeProgressEvent = transMemoryMergeProgressEvent;
        this.transactionUtil = transactionUtil;
        this.projectIterationDAO = projectIterationDAO;
        this.versionStateCacheImpl = versionStateCacheImpl;
        this.authenticatedAccount = authenticatedAccount;
        this.serverPath = serverPath;
        this.tmBands = tmBands.getMap();
        this.emailSender = emailSender;
    }

    public TransMemoryMergeServiceImpl() {
    }

    /**
     * TM Merge for an individual document
     * @param request
     * @param asyncTaskHandle
     * @return
     */
    @Override
    @Transactional
    public List<TranslationResult> executeMerge(
            TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle) {
        List<TranslationResult> finalResult = Lists.newLinkedList();
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
                if (processedSize == 0) {
                    break;
                }
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
                List<TranslationResult> batchResult =
                        translateInBatch(request, textFlowsBatch, targetLocale,
                                request.getInternalTMSource(), callback);
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

    /**
     * Async TM Merge for an individual document
     * @param request
     * @param asyncTaskHandle
     * @return
     */
    @Futureable
    @Override
    @Transactional
    public Future<List<TranslationResult>> executeMergeAsync(TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle) {
        List<TranslationResult> translationResults =
                executeMerge(request, asyncTaskHandle);
        return AsyncTaskResult.completed(translationResults);
    }

    /**
     * TM Merge for an entire project version
     */
    @Futureable
    @Override
    public Future<Void> startMergeTranslations(Long targetVersionId,
            VersionTMMerge mergeRequest,
            @Nonnull MergeTranslationsTaskHandle taskHandle) {
        InternalTMSource internalTMSource = mergeRequest.getInternalTMSource();
        List<Long> fromVersionIds = internalTMSource.getFilteredProjectVersionIds();
        if (internalTMSource.getChoice() == SelectSome
                && fromVersionIds.isEmpty()) {
            log.error(
                    "selected internal TM versions list has nothing to copy from");
            return AsyncTaskResult.completed();
        }
        TMMergeResult mergeResult = new TMMergeResult(tmBands);
        // we need a separate read only transaction to load all the entities and close
        // it so that the following batch job can take as long as it can and no
        // transaction reaper will kick in.
        NeededEntities neededEntities;
        try {
            neededEntities = loadEntities(targetVersionId, mergeRequest);
        } catch (Exception e) {
            log.error("error loading entities", e);
            return AsyncTaskResult.completed();
        }

        long activeTextFlows = neededEntities.activeTextFlows;
        HLocale targetLocale = neededEntities.targetLocale;
        String targetVersionStr = neededEntities.targetVersion.userFriendlyToString();

        if (!neededEntities.isTargetLocaleEnabledInVersion()) {
            log.error("No locales enabled in target version of [{}]",
                    neededEntities.targetVersion.userFriendlyToString());
            return AsyncTaskResult.completed();
        }

        taskHandle.setTriggeredBy(authenticatedAccount.getUsername());
        taskHandle.setMaxProgress(activeTextFlows);
        taskHandle.setTotalTextFlows(activeTextFlows);

        Stopwatch overallStopwatch = Stopwatch.createStarted();
        log.info("merge translations from TM start: from {} to {}",
                mergeRequest.getInternalTMSource(),
                targetVersionStr);
        int textFlowIndex = 0;
        boolean cancelled = false;

        while (textFlowIndex < activeTextFlows && !(cancelled = taskHandle.isCancelled())) {
            int count = mergeTextFlowBatch(targetVersionId, mergeRequest,
                    internalTMSource, mergeResult, targetLocale, textFlowIndex);
            taskHandle.increaseProgress(count);
            textFlowIndex += BATCH_SIZE;
        }
        versionStateCacheImpl.clearVersionStatsCache(targetVersionId);
        log.info("{} merge translations from TM: from {} to {}, {}",
                cancelled ? "CANCELLED" : "COMPLETED",
                mergeRequest.getInternalTMSource(),
                targetVersionStr, overallStopwatch);

        // send email to requester using mergeResult
        sendTMMergeEmail(mergeRequest, mergeResult, neededEntities);

        return AsyncTaskResult.completed();
    }

    private int mergeTextFlowBatch(Long targetVersionId,
            VersionTMMerge mergeRequest, InternalTMSource internalTMSource,
            TMMergeResult mergeResult, HLocale targetLocale,
            int batchStart) {
        // Get the next batch of text flows. They may or may not need
        // translation, but they won't change during the TM merge
        // (unless someone uploads a source document). We will be
        // creating/modifying TFTs, so the offset queries shouldn't use them.
        List<Long> textFlowsIds =
                textFlowDAO.getActiveTextFlowsIdsInProjectVersion(
                        targetVersionId, batchStart, BATCH_SIZE);
        // Look up each text flow and its translation state (if any)
        // This might be more efficient if the HQL query were to filter by
        // translation state directly.
        List<HTextFlow> translatableTextFlows = textFlowDAO
                .getTextFlowAndMaybeTargetState(
                        textFlowsIds, targetLocale.getId())
                // check target's nullable ContentState from Pair
                .filter(pair -> shouldTranslate(pair.component2()))
                // get TextFlow from Pair
                .map(Pair::component1)
                .collect(Collectors.toList());

        List<TranslationResult> batchResults =
            translateInBatch(mergeRequest, translatableTextFlows,
                targetLocale, internalTMSource, NOOP);
        for (TranslationResult result : batchResults) {
            // we don't count copies which failed because of validation or concurrent edits
            if (result.isTranslationSuccessful()) {
                HTextFlow hTextFlow =
                        result.getTranslatedTextFlowTarget().getTextFlow();
                long charCount = codePoints(hTextFlow);
                long wordCount = hTextFlow.getWordCount();
                // round down (assuming non-negative)
                int similarity = (int) result.getSimilarityPercent();
                mergeResult.count(result.getNewContentState(), similarity,
                        charCount, wordCount, 1);
            }
        }
        return textFlowsIds.size();
    }

    @NotNull
    private NeededEntities loadEntities(
            Long targetVersionId, VersionTMMerge mergeRequest)
            throws Exception {
        NeededEntities neededEntities;
        neededEntities = new NeededEntities();
        transactionUtil.call(() -> {
            // since this is async we need to reload entities
            neededEntities.targetVersion =
                    projectIterationDAO.findById(targetVersionId);
            neededEntities.targetLocale =
                    localeServiceImpl.getByLocaleId(mergeRequest.getLocaleId());
            neededEntities.localesInTargetVersion = localeServiceImpl
                    .getSupportedLanguageByProjectIteration(neededEntities.targetVersion);
            neededEntities.activeTextFlows = textFlowDAO.countActiveTextFlowsInProjectIteration(
                    targetVersionId);
            return neededEntities;
        });
        return neededEntities;
    }

    private boolean shouldTranslate(@Nullable ContentState state) {
        if (state == null) return true;
        switch (state) {
            case New:
            case NeedReview:
            case Rejected:
                return true;
            case Translated:
            case Approved:
                return false;
            default:
                log.warn("unexpected state {}", state);
                return false;
        }
    }

    private void sendTMMergeEmail(VersionTMMerge mergeRequest,
            TMMergeResult mergeResult,
            NeededEntities neededEntities) {
        HProjectIteration projVersion = neededEntities.targetVersion;
        HProject proj = projVersion.getProject();
        String verSlug = projVersion.getSlug();
        String projSlug = proj.getSlug();
        String verUrl = serverPath + "/iteration/view/" + projSlug + "/" + verSlug;
        String projUrl = serverPath + "/project/view/" + proj.getSlug();
        List<? extends InternetAddress> toAddresses = singletonList(
                Addresses.getAddress(authenticatedAccount.getPerson()));
        TMMergeEmailContext settings = new TMMergeEmailContext(
                toAddresses,
                new ProjectInfo(proj.getName(), projUrl),
                new VersionInfo(projVersion.getSlug(), verUrl),
                mergeRequest.getLocaleId(),
                new IntRange(mergeRequest.getThresholdPercent(), 100));
        TMMergeEmailStrategy strategy = new TMMergeEmailStrategy(
                settings, mergeResult);
        emailSender.sendMessage(strategy);
    }

    /**
     * Counts the Unicode code points in *all* the contents
     * @param text
     * @return
     */
    private static long codePoints(HasContents text) {
        return text.getContents()
                .stream()
                .mapToLong(s -> (long) s.codePointCount(0, s.length()))
                .sum();
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
     * @param internalTMSource
     *            source versions from internal TM
     * @param callbackOnUpdate
     *            a callback to call when we have a
     *            TransUnitUpdateRequest ready
     * @return translation results
     */
    private List<TranslationResult> translateInBatch(
            HasTMMergeCriteria request, List<HTextFlow> textFlows,
            HLocale targetLocale,
            InternalTMSource internalTMSource,
            Consumer<TransUnitUpdateRequest> callbackOnUpdate) {

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
                    @Nullable HTextFlowTarget hTextFlowTarget =
                            hTextFlow.getTargets().get(targetLocale.getId());
                    Optional<TransMemoryResultItem> tmResult =
                            translationMemoryServiceImpl.searchBestMatchTransMemory(
                                    hTextFlow, targetLocale.getLocaleId(),
                                    hTextFlow.getDocument().getLocale().getLocaleId(),
                                    checkContext, checkDocument, checkProject,
                                    request.getThresholdPercent(),
                                    internalTMSource);
                    if (tmResult.isPresent()) {
                        TransUnitUpdateRequest updateRequest =
                                createRequest(request, targetLocale,
                                        hTextFlow, tmResult.get(), hTextFlowTarget);

                        if (updateRequest != null) {
                            updateRequests.add(updateRequest);
                            callbackOnUpdate.accept(updateRequest);
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
            @Nullable HTextFlowTarget oldTarget) {
        Long tmSourceId = tmResult.getSourceIdList().get(0);
        ContentState statusToSet;
        String comment;
        String revisionComment;
        String entityType;
        Long entityId;
        switch (tmResult.getMatchType()) {
            case Imported:
                TransMemoryUnit tu = transMemoryUnitDAO.findById(tmSourceId);
                statusToSet = TransMemoryMergeStatusResolver.newInstance()
                        .decideStatus(action, tmResult, oldTarget);
                comment = buildTargetComment(tu);
                revisionComment = TranslationUtil.getTMMergeMessage(tu);
                entityId = tu.getId();
                entityType = EntityType.TMX.getAbbr();
                break;
            case TranslatedInternal:
            case ApprovedInternal:
                HTextFlow tmSource = textFlowDAO.findById(tmSourceId, false);
                TransMemoryDetails tmDetail = translationMemoryServiceImpl
                        .getTransMemoryDetail(hLocale, tmSource);
                statusToSet = TransMemoryMergeStatusResolver.newInstance()
                        .decideStatus(action, hTextFlowToBeFilled, tmDetail,
                                tmResult, oldTarget);
                comment = buildTargetComment(tmDetail);
                revisionComment = TranslationUtil.getTMMergeMessage(tmDetail);
                // TODO use a more efficient way to get the HTextFlowTarget directly
                HTextFlowTarget target = tmSource.getTargets().get(hLocale.getId());
                entityId = TranslationUtil.getCopiedEntityId(target);
                entityType = TranslationUtil.getCopiedEntityType(target).getAbbr();
                break;
            default:
                throw new RuntimeException("unhandled match type: " + tmResult.getMatchType());
        }
        if (statusToSet != null) {
            TransUnitUpdateRequest request =
                new TransUnitUpdateRequest(
                    new TransUnitId(hTextFlowToBeFilled.getId()),
                    tmResult.getTargetContents(), statusToSet,
                    oldTarget == null ? 0 : oldTarget.getVersionNum(),
                    revisionComment, entityId, entityType,
                    TranslationSourceType.TM_MERGE.getAbbr(),
                    tmResult.getSimilarityPercent());
            request.addTargetComment(comment);
            log.debug("auto translate from translation memory {}", request);
            return request;
        }
        return null;
    }

    private static String buildTargetComment(TransMemoryDetails tmDetail) {
        return commentPrefix + " project: " +
                tmDetail.getProjectName() + ", version: " +
                tmDetail.getIterationName() + ", DocId: " +
                tmDetail.getDocId();
    }

    private static String buildTargetComment(TransMemoryUnit tu) {
        return commentPrefix + " translation memory: " +
                tu.getTranslationMemory().getSlug() +
                ", unique id: " + tu.getUniqueId();
    }

    private static class NeededEntities {
        private HProjectIteration targetVersion;
        private HLocale targetLocale;
        private List<HLocale> localesInTargetVersion;
        private long activeTextFlows;

        boolean isTargetLocaleEnabledInVersion() {
            return localesInTargetVersion.contains(targetLocale);
        }
    }
}
