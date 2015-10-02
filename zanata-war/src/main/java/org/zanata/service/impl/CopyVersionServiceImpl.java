package org.zanata.service.impl;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.ContainsAsyncMethods;
import org.zanata.async.handle.CopyVersionTaskHandle;
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
import org.zanata.util.JPACopier;
import org.zanata.util.TranslationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("copyVersionServiceImpl")
@javax.enterprise.context.Dependent
@Slf4j

@ContainsAsyncMethods
public class CopyVersionServiceImpl implements CopyVersionService {

    // Document batch size
    protected final static int DOC_BATCH_SIZE = 2;

    // TextFlow batch size
    protected final static int TF_BATCH_SIZE = 20;

    // TextFlowTarget batch size
    protected final static int TFT_BATCH_SIZE = 20;

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
                projectSlug + ":" + versionSlug, projectSlug + ":"
                        + newVersionSlug);

        // Copy of HProjectIteration
        HProjectIteration newVersion =
                projectIterationDAO.getBySlug(projectSlug, newVersionSlug);

        newVersion = copyVersionSettings(version, newVersion);
        newVersion = projectIterationDAO.makePersistent(newVersion);

        // Copy of HDocument
        int docSize =
                documentDAO.getDocCountByVersion(projectSlug, versionSlug);

        int docStart = 0;
        while (docStart < docSize) {
            Map<Long, Long> docMap =
                    copyDocumentBatch(version.getId(), newVersion.getId(),
                            docStart, DOC_BATCH_SIZE);
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

        // restore version.status after complete
        newVersion = projectIterationDAO.getBySlug(projectSlug, newVersionSlug);
        newVersion.setStatus(version.getStatus());
        projectIterationDAO.makePersistent(newVersion);
        projectIterationDAO.flush();

        // clear any cache that has been loaded in this new version before copy
        // completed
        versionStateCacheImpl.clearVersionStatsCache(newVersion.getId());
        log.info("copy version end: copy {} to {}, {}", projectSlug
                + ":" + versionSlug, projectSlug + ":" + newVersionSlug,
                overallStopwatch);
    }

    @Override
    @Async
    public Future<Void> startCopyVersion(@Nonnull String projectSlug,
            @Nonnull String versionSlug,
            @Nonnull String newVersionSlug, CopyVersionTaskHandle handle) {
        copyVersion(projectSlug, versionSlug, newVersionSlug, handle);
        return AsyncTaskResult.taskResult();
    }

    private void prepareCopyVersionHandle(@Nonnull HProjectIteration originalVersion,
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
            return new CopyDocumentWork(versionId, newVersionId, documentDAO,
                    projectIterationDAO, filePersistService, this,
                    batchStart, batchLength).workInTransaction();
        } catch (Exception e) {
            log.warn("exception during copy document", e);
            return Collections.emptyMap();
        }
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
        int tfStart = 0, tftStart = 0, totalTftCount = 0;
        int tfCount = textFlowDAO.countActiveTextFlowsInDocument(documentId);

        while (tfStart < tfCount) {
            Map<Long, Long> tfMap =
                    copyTextFlowBatch(documentId, newDocumentId, tfStart,
                            TF_BATCH_SIZE);
            tfStart += TF_BATCH_SIZE;
            textFlowDAO.clear();
            documentDAO.clear();

            for (Map.Entry<Long, Long> entry : tfMap.entrySet()) {
                tftStart = 0;
                int tftCount =
                        textFlowTargetDAO.countTextFlowTargetsInTextFlow(
                                entry.getKey());

                while (tftStart < tftCount) {
                    totalTftCount +=
                            copyTextFlowTargetBatch(entry.getKey(),
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
            return new CopyTextFlowWork(documentId, newDocumentId, textFlowDAO,
                    documentDAO, this, batchStart, batchLength)
                    .workInTransaction();
        } catch (Exception e) {
            log.warn("exception during copy text flow", e);
            return Collections.EMPTY_MAP;
        }
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
            return new CopyTextFlowTargetWork(tfId, newTfId, textFlowTargetDAO,
                    textFlowDAO, this, batchStart, batchLength)
                    .workInTransaction();
        } catch (Exception e) {
            log.warn("exception during copy text flow target", e);
            return 0;
        }
    }

    @Override
    public HProjectIteration copyVersionSettings(
            HProjectIteration version, HProjectIteration newVersion) {
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
        HDocument copy =
                JPACopier.<HDocument> copyBean(document, "projectIteration",
                        "poHeader", "poTargetHeaders", "rawDocument",
                        "textFlows", "allTextFlows");
        copy.setProjectIteration(newVersion);

        if (document.getPoHeader() != null) {
            HPoHeader poHeader =
                    JPACopier.<HPoHeader> copyBean(document.getPoHeader());
            copy.setPoHeader(poHeader);
        }

        for (Map.Entry<HLocale, HPoTargetHeader> entry : document
                .getPoTargetHeaders().entrySet()) {
            HPoTargetHeader poTargetHeader =
                    JPACopier.<HPoTargetHeader> copyBean(entry.getValue(),
                            "document");
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
        HTextFlow copy =
                JPACopier.<HTextFlow> copyBean(textFlow, "document", "content",
                        "targets", "history", "potEntryData");
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
        HTextFlowTarget copy =
                JPACopier.<HTextFlowTarget> copyBean(tft, "textFlow",
                        "reviewComments", "history");
        copy.setTextFlow(newTf);
        copy.setTextFlowRevision(newTf.getRevision());
        if(tft.getComment() != null) {
            copy.setComment(new HSimpleComment(tft.getComment().getComment()));
        }
        copy.setRevisionComment(TranslationUtil.getCopyVersionMessage(tft));
        copy.setSourceType(TranslationSourceType.COPY_VERSION);
        TranslationUtil.copyEntity(tft, copy);

        // copy review comment
        copy.setReviewComments(Lists
                .<HTextFlowTargetReviewComment> newArrayList());
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
