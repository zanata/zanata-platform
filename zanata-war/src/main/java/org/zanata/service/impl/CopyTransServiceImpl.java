/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.ContainsAsyncMethods;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationStateCache;
import org.zanata.util.ServiceLocator;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Named("copyTransServiceImpl")
@javax.enterprise.context.Dependent
@Slf4j
@ContainsAsyncMethods
@AllArgsConstructor
@NoArgsConstructor
public class CopyTransServiceImpl implements CopyTransService {

    private static final int COPY_TRANS_BATCH_SIZE = 20;

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private CopyTransWorkFactory copyTransWorkFactory;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private TranslationStateCache translationStateCacheImpl;
    @Inject
    private TextFlowDAO textFlowDAO;

    /**
     * Copies previous matching translations for the given locale into a
     * document. Translations are matching if their document id, textflow id and
     * source content are identical, and their state is approved.
     *
     * The text flow revision for copied targets is set to the current text flow
     * revision.
     *
     * @param document
     *            the document to copy translations into
     * @param targetLocale
     *            the locale of translations to copy
     */
    private void copyTransForDocumentLocale(HDocument document,
            final HLocale targetLocale, final HCopyTransOptions options,
            Optional<CopyTransTaskHandle> taskHandleOpt) {

        int numCopied = 0;
        int start = 0;

        // need to reload HDocument because of different hibernate session
        document = documentDAO.findById(document.getId());

        // heuristic optimization
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean hasTranslationToCopy = true;
        if (options.getDocIdMismatchAction() ==
                HCopyTransOptions.ConditionRuleAction.REJECT) {
            long translationCandidate =
                    textFlowTargetDAO
                            .getTranslationCandidateCountWithDocIdAndLocale(
                                    document, targetLocale);
            hasTranslationToCopy = (translationCandidate != 0);
        }
        if (hasTranslationToCopy && options.getProjectMismatchAction() ==
                HCopyTransOptions.ConditionRuleAction.REJECT) {
            long translationCandidate =
                    textFlowTargetDAO
                            .getTranslationCandidateCountWithProjectAndLocale(
                                    document,
                                    targetLocale);
            hasTranslationToCopy = (translationCandidate != 0);
        }
        // TODO if the translation candidate number is small (compare to large
        // number of copy targets), it's better to inverse the process, i.e.
        // iterate through candidates and inject matches

        if (hasTranslationToCopy) {
            boolean requireTranslationReview =
                    document.getProjectIteration().getRequireTranslationReview();

            while (start < document.getTextFlows().size()) {
                numCopied +=
                        copyTransForBatch(document, start, COPY_TRANS_BATCH_SIZE,
                                targetLocale, options, taskHandleOpt,
                                requireTranslationReview);
                start += COPY_TRANS_BATCH_SIZE;
                documentDAO.clear();
            }
        } else if (taskHandleOpt.isPresent()) {
            int totalActiveTextFlows =
                    ServiceLocator.instance().getInstance(TextFlowDAO.class)
                            .countActiveTextFlowsInDocument(
                                    document.getId());

            taskHandleOpt.get().increaseProgress(totalActiveTextFlows);
        }

        // If there have been any changes to the document stats, reset them
        if (numCopied > 0) {
            translationStateCacheImpl.clearDocumentStatistics(document.getId(),
                    targetLocale.getLocaleId());
        }

        stopwatch.stop();
        log.info(
                "copyTrans: {} {} translations for document \"{}{}\" - duration: {}",
                numCopied, targetLocale.getLocaleId(), document.getPath(),
                document.getName(), stopwatch);
    }

    /**
     * Perform copy trans on a batch of text flows for a document.
     *
     * @param requireTranslationReview
     *            whether the project iteration requires translation review
     * @param batchStart
     *            USE_HIBERNATE_SEARCH The text flow position to start copying.
     * @param batchLength
     *            The number of text flows on which to perform copy trans,
     *            starting from batchStart.
     * @return The number of actual copied translations for the segment.
     */
    private int copyTransForBatch(HDocument document, final int batchStart,
            final int batchLength, final HLocale targetLocale,
            final HCopyTransOptions options,
            Optional<CopyTransTaskHandle> taskHandleOpt,
            boolean requireTranslationReview) {

        try {
            HDocument hDocument = documentDAO.findById(document.getId());
            List<HTextFlow> docTextFlows = hDocument.getTextFlows();
            int batchEnd =
                    Math.min(batchStart + batchLength, docTextFlows.size());
            int batchSize = batchEnd - batchStart;
            List<HTextFlow> copyTargets =
                    docTextFlows.subList(batchStart, batchEnd);
            Integer numCopied =
                    copyTransWorkFactory.createCopyTransWork(targetLocale,
                            options, document, requireTranslationReview,
                            copyTargets)
                            .workInTransaction();
            if (taskHandleOpt.isPresent()) {
                taskHandleOpt.get().increaseProgress(batchSize);
            }
            return numCopied;
        } catch (Exception e) {
            log.warn("exception during copy trans", e);
            return 0;
        }
    }

