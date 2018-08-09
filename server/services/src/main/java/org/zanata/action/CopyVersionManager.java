package org.zanata.action;

import static org.zanata.async.AsyncTaskKey.joinFields;

import java.io.Serializable;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.AsyncTaskKey;
import org.zanata.async.GenericAsyncTaskKey;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.i18n.Messages;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyVersionService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Manages copy version tasks.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Dependent
public class CopyVersionManager implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CopyVersionManager.class);

    private static final long serialVersionUID = 3414395834255069870L;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    private CopyVersionService copyVersionServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private Messages messages;

    /**
     * Copy existing version to new version
     *
     * @param projectSlug
     *            - existing project identifier
     * @param versionSlug
     *            - existing version identifier
     * @param newVersionSlug
     *            - new version identifier
     */
    public void startCopyVersion(String projectSlug, String versionSlug,
            String newVersionSlug) {
        AsyncTaskKey key = CopyVersionKey.getKey(projectSlug, newVersionSlug);
        CopyVersionTaskHandle handle = new CopyVersionTaskHandle();
        handle.setTaskName(
                messages.format("jsf.tasks.copyVersion", projectSlug,
                        versionSlug, newVersionSlug));
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        copyVersionServiceImpl.startCopyVersion(projectSlug, versionSlug,
                newVersionSlug, handle);
    }

    /**
     * Cancel running copy version task
     *
     * @param projectSlug
     *            - target project identifier
     * @param versionSlug
     *            - target version identifier
     */
    public void cancelCopyVersion(String projectSlug, String versionSlug) {
        if (isCopyVersionRunning(projectSlug, versionSlug)) {
            CopyVersionTaskHandle handle =
                    getCopyVersionProcessHandle(projectSlug, versionSlug);
            handle.cancel(true);
            handle.setCancelledTime(System.currentTimeMillis());
            handle.setCancelledBy(identity.getCredentials().getUsername());
            log.info("Copy version cancelled- {}:{}", projectSlug, versionSlug);
        }
    }

    public CopyVersionTaskHandle getCopyVersionProcessHandle(String projectSlug,
            String versionSlug) {
        return (CopyVersionTaskHandle) asyncTaskHandleManager.getHandleByKey(
                CopyVersionKey.getKey(projectSlug, versionSlug));
    }

    public boolean isCopyVersionRunning(String projectSlug,
            String versionSlug) {
        CopyVersionTaskHandle handle =
                getCopyVersionProcessHandle(projectSlug, versionSlug);
        return handle != null && !handle.isDone();
    }

    /**
     * Key used for copy version task
     *
     */
    public static final class CopyVersionKey {
        private static final String KEY_NAME = "copyVersion";

        /**
         *
         * @param projectSlug
         *            - target project identifier
         * @param versionSlug
         *            - target version identifier
         */
        public static AsyncTaskKey
                getKey(String projectSlug, String versionSlug) {
            return new GenericAsyncTaskKey(
                    joinFields(KEY_NAME, projectSlug, versionSlug));
        }

    }
}
