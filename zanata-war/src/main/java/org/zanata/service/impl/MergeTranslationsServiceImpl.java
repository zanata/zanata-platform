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

import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.ContainsAsyncMethods;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.MergeTranslationsService;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.VersionStateCache;

/**
 * Service provider for merge translations task.
 *
 * @see org.zanata.action.MergeTranslationsManager
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("mergeTranslationsServiceImpl")
@javax.enterprise.context.Dependent
@Slf4j
@ContainsAsyncMethods
public class MergeTranslationsServiceImpl implements MergeTranslationsService {

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

    private final static int TRANSLATION_BATCH_SIZE = 10;

    @Override
    @Async
    public Future<Void> startMergeTranslations(String sourceProjectSlug,
            String sourceVersionSlug, String targetProjectSlug,
            String targetVersionSlug, boolean useNewerTranslation,
            MergeTranslationsTaskHandle handle) {

        HProjectIteration sourceVersion =
                projectIterationDAO.getBySlug(sourceProjectSlug,
                        sourceVersionSlug);

        if (sourceVersion == null) {
            log.error("Cannot find source version of {}:{}", sourceProjectSlug,
                    sourceVersionSlug);
            return AsyncTaskResult.taskResult();
        }

        HProjectIteration targetVersion =
                projectIterationDAO.getBySlug(targetProjectSlug,
                        targetVersionSlug);

        if (targetVersion == null) {
            log.error("Cannot find target version of {}:{}", targetProjectSlug,
                    targetVersionSlug);
            return AsyncTaskResult.taskResult();
        }

        if (isVersionsEmpty(sourceVersion, targetVersion)) {
            return AsyncTaskResult.taskResult();
        }

        if (getSupportedLocales(targetProjectSlug, targetVersionSlug).isEmpty()) {
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
        log.info("merge translations start: from {} to {}", sourceProjectSlug
                + ":" + sourceVersionSlug, targetProjectSlug + ":"
                + targetVersionSlug);

        int startCount = 0;
        int totalCount = getTotalMatchCount(sourceVersion.getId(),
                targetVersion.getId());

        List<HLocale> supportedLocales = getSupportedLocales(targetVersion
                .getProject().getSlug(), targetVersion.getSlug());

        while (startCount < totalCount) {
            int processedCount =
                    mergeTranslationBatch(sourceVersion, targetVersion,
                            supportedLocales, useNewerTranslation, startCount,
                            TRANSLATION_BATCH_SIZE);
            if (taskHandleOpt.isPresent()) {
                taskHandleOpt.get().increaseProgress(processedCount);
            }

            startCount += TRANSLATION_BATCH_SIZE;
            textFlowDAO.clear();
        }
        versionStateCacheImpl.clearVersionStatsCache(targetVersion.getId());
        log.info("merge translation end: from {} to {}, {}", sourceProjectSlug
                + ":" + sourceVersionSlug, targetProjectSlug + ":"
                + targetVersionSlug, overallStopwatch);

        return AsyncTaskResult.taskResult();
    }

    protected int mergeTranslationBatch(HProjectIteration sourceVersion,
            HProjectIteration targetVersion, List<HLocale> supportedLocales,
            boolean useNewerTranslation, int offset, int batchSize) {
        try {
            return new MergeTranslationsWork(sourceVersion.getId(),
                    targetVersion.getId(), offset, batchSize,
                    useNewerTranslation, supportedLocales,
                    textFlowDAO, translationStateCacheImpl)
                    .workInTransaction();
        } catch (Exception e) {
            log.warn("exception during copy text flow target", e);
            return 0;
        }
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
            log.error("No documents in source version {}:{}", sourceVersion
                    .getProject().getSlug(), sourceVersion.getSlug());
            return true;
        }
        if (targetVersion.getDocuments().isEmpty()) {
            log.error("No documents in target version {}:{}", targetVersion
                    .getProject().getSlug(), targetVersion.getSlug());
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

        List<HLocale> locales =
                getSupportedLocales(targetVersion.getProject().getSlug(),
                        targetVersion.getSlug());

        return matchCount * locales.size();
    }

    private int getTotalMatchCount(Long sourceVersionId, Long targetVersionId) {
        return textFlowDAO.getSourceByMatchedContextCount(
            sourceVersionId, targetVersionId);
    }

    public List<HLocale> getSupportedLocales(String projectSlug,
            String versionSlug) {
        return localeServiceImpl.getSupportedLanguageByProjectIteration(
                projectSlug, versionSlug);
    }

    // @formatter:off
    /**
     * Rule of which translation should merge
     * |          from         |       to         |   copy?   |
     * |-----------------------|------------------|-----------|
     * |fuzzy/untranslated     |       any        |     no    |
     * |-----------------------|------------------|-----------|
     * |different source text/ |                  |           |
     * |docId/locale/resId     |       any        |     no    |
     * |-----------------------|------------------|-----------|
     * |translated/approved    |   untranslated   |    yes    |
     * |-----------------------|------------------|-----------|
     * |translated/approved    |       fuzzy      |    yes    |
     * |-----------------------|------------------|-----------|
     * |translated/approved    |   same as from   | copy if from is newer
     *                                              and option says to copy
     *
     * @param sourceTft - matched documentId, source text,
     *                    translated/approved HTextFlowTarget.
     *     @see org.zanata.dao.TextFlowDAO#getSourceByMatchedContext
     * @param targetTft - HTextFlowTarget from target version
     */
    // @formatter:on
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
        return useNewerTranslation && sourceTft.getLastChanged().after(
                targetTft.getLastChanged());
    }
}