    @Override
    public void copyTransForDocument(HDocument document, CopyTransTaskHandle handle) {
        copyTransForDocument(document, null, handle);
    }

    @Override
    public void copyTransForDocument(HDocument document,
            HCopyTransOptions copyTransOpts, CopyTransTaskHandle handle) {

        Optional<CopyTransTaskHandle> taskHandleOpt =
                Optional.fromNullable(handle);
        if (taskHandleOpt.isPresent()) {
            prepareCopyTransHandle(document, taskHandleOpt.get());
        }

        // use project level options
        if (copyTransOpts == null) {
            // NB: Need to reload the options from the db
            copyTransOpts =
                    projectDAO
                            .findById(
                                    document.getProjectIteration().getProject()
                                            .getId(), false)
                            .getDefaultCopyTransOpts();
        }
        // use the global default options
        if (copyTransOpts == null) {
            copyTransOpts = new HCopyTransOptions();
        }

        log.info("copyTrans start: document \"{}\"", document.getDocId());
        List<HLocale> localeList =
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                        document.getProjectIteration().getProject().getSlug(),
                        document.getProjectIteration().getSlug());

        for (HLocale targetLocale : localeList) {
            if (taskHandleOpt.isPresent() && taskHandleOpt.get().isCancelled()) {
                return;
            }
            copyTransForDocumentLocale(document, targetLocale, copyTransOpts,
                    taskHandleOpt);
        }
        log.info("copyTrans finished: document \"{}\"", document.getDocId());
    }

    @Override
    @Async
    public Future<Void> startCopyTransForDocument(HDocument document,
            HCopyTransOptions copyTransOptions, CopyTransTaskHandle handle) {
        copyTransForDocument(document, copyTransOptions, handle);
        return AsyncTaskResult.taskResult();
    }

    @Override
    @Async
    public Future<Void> startCopyTransForIteration(HProjectIteration iteration,
            HCopyTransOptions copyTransOptions, CopyTransTaskHandle handle) {
        copyTransForIteration(iteration, copyTransOptions, handle);
        return AsyncTaskResult.taskResult();
    }

    @Override
    public void copyTransForIteration(HProjectIteration iteration,
            HCopyTransOptions copyTransOptions,
            @NotNull CopyTransTaskHandle handle) {
        Optional<CopyTransTaskHandle> taskHandleOpt =
                Optional.fromNullable(handle);

        if (taskHandleOpt.isPresent()) {
            prepareCopyTransHandle(iteration, taskHandleOpt.get());
        }

        for (HDocument doc : iteration.getDocuments().values()) {
            if (taskHandleOpt.isPresent() && taskHandleOpt.get().isCancelled()) {
                return;
            }
            // if options are to reject translation from other projects and there is
            // only one version in current project, don't even bother running
            // copyTrans
            if (copyTransOptions.getProjectMismatchAction() == HCopyTransOptions.ConditionRuleAction.REJECT) {
                int copyCandidates = projectDAO.getTranslationCandidateCount(
                        iteration.getProject().getId());
                if (copyCandidates < 2) {
                    if (taskHandleOpt.isPresent()) {
                        taskHandleOpt.get().increaseProgress(
                                taskHandleOpt.get().getMaxProgress());
                    }
                    return;
                }
            }
            this.copyTransForDocument(doc, copyTransOptions, handle);
        }
    }

    private void prepareCopyTransHandle(HProjectIteration iteration, CopyTransTaskHandle handle) {
        if (!handle.isPrepared()) {
            // TODO Progress should be handle as long
            handle.setMaxProgress((int) getMaxProgress(iteration));
            handle.setPrepared();
        }
    }

    private void prepareCopyTransHandle(HDocument document, CopyTransTaskHandle handle) {
        if (!handle.isPrepared()) {
            // TODO Progress should be handle as long
            handle.setMaxProgress((int) getMaxProgress(document));
            handle.setPrepared();
        }
    }

    private long getMaxProgress(HProjectIteration iteration) {
        log.debug("counting locales");
        List<HLocale> localeList =
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                        iteration.getProject().getSlug(),
                        iteration.getSlug());
        int localeCount = localeList.size();
        log.debug("counting locales finished");
        log.debug("counting textflows");
        long textFlowCount = textFlowDAO.countActiveTextFlowsInProjectIteration(
                iteration.getId());
        log.debug("counting textflows finished");
        return localeCount * textFlowCount;
    }

    private long getMaxProgress(HDocument document) {
        List<HLocale> localeList =
                localeServiceImpl.getSupportedLanguageByProjectIteration(document
                        .getProjectIteration().getProject().getSlug(), document
                        .getProjectIteration().getSlug());
        int localeCount = localeList.size();

        int textFlowCount =
                textFlowDAO.countActiveTextFlowsInDocument(document.getId());
        return localeCount * textFlowCount;
    }

}
