package org.zanata.action;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.service.CopyVersionService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">aeng@redhat.com</a>
 */
@AutoCreate
@Name("copyVersionManager")
@Scope(ScopeType.STATELESS)
@Slf4j
public class CopyVersionManager implements Serializable {
    @In
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @In
    private CopyVersionService copyVersionServiceImpl;

    @In
    private Identity identity;

    public void startCopyVersion(String projectSlug, String versionSlug,
            String newVersionSlug) {
        CopyVersionKey key = CopyVersionKey.getKey(projectSlug, newVersionSlug);
        CopyVersionTaskHandle handle = new CopyVersionTaskHandle();
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        copyVersionServiceImpl.startCopyVersion(projectSlug, versionSlug,
                newVersionSlug, handle);
    }

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
