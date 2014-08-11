package org.zanata.action;

import static org.zanata.async.tasks.CopyVersionTask.CopyVersionTaskHandle;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.zanata.async.tasks.CopyVersionTask;
import org.zanata.service.AsyncTaskManagerService;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AutoCreate
@Name("copyVersionManager")
@Scope(ScopeType.STATELESS)
@Slf4j
public class CopyVersionManager implements Serializable {
    @In
    private AsyncTaskManagerService asyncTaskManagerServiceImpl;

    @In
    private Identity identity;

    public void startCopyVersion(String projectSlug, String versionSlug,
            String newVersionSlug) {
        asyncTaskManagerServiceImpl.startTask(new CopyVersionTask(
                projectSlug, versionSlug, newVersionSlug),
                CopyVersionKey.getKey(projectSlug, newVersionSlug));
    }

    public void cancelCopyVersion(String projectSlug, String versionSlug) {
        if (isCopyVersionRunning(projectSlug, versionSlug)) {
            CopyVersionTaskHandle handle =
                    getCopyVersionProcessHandle(projectSlug, versionSlug);
            handle.forceCancel();
            handle.setCancelledTime(System.currentTimeMillis());
            handle.setCancelledBy(identity.getCredentials().getUsername());

            log.info("Copy version cancelled- {}:{}", projectSlug, versionSlug);
        }
    }

    public CopyVersionTask.CopyVersionTaskHandle getCopyVersionProcessHandle(
            String projectSlug, String versionSlug) {
        return (CopyVersionTask.CopyVersionTaskHandle) asyncTaskManagerServiceImpl
                .getHandleByKey(CopyVersionKey.getKey(projectSlug, versionSlug));
    }

    public boolean isCopyVersionRunning(String projectSlug, String versionSlug) {
        CopyVersionTaskHandle handle =
                getCopyVersionProcessHandle(projectSlug, versionSlug);
        return handle != null && !handle.isDone();
    }

    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    public static final class CopyVersionKey implements Serializable {
        private final String projectSlug;
        private final String versionSlug;

        public static CopyVersionKey getKey(String projectSlug,
                String versionSlug) {
            return new CopyVersionKey(projectSlug, versionSlug);
        }
    }
}
