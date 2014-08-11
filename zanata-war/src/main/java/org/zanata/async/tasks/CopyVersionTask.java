package org.zanata.async.tasks;

import javax.annotation.Nonnull;

import org.zanata.async.AsyncTask;
import org.zanata.async.TimedAsyncHandle;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyVersionService;
import org.zanata.service.impl.CopyVersionServiceImpl;
import org.zanata.util.ServiceLocator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

public class CopyVersionTask implements
        AsyncTask<Void, CopyVersionTask.CopyVersionTaskHandle> {

    private final CopyVersionTaskHandle handle;

    private final String projectSlug;
    private final String versionSlug;
    private final String newVersionSlug;

    public CopyVersionTask(String projectSlug, String versionSlug,
            String newVersionSlug) {
        handle =
                new CopyVersionTaskHandle("VersionCopyTask:" + versionSlug
                        + ":" + newVersionSlug);
        this.projectSlug = projectSlug;
        this.versionSlug = versionSlug;
        this.newVersionSlug = newVersionSlug;
    }

    /**
     * @return The maximum progress for the copy version task. (total document
     *         to be copied)
     */
    protected int getMaxProgress() {
        CopyVersionService copyVersionServiceImpl =
                ServiceLocator.instance().getInstance(
                        CopyVersionServiceImpl.class);

        return copyVersionServiceImpl.getTotalDocCount(
                projectSlug, versionSlug);
    }

    protected void runCopyVersion() {
        CopyVersionService copyVersionServiceImpl =
                ServiceLocator.instance().getInstance(
                        CopyVersionServiceImpl.class);
        copyVersionServiceImpl.copyVersion(projectSlug, versionSlug,
                newVersionSlug);
    }

    @Override
    public Void call() throws Exception {
        getHandle().startTiming();
        getHandle()
                .setTriggeredBy(ZanataIdentity.instance().getAccountUsername());
        getHandle().setMaxProgress(getMaxProgress());
        getHandle().setTotalDoc(getMaxProgress());

        runCopyVersion();

        getHandle().finishTiming();
        return null;
    }

    @Nonnull
    @Override
    public CopyVersionTaskHandle getHandle() {
        return handle;
    }

    @Getter
    @Setter
    public static class CopyVersionTaskHandle extends TimedAsyncHandle<Void> {
        private int documentCopied;
        private int totalDoc;
        private String cancelledBy;
        private long cancelledTime;
        private String triggeredBy;

        public CopyVersionTaskHandle(String taskName) {
            super(taskName);
        }

        /**
         * Increments the processed task by 1
         */
        public void incrementDocumentProcessed() {
            documentCopied++;
        }

    }
}
