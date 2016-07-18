package org.zanata.action;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyVersionService;

/**
 * Manages copy version tasks.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Dependent
@Slf4j
public class CopyVersionManager implements Serializable {
    private static final long serialVersionUID = 3414395834255069870L;
    @Inject
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @Inject
    private CopyVersionService copyVersionServiceImpl;

    @Inject
    private ZanataIdentity identity;

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
        CopyVersionKey key = CopyVersionKey.getKey(projectSlug, newVersionSlug);
        CopyVersionTaskHandle handle = new CopyVersionTaskHandle();
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

    public CopyVersionTaskHandle getCopyVersionProcessHandle(
            String projectSlug, String versionSlug) {
        return (CopyVersionTaskHandle) asyncTaskHandleManager
                .getHandleByKey(CopyVersionKey.getKey(projectSlug, versionSlug));
    }

    public boolean isCopyVersionRunning(String projectSlug, String versionSlug) {
        CopyVersionTaskHandle handle =
                getCopyVersionProcessHandle(projectSlug, versionSlug);
        return handle != null && !handle.isDone();
    }

    /**
     * Key used for copy version task
     *
     * @param projectSlug
     *            - target project identifier
     * @param versionSlug
     *            - target version identifier
     */
    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    public static final class CopyVersionKey implements Serializable {
        // target project identifier
        private final String projectSlug;
        // target version identifier
        private final String versionSlug;

        public static CopyVersionKey getKey(String projectSlug,
                String versionSlug) {
            return new CopyVersionKey(projectSlug, versionSlug);
        }
    }
}
