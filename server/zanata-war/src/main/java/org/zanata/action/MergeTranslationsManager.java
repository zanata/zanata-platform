package org.zanata.action;

import java.io.Serializable;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.MergeTranslationsService;

/**
 * Manages tasks to copy translations from one existing version to another.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Dependent
public class MergeTranslationsManager implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(MergeTranslationsManager.class);

    private static final long serialVersionUID = -8717740654253262530L;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    private MergeTranslationsService mergeTranslationsServiceImpl;
    @Inject
    private ZanataIdentity identity;

    /**
     * Merge translations from an existing version to another.
     *
     * @param sourceProjectSlug
     *            - source project identifier
     * @param sourceVersionSlug
     *            - source version identifier
     * @param targetProjectSlug
     *            - target project identifier
     * @param targetVersionSlug
     *            - target version identifier
     * @param useNewerTranslation
     *            - to override translated/approved string in target with newer
     *            entry in source
     */
    public void start(String sourceProjectSlug, String sourceVersionSlug,
            String targetProjectSlug, String targetVersionSlug,
            boolean useNewerTranslation) {
        MergeVersionKey
                key = MergeVersionKey
                .getKey(targetProjectSlug, targetVersionSlug);
        MergeTranslationsTaskHandle handle = new MergeTranslationsTaskHandle();
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        mergeTranslationsServiceImpl.startMergeTranslations(sourceProjectSlug,
                sourceVersionSlug, targetProjectSlug, targetVersionSlug,
                useNewerTranslation, handle);
    }

    /**
     * Cancel running merge translations task
     *
     * @param projectSlug
     *            - target project identifier
     * @param versionSlug
     *            - target version identifier
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

    public MergeTranslationsTaskHandle getProcessHandle(String projectSlug,
            String versionSlug) {
        return (MergeTranslationsTaskHandle) asyncTaskHandleManager
                .getHandleByKey(MergeVersionKey.getKey(projectSlug, versionSlug));
    }

    public boolean isRunning(String projectSlug, String versionSlug) {
        MergeTranslationsTaskHandle handle =
                getProcessHandle(projectSlug, versionSlug);
        return handle != null && !handle.isDone();
    }

    /**
     * Key used for merge version task
     */
    public static final class MergeVersionKey implements
            AsyncTaskHandleManager.AsyncTaskKey {
        private static final long serialVersionUID = 1L;
        private static final String KEY_NAME = "mergeVersion";
        // target project identifier
        private final String projectSlug;
        // target version identifier
        private final String versionSlug;
        private final String id;

        public static MergeVersionKey getKey(String projectSlug, String versionSlug) {
            return new MergeVersionKey(projectSlug, versionSlug);
        }


        public String getProjectSlug() {
            return this.projectSlug;
        }

        public String getVersionSlug() {
            return this.versionSlug;
        }

        @java.beans.ConstructorProperties({ "projectSlug", "versionSlug" })
        public MergeVersionKey(final String projectSlug, final String versionSlug) {
            this.projectSlug = projectSlug;
            this.versionSlug = versionSlug;
            this.id = joinFields(KEY_NAME, projectSlug, versionSlug);
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MergeVersionKey that = (MergeVersionKey) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
