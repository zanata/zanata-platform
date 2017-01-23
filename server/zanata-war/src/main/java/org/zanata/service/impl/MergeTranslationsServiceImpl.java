/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LocaleService;
import org.zanata.service.MergeTranslationsService;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.VersionStateCache;
import org.zanata.util.TranslationUtil;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import static org.zanata.events.TextFlowTargetStateEvent.TextFlowTargetStateChange;
import static org.zanata.transaction.TransactionUtilImpl.runInTransaction;
// Not @Transactional, because we use runInTransaction

/**
 * Service provider for merge translations task.
 *
 * @see org.zanata.action.MergeTranslationsManager
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("mergeTranslationsServiceImpl")
@RequestScoped
public class MergeTranslationsServiceImpl implements MergeTranslationsService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(MergeTranslationsServiceImpl.class);

    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private VersionStateCache versionStateCacheImpl;
    @Inject
    private TranslationStateCache translationStateCacheImpl;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private Event<TextFlowTargetStateEvent> textFlowTargetStateEvent;
    @Inject
    private Event<DocStatsEvent> docStatsEvent;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    /**
     * Batch size for find matching HTextFlow to process merging of
     * translations. Each TextFlow may lead to changes in multiple
     * TextFlowTargets (up to one per locale)
     *
     * This will determine how many DocStatsEvent will be trigger as part of
     * webhook event. The larger the number, the less DocStatsEvent will be
     * triggered as it aggregates related translated states.
     */
    private static final int TEXTFLOWS_PER_BATCH = 20;

    @Override
    @Async
    public Future<Void> startMergeTranslations(String sourceProjectSlug,
            String sourceVersionSlug, String targetProjectSlug,
            String targetVersionSlug, boolean useNewerTranslation,
            MergeTranslationsTaskHandle handle) {
        HProjectIteration sourceVersion = projectIterationDAO
                .getBySlug(sourceProjectSlug, sourceVersionSlug);
        if (sourceVersion == null) {
            log.error("Cannot find source version of {}:{}", sourceProjectSlug,
                    sourceVersionSlug);
            return AsyncTaskResult.taskResult();
        }
        HProjectIteration targetVersion = projectIterationDAO
                .getBySlug(targetProjectSlug, targetVersionSlug);
        if (targetVersion == null) {
            log.error("Cannot find target version of {}:{}", targetProjectSlug,
                    targetVersionSlug);
            return AsyncTaskResult.taskResult();
        }
        if (isVersionsEmpty(sourceVersion, targetVersion)) {
            return AsyncTaskResult.taskResult();
        }
        if (getSupportedLocales(targetProjectSlug, targetVersionSlug)
                .isEmpty()) {
            log.error("No locales enabled in target version of {} [{}]",
                    targetProjectSlug, targetVersionSlug);
            return AsyncTaskResult.taskResult();
        }
        Optional<MergeTranslationsTaskHandle> taskHandleOpt =
                Optional.fromNullable(handle);
        if (taskHandleOpt.isPresent()) {
            prepareMergeTranslationsHandle(sourceVersion, targetVersion,
                    taskHandleOpt.get());
        }
        Stopwatch overallStopwatch = Stopwatch.createStarted();
        log.info("merge translations start: from {} to {}",
                sourceProjectSlug + ":" + sourceVersionSlug,
                targetProjectSlug + ":" + targetVersionSlug);
        int startCount = 0;
        int totalCount = getTotalMatchCount(sourceVersion.getId(),
                targetVersion.getId());
        List<HLocale> supportedLocales = getSupportedLocales(
                targetVersion.getProject().getSlug(), targetVersion.getSlug());
        while (startCount < totalCount) {
            int processedCount = mergeTranslationBatch(sourceVersion,
                    targetVersion, supportedLocales, useNewerTranslation,
                    startCount, TEXTFLOWS_PER_BATCH);
            if (taskHandleOpt.isPresent()) {
                taskHandleOpt.get().increaseProgress(processedCount);
            }
            startCount += TEXTFLOWS_PER_BATCH;
            textFlowDAO.clear();
        }
        versionStateCacheImpl.clearVersionStatsCache(targetVersion.getId());
        log.info("merge translation end: from {} to {}, {}",
                sourceProjectSlug + ":" + sourceVersionSlug,
                targetProjectSlug + ":" + targetVersionSlug, overallStopwatch);
        return AsyncTaskResult.taskResult();
    }

    protected int mergeTranslationBatch(HProjectIteration sourceVersion,
            HProjectIteration targetVersion, List<HLocale> supportedLocales,
            boolean useNewerTranslation, int offset, int batchSize) {
        try {
            return runInTransaction(() -> this.mergeTranslations(
                    sourceVersion.getId(), targetVersion.getId(), offset,
                    batchSize, useNewerTranslation, supportedLocales));
        } catch (Exception e) {
            log.warn("exception during copy text flow target", e);
            return 0;
        }
    }

    private Integer mergeTranslations(final Long sourceVersionId,
            final Long targetVersionId, final int batchStart,
            final int batchLength, final boolean useNewerTranslation,
            final List<HLocale> supportedLocales) throws Exception {
        final Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        List<HTextFlow[]> matches = textFlowDAO.getSourceByMatchedContext(
                sourceVersionId, targetVersionId, batchStart, batchLength);
        Multimap<DocumentLocaleKey, TextFlowTargetStateChange> eventMap =
                HashMultimap.create();
        Map<DocumentLocaleKey, Map<ContentState, Long>> docStatsMap =
                Maps.newHashMap();
        Map<DocumentLocaleKey, Long> lastUpdatedTargetId = Maps.newHashMap();
        ;
        for (HTextFlow[] results : matches) {
            HTextFlow sourceTf = results[0];
            HTextFlow targetTf = results[1];
            boolean foundChange = false;
            Map<Long, ContentState> localeContentStateMap = Maps.newHashMap();
            for (HLocale hLocale : supportedLocales) {
                HTextFlowTarget sourceTft =
                        sourceTf.getTargets().get(hLocale.getId());
                // only process translated state
                if (sourceTft == null || !sourceTft.getState().isTranslated()) {
                    continue;
                }
                HTextFlowTarget targetTft =
                        targetTf.getTargets().get(hLocale.getId());
                if (targetTft == null) {
                    targetTft = new HTextFlowTarget(targetTf, hLocale);
                    targetTft.setVersionNum(0);
                    targetTf.getTargets().put(hLocale.getId(), targetTft);
                }
                if (MergeTranslationsServiceImpl.shouldMerge(sourceTft,
                        targetTft, useNewerTranslation)) {
                    foundChange = true;
                    ContentState oldState = targetTft.getState();
                    localeContentStateMap.put(hLocale.getId(), oldState);
                    mergeTextFlowTarget(sourceTft, targetTft);
                }
            }
            if (foundChange) {
                translationStateCacheImpl.clearDocumentStatistics(
                        targetTf.getDocument().getId());
                textFlowDAO.makePersistent(targetTf);
                textFlowDAO.flush();
                for (Map.Entry<Long, ContentState> entry : localeContentStateMap
                        .entrySet()) {
                    HTextFlowTarget updatedTarget =
                            targetTf.getTargets().get(entry.getKey());
                    DocumentLocaleKey key = new DocumentLocaleKey(
                            targetTf.getDocument().getId(),
                            updatedTarget.getLocale().getLocaleId());
                    eventMap.put(key,
                            new TextFlowTargetStateEvent.TextFlowTargetStateChange(
                                    targetTf.getId(), updatedTarget.getId(),
                                    updatedTarget.getState(),
                                    entry.getValue()));
                    lastUpdatedTargetId.put(key, updatedTarget.getId());
                    Map<ContentState, Long> contentStateDeltas =
                            docStatsMap.get(key) == null ? Maps.newHashMap()
                                    : docStatsMap.get(key);
                    DocStatsEvent.updateContentStateDeltas(contentStateDeltas,
                            updatedTarget.getState(), entry.getValue(),
                            targetTf.getWordCount());
                    docStatsMap.put(key, contentStateDeltas);
                }
            }
        }
        Long actorId = authenticatedAccount.getPerson().getId();
        for (Map.Entry<DocumentLocaleKey, Collection<TextFlowTargetStateChange>> entry : eventMap
                .asMap().entrySet()) {
            TextFlowTargetStateEvent tftUpdatedEvent =
                    new TextFlowTargetStateEvent(entry.getKey(),
                            targetVersionId, actorId,
                            ImmutableList.copyOf(entry.getValue()));
            textFlowTargetStateEvent.fire(tftUpdatedEvent);
        }
        for (Map.Entry<DocumentLocaleKey, Map<ContentState, Long>> entry : docStatsMap
                .entrySet()) {
            DocStatsEvent docEvent = new DocStatsEvent(entry.getKey(),
                    targetVersionId, entry.getValue(),
                    lastUpdatedTargetId.get(entry.getKey()));
            docStatsEvent.fire(docEvent);
        }
        stopwatch.stop();
        log.info("Complete merge translations of {} in {}",
                matches.size() * supportedLocales.size(), stopwatch);
        return matches.size() * supportedLocales.size();
    }

    private void mergeTextFlowTarget(HTextFlowTarget sourceTft,
            HTextFlowTarget targetTft) {
        targetTft.setContents(sourceTft.getContents());
        targetTft.setState(sourceTft.getState());
        targetTft.setLastChanged(sourceTft.getLastChanged());
        targetTft.setLastModifiedBy(sourceTft.getLastModifiedBy());
        targetTft.setTranslator(sourceTft.getTranslator());
        if (sourceTft.getComment() == null) {
            targetTft.setComment(null);
        } else {
            HSimpleComment hComment = targetTft.getComment();
            if (hComment == null) {
                hComment = new HSimpleComment();
                targetTft.setComment(hComment);
            }
            hComment.setComment(sourceTft.getComment().getComment());
        }
        targetTft.setRevisionComment(
                TranslationUtil.getMergeTranslationMessage(sourceTft));
        targetTft.setSourceType(TranslationSourceType.MERGE_VERSION);
        TranslationUtil.copyEntity(sourceTft, targetTft);
    }

    /**
     * Check if sourceVersion or targetVersion has source document.
     *
     * @param sourceVersion
     * @param targetVersion
     */
    private boolean isVersionsEmpty(HProjectIteration sourceVersion,
            HProjectIteration targetVersion) {
        if (sourceVersion.getDocuments().isEmpty()) {
            log.error("No documents in source version {}:{}",
                    sourceVersion.getProject().getSlug(),
                    sourceVersion.getSlug());
            return true;
        }
        if (targetVersion.getDocuments().isEmpty()) {
            log.error("No documents in target version {}:{}",
                    targetVersion.getProject().getSlug(),
                    targetVersion.getSlug());
            return true;
        }
        return false;
    }

    private void prepareMergeTranslationsHandle(
            @Nonnull HProjectIteration sourceVersion,
            @Nonnull HProjectIteration targetVersion,
            @Nonnull MergeTranslationsTaskHandle handle) {
        handle.setTriggeredBy(identity.getAccountUsername());
        int total = getTotalProgressCount(sourceVersion, targetVersion);
        handle.setMaxProgress(total);
        handle.setTotalTranslations(total);
    }

    @Override
    public int getTotalProgressCount(HProjectIteration sourceVersion,
            HProjectIteration targetVersion) {
        int matchCount = getTotalMatchCount(sourceVersion.getId(),
                targetVersion.getId());
        List<HLocale> locales = getSupportedLocales(
                targetVersion.getProject().getSlug(), targetVersion.getSlug());
        return matchCount * locales.size();
    }

    private int getTotalMatchCount(Long sourceVersionId, Long targetVersionId) {
        return textFlowDAO.getSourceByMatchedContextCount(sourceVersionId,
                targetVersionId);
    }

    public List<HLocale> getSupportedLocales(String projectSlug,
            String versionSlug) {
        return localeServiceImpl.getSupportedLanguageByProjectIteration(
                projectSlug, versionSlug);
    }
    // @formatter:off
    // @formatter:on

    /**
     * Rule of which translation should merge | from | to | copy? |
     * |-----------------------|------------------|-----------|
     * |fuzzy/untranslated | any | no |
     * |-----------------------|------------------|-----------| |different
     * source text/ | | | |docId/locale/resId | any | no |
     * |-----------------------|------------------|-----------|
     * |translated/approved | untranslated | yes |
     * |-----------------------|------------------|-----------|
     * |translated/approved | fuzzy | yes |
     * |-----------------------|------------------|-----------|
     * |translated/approved | same as from | copy if from is newer and option
     * says to copy
     *
     * @param sourceTft
     *            - matched documentId, source text, translated/approved
     *            HTextFlowTarget.
     * @see org.zanata.dao.TextFlowDAO#getSourceByMatchedContext
     * @param targetTft
     *            - HTextFlowTarget from target version
     */
    public static boolean shouldMerge(HTextFlowTarget sourceTft,
            HTextFlowTarget targetTft, boolean useNewerTranslation) {
        // should NOT merge is source tft is not translated/approved
        if (!sourceTft.getState().isTranslated()) {
            return false;
        }
        // should merge if target is not in translated/approved state
        if (!targetTft.getState().isTranslated()) {
            return true;
        }
        // should NOT merge if both state and contents are the same
        if (sourceTft.getState().equals(targetTft.getState())
                && sourceTft.getContents().equals(targetTft.getContents())) {
            return false;
        }
        // if both in translated state return latest if enabled
        return useNewerTranslation
                && sourceTft.getLastChanged().after(targetTft.getLastChanged());
    }
}
