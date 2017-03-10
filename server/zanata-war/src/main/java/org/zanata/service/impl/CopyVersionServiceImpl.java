/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.file.FilePersistService;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.po.HPoHeader;
import org.zanata.model.po.HPoTargetHeader;
import org.zanata.model.po.HPotEntryData;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyVersionService;
import org.zanata.service.VersionStateCache;
import org.zanata.transaction.TransactionUtil;
import org.zanata.util.JPACopier;
import org.zanata.util.TranslationUtil;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
// Not @Transactional, because we use runInTransaction

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("copyVersionServiceImpl")
@RequestScoped
public class CopyVersionServiceImpl implements CopyVersionService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CopyVersionServiceImpl.class);

    // Document batch size
    protected static final int DOC_BATCH_SIZE = 2;
    // TextFlow batch size
    protected static final int TF_BATCH_SIZE = 20;
    // TextFlowTarget batch size
    protected static final int TFT_BATCH_SIZE = 20;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private VersionStateCache versionStateCacheImpl;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private TransactionUtil transactionUtil;
    // Stop watch for textFlow and target copy process
    private Stopwatch copyTfAndTftStopWatch = Stopwatch.createUnstarted();

    @Override
    public void copyVersion(@Nonnull String projectSlug,
            @Nonnull String versionSlug, @Nonnull String newVersionSlug,
            @Nullable CopyVersionTaskHandle handle) {
        Optional<CopyVersionTaskHandle> taskHandleOpt =
                Optional.fromNullable(handle);
        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (version == null) {
            log.error("Cannot find project iteration of {}:{}", projectSlug,
                    versionSlug);
            return;
        }
        if (taskHandleOpt.isPresent()) {
            prepareCopyVersionHandle(version, taskHandleOpt.get());
        }
        Stopwatch overallStopwatch = Stopwatch.createStarted();
        log.info("copy version start: copy {} to {}",
                projectSlug + ":" + versionSlug,
                projectSlug + ":" + newVersionSlug);
        // Copy of HProjectIteration
        HProjectIteration newVersion = new HProjectIteration();
        try {
            newVersion.setSlug(newVersionSlug);
            newVersion.setStatus(EntityStatus.READONLY);
            newVersion.setProject(version.getProject());
            newVersion = copyVersionSettings(version, newVersion);
            newVersion = projectIterationDAO.makePersistent(newVersion);
            projectIterationDAO.flush();
            // Copy of HDocument
            int docSize =
                    documentDAO.getDocCountByVersion(projectSlug, versionSlug);
            int docStart = 0;
            while (docStart < docSize) {
                Map<Long, Long> docMap = copyDocumentBatch(version.getId(),
                        newVersion.getId(), docStart, DOC_BATCH_SIZE);
                docStart += DOC_BATCH_SIZE;
                for (Map.Entry<Long, Long> entry : docMap.entrySet()) {
                    // Copy of HTextFlow and HTextFlowTarget
                    copyTextFlowAndTarget(entry.getKey(), entry.getValue());
                    if (taskHandleOpt.isPresent()) {
                        taskHandleOpt.get().incrementDocumentProcessed();
                        taskHandleOpt.get().increaseProgress(1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during copy version from project \'{}\' {} to {}.",
                    projectSlug, versionSlug, newVersionSlug, e);
        } finally {
            // restore version.status after complete
            newVersion =
                    projectIterationDAO.getBySlug(projectSlug, newVersionSlug);
            newVersion.setStatus(version.getStatus());
            projectIterationDAO.makePersistent(newVersion);
            projectIterationDAO.flush();
            // clear any cache that has been loaded in this new version before
            // copy
            // completed
            versionStateCacheImpl.clearVersionStatsCache(newVersion.getId());
            log.info("copy version end: copy {} to {}, {}",
                    projectSlug + ":" + versionSlug,
                    projectSlug + ":" + newVersionSlug, overallStopwatch);
        }
    }

    @Override
    @Async
    public Future<Void> startCopyVersion(@Nonnull String projectSlug,
            @Nonnull String versionSlug, @Nonnull String newVersionSlug,
            CopyVersionTaskHandle handle) {
        copyVersion(projectSlug, versionSlug, newVersionSlug, handle);
        return AsyncTaskResult.taskResult();
    }

    private void prepareCopyVersionHandle(
            @Nonnull HProjectIteration originalVersion,
            @Nonnull CopyVersionTaskHandle handle) {
        handle.setTriggeredBy(identity.getAccountUsername());
        int totalDocCount =
                getTotalDocCount(originalVersion.getProject().getSlug(),
                        originalVersion.getSlug());
        handle.setMaxProgress(totalDocCount);
        handle.setTotalDoc(totalDocCount);
    }

    @Override
    public int getTotalDocCount(@Nonnull String projectSlug,
            @Nonnull String versionSlug) {
        return documentDAO.getDocCountByVersion(projectSlug, versionSlug);
    }

    /**
     * Return map of old HDocument id, new HDocument id copied
     *
     * @param versionId
     * @param newVersionId
     * @param batchStart
     * @param batchLength
     */
    protected Map<Long, Long> copyDocumentBatch(Long versionId,
            Long newVersionId, int batchStart, int batchLength) {
        try {
            return transactionUtil.call(() -> copyDocument(versionId,
                    newVersionId, batchStart, batchLength));
        } catch (Exception e) {
            log.warn("exception during copy document", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Copy documents from HProjectIteration(id=versionId) in
     * batches(batchStart, batchLength) into HProjectIteration(id=newVersionId).
     * Copying includes HRawDocument stored in file system.
     *
     * @return Map indexed by the original document id, and to the new copied
     *         document id.
     * @throws Exception
     */
    private Map<Long, Long> copyDocument(final Long versionId,
            final Long newVersionId, final int batchStart,
            final int batchLength) throws Exception {
        Map<Long, Long> docMap = Maps.newHashMap();
        HProjectIteration newVersion =
                projectIterationDAO.findById(newVersionId);
        List<HDocument> documents = documentDAO.findAllByVersionId(versionId,
                batchStart, batchLength);
        for (HDocument doc : documents) {
            HDocument newDocument = copyDocument(newVersion, doc);
            // Needs to persist before inserting raw document
            newDocument = documentDAO.makePersistent(newDocument);
            if (doc.getRawDocument() != null) {
                HRawDocument newRawDocument =
                        copyRawDocument(newDocument, doc.getRawDocument());
                filePersistService.copyAndPersistRawDocument(
                        doc.getRawDocument(), newRawDocument);
                documentDAO.addRawDocument(newDocument, newRawDocument);
            }
            newVersion.getDocuments().put(newDocument.getDocId(), newDocument);
            docMap.put(doc.getId(), newDocument.getId());
        }
        documentDAO.flush();
        return docMap;
    }

    /**
     * Copy text flows and targets of document with id=documentId to document
     * with id=newDocumentId
     *
     * @param documentId
     * @param newDocumentId
     */
    private void copyTextFlowAndTarget(Long documentId, Long newDocumentId) {
        copyTfAndTftStopWatch.start();
        int tfStart = 0;
        int tftStart = 0;
        int totalTftCount = 0;
        int tfCount = textFlowDAO.countActiveTextFlowsInDocument(documentId);
        while (tfStart < tfCount) {
            Map<Long, Long> tfMap = copyTextFlowBatch(documentId, newDocumentId,
                    tfStart, TF_BATCH_SIZE);
            tfStart += TF_BATCH_SIZE;
            textFlowDAO.clear();
            documentDAO.clear();
            for (Map.Entry<Long, Long> entry : tfMap.entrySet()) {
                tftStart = 0;
                int tftCount = textFlowTargetDAO
                        .countTextFlowTargetsInTextFlow(entry.getKey());
                while (tftStart < tftCount) {
                    totalTftCount += copyTextFlowTargetBatch(entry.getKey(),
                            entry.getValue(), tftStart, TFT_BATCH_SIZE);
                    tftStart += TFT_BATCH_SIZE;
                    textFlowDAO.clear();
                    textFlowTargetDAO.clear();
                }
            }
        }
        copyTfAndTftStopWatch.stop();
        log.info(
                "copy document- textFlow:{}, textFlowTarget:{} copied for document:{} - {}",
                tfCount, totalTftCount, newDocumentId, copyTfAndTftStopWatch);
        copyTfAndTftStopWatch.reset();
    }

    /**
     * Return map of old HTextFlow id, new HTextFlow id copied
     *
     * @param documentId
     * @param newDocumentId
     * @param batchStart
     * @param batchLength
     */
    protected Map<Long, Long> copyTextFlowBatch(Long documentId,
            Long newDocumentId, int batchStart, int batchLength) {
        try {
            return transactionUtil.call(() -> this.copyTextFlows(documentId,
                    newDocumentId, batchStart, batchLength));
        } catch (Exception e) {
            log.warn("exception during copy text flow", e);
            return Collections.EMPTY_MAP;
        }
    }

    private Map<Long, Long> copyTextFlows(final Long documentId,
            final Long newDocumentId, final int batchStart,
            final int batchLength) throws Exception {
        Map<Long, HTextFlow> tfMap = Maps.newHashMap();
        List<HTextFlow> textFlows = textFlowDAO
                .getTextFlowsByDocumentId(documentId, batchStart, batchLength);
        HDocument newDocument = documentDAO.getById(newDocumentId);
        for (HTextFlow textFlow : textFlows) {
            HTextFlow newTextFlow = copyTextFlow(newDocument, textFlow);
            newDocument.getTextFlows().add(newTextFlow);
            newDocument.getAllTextFlows().put(newTextFlow.getResId(),
                    newTextFlow);
            tfMap.put(textFlow.getId(), newTextFlow);
        }
        documentDAO.makePersistent(newDocument);
        documentDAO.flush();
        return Maps.transformEntries(tfMap, (key, value) -> value.getId());
    }

    /**
     * Return number of HTextFlowTarget copied
     *
     * @param tfId
     * @param newTfId
     * @param batchStart
     * @param batchLength
     */
    protected int copyTextFlowTargetBatch(Long tfId, Long newTfId,
            int batchStart, int batchLength) {
        try {
            return transactionUtil.call(() -> this.copyTextFlowTargets(tfId,
                    newTfId, batchStart, batchLength));
        } catch (Exception e) {
            log.warn("exception during copy text flow target", e);
            return 0;
        }
    }

    /**
     * Copy HTextFlowTarget from HTextFlow(id=tfId) in batches(batchStart,
     * batchLength) into HTextFlow(id=newTfId).
     *
     * @return Number of text flow targets copied.
     * @throws Exception
     */
    private Integer copyTextFlowTargets(final Long tfId, final Long newTfId,
            final int batchStart, final int batchLength) throws Exception {
        HTextFlow newTextFlow = textFlowDAO.findById(newTfId);
        List<HTextFlowTarget> copyTargets = textFlowTargetDAO
                .getByTextFlowId(tfId, batchStart, batchLength);
        for (HTextFlowTarget tft : copyTargets) {
            HTextFlowTarget newTextFlowTarget =
                    copyTextFlowTarget(newTextFlow, tft);
            newTextFlow.getTargets().put(newTextFlowTarget.getLocale().getId(),
                    newTextFlowTarget);
        }
        textFlowDAO.makePersistent(newTextFlow);
        textFlowDAO.flush();
        return copyTargets.size();
    }

    @Override
    public HProjectIteration copyVersionSettings(HProjectIteration version,
            HProjectIteration newVersion) {
        try {
            JPACopier.copyBean(version, newVersion, "slug", "status", "project",
                    "children", "documents", "allDocuments");
        } catch (Exception e) {
            log.warn("exception during copy version", e);
        }
        return newVersion;
    }

    @Override
    public HDocument copyDocument(HProjectIteration newVersion,
            HDocument document) throws Exception {
        HDocument copy = JPACopier.<HDocument> copyBean(document,
                "projectIteration", "poHeader", "poTargetHeaders",
                "rawDocument", "textFlows", "allTextFlows");
        copy.setProjectIteration(newVersion);
        if (document.getPoHeader() != null) {
            HPoHeader poHeader =
                    JPACopier.<HPoHeader> copyBean(document.getPoHeader());
            copy.setPoHeader(poHeader);
        }
        for (Map.Entry<HLocale, HPoTargetHeader> entry : document
                .getPoTargetHeaders().entrySet()) {
            HPoTargetHeader poTargetHeader = JPACopier
                    .<HPoTargetHeader> copyBean(entry.getValue(), "document");
            poTargetHeader.setDocument(copy);
            copy.getPoTargetHeaders().put(entry.getKey(), poTargetHeader);
        }
        return copy;
    }

    @Override
    public HRawDocument copyRawDocument(HDocument newDocument,
            @Nonnull HRawDocument rawDocument) throws Exception {
        HRawDocument copy =
                JPACopier.<HRawDocument> copyBean(rawDocument, "document");
        copy.setDocument(newDocument);
        return copy;
    }

    @Override
    public HTextFlow copyTextFlow(HDocument newDocument, HTextFlow textFlow)
            throws Exception {
        HTextFlow copy = JPACopier.<HTextFlow> copyBean(textFlow, "document",
                "content", "targets", "history", "potEntryData");
        copy.setDocument(newDocument);
        // copy PotEntryData
        if (textFlow.getPotEntryData() != null) {
            HPotEntryData potEntryData = JPACopier.<HPotEntryData> copyBean(
                    textFlow.getPotEntryData(), "textFlow");
            copy.setPotEntryData(potEntryData);
        }
        return copy;
    }

    @Override
    public HTextFlowTarget copyTextFlowTarget(HTextFlow newTf,
            HTextFlowTarget tft) throws Exception {
        HTextFlowTarget copy = JPACopier.<HTextFlowTarget> copyBean(tft,
                "textFlow", "reviewComments", "history");
        copy.setTextFlow(newTf);
        copy.setTextFlowRevision(newTf.getRevision());
        if (tft.getComment() != null) {
            copy.setComment(new HSimpleComment(tft.getComment().getComment()));
        }
        copy.setRevisionComment(TranslationUtil.getCopyVersionMessage(tft));
        copy.setSourceType(TranslationSourceType.COPY_VERSION);
        TranslationUtil.copyEntity(tft, copy);
        // copy review comment
        copy.setReviewComments(
                Lists.<HTextFlowTargetReviewComment> newArrayList());
        for (HTextFlowTargetReviewComment comment : tft.getReviewComments()) {
            copy.addReviewComment(comment.getComment(), comment.getCommenter());
        }
        // copy history
        for (HTextFlowTargetHistory history : tft.getHistory().values()) {
            HTextFlowTargetHistory newHistory =
                    JPACopier.<HTextFlowTargetHistory> copyBean(history,
                            "textFlowTarget", "content");
            newHistory.setTextFlowTarget(copy);
            newHistory.setSourceType(TranslationSourceType.COPY_VERSION);
            TranslationUtil.copyEntity(history, newHistory);
            copy.getHistory().put(newHistory.getVersionNum(), newHistory);
        }
        return copy;
    }
}
