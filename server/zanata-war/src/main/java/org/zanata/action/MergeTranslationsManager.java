package org.zanata.action;

import java.io.Serializable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
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
        Key key = Key.getKey(targetProjectSlug, targetVersionSlug);
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
                .getHandleByKey(Key.getKey(projectSlug, versionSlug));
    }

    public boolean isRunning(String projectSlug, String versionSlug) {
        MergeTranslationsTaskHandle handle =
                getProcessHandle(projectSlug, versionSlug);
        return handle != null && !handle.isDone();
    }

    /**
     * Key used for copy version task
     */
    public static final class Key implements Serializable {
        // target project identifier
        private final String projectSlug;
        // target version identifier
        private final String versionSlug;

        public static Key getKey(String projectSlug, String versionSlug) {
            return new Key(projectSlug, versionSlug);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof MergeTranslationsManager.Key))
                return false;
            final Key other = (Key) o;
            final Object this$projectSlug = this.getProjectSlug();
            final Object other$projectSlug = other.getProjectSlug();
            if (this$projectSlug == null ? other$projectSlug != null
                    : !this$projectSlug.equals(other$projectSlug))
                return false;
            final Object this$versionSlug = this.getVersionSlug();
            final Object other$versionSlug = other.getVersionSlug();
            if (this$versionSlug == null ? other$versionSlug != null
                    : !this$versionSlug.equals(other$versionSlug))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $projectSlug = this.getProjectSlug();
            result = result * PRIME
                    + ($projectSlug == null ? 43 : $projectSlug.hashCode());
            final Object $versionSlug = this.getVersionSlug();
            result = result * PRIME
                    + ($versionSlug == null ? 43 : $versionSlug.hashCode());
            return result;
        }

        public String getProjectSlug() {
            return this.projectSlug;
        }

        public String getVersionSlug() {
            return this.versionSlug;
        }

        @java.beans.ConstructorProperties({ "projectSlug", "versionSlug" })
        public Key(final String projectSlug, final String versionSlug) {
            this.projectSlug = projectSlug;
            this.versionSlug = versionSlug;
        }
    }
}
