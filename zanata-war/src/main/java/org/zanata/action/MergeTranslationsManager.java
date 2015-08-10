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
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.MergeTranslationsService;

/**
 * Manages tasks to copy translations from one existing version to another.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AutoCreate
@Name("mergeTranslationsManager")
@Scope(ScopeType.STATELESS)
@Slf4j
public class MergeTranslationsManager implements Serializable {
    private static final long serialVersionUID = -8717740654253262530L;
    @In
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @In
    private MergeTranslationsService mergeTranslationsServiceImpl;

    @In
    private ZanataIdentity identity;

    /**
     * Merge translations from an existing version to another.
     *
     * @param sourceProjectSlug - source project identifier
     * @param sourceVersionSlug - source version identifier
     * @param targetProjectSlug - target project identifier
     * @param targetVersionSlug - target version identifier
     * @param useNewerTranslation - to override translated/approved string
     *                                 in target with newer entry in source
     */
    public void start(String sourceProjectSlug, String sourceVersionSlug,
        String targetProjectSlug, String targetVersionSlug,
        boolean useNewerTranslation) {

        Key key =
                Key.getKey(targetProjectSlug,
                    targetVersionSlug);

        MergeTranslationsTaskHandle handle = new MergeTranslationsTaskHandle();
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        mergeTranslationsServiceImpl.startMergeTranslations(sourceProjectSlug,
                sourceVersionSlug, targetProjectSlug, targetVersionSlug,
            useNewerTranslation, handle);
    }

    /**
     * Cancel running merge translations task
     *
     * @param projectSlug - target project identifier
     * @param versionSlug - target version identifier
     */
    public void cancel(String projectSlug, String versionSlug) {
        if (isRunning(projectSlug, versionSlug)) {
            MergeTranslationsTaskHandle handle =
                    getProcessHandle(projectSlug, versionSlug);
            handle.cancel(true);
            handle.setCancelledTime(System.currentTimeMillis());
            handle.setCancelledBy(identity.getCredentials().getUsername());

            log.info("Merge translations cancelled- {}:{}", projectSlug,
                    versionSlug);
        }
    }

    public MergeTranslationsTaskHandle getProcessHandle(
        String projectSlug, String versionSlug) {
        return (MergeTranslationsTaskHandle) asyncTaskHandleManager
                .getHandleByKey(
                    Key.getKey(projectSlug, versionSlug));
    }

    public boolean isRunning(String projectSlug, String versionSlug) {
        MergeTranslationsTaskHandle handle =
            getProcessHandle(projectSlug, versionSlug);
        return handle != null && !handle.isDone();
    }

    /**
     * Key used for copy version task
     *
     */
    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    public static final class Key implements Serializable {
        // target project identifier
        private final String projectSlug;
        // target version identifier
        private final String versionSlug;

        public static Key getKey(String projectSlug,
                String versionSlug) {
            return new Key(projectSlug, versionSlug);
        }
    }
}
