package org.zanata.action;

import java.io.Serializable;
import java.text.DecimalFormat;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationArchiveService;

@Named("projectIterationZipFileAction")
@GroupedConversationScoped
@Model
@Transactional
public class ProjectIterationZipFileAction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private GroupedConversation conversation;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    private AsyncTaskHandle<String> zipFilePrepHandle;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private TranslationArchiveService translationArchiveServiceImpl;

    public void prepareIterationZipFile(boolean isPoProject, String projectSlug,
            String versionSlug, String localeId) {
        identity.checkPermission("download-all",
                projectIterationDAO.getBySlug(projectSlug, versionSlug));
        if (zipFilePrepHandle != null && !zipFilePrepHandle.isDone()) {
            // Cancel any other processes for this conversation
            zipFilePrepHandle.cancel(true);
        }
        // Start the zip file build
        zipFilePrepHandle = new AsyncTaskHandle<String>();
        asyncTaskHandleManager.registerTaskHandle(zipFilePrepHandle);
        try {
            translationArchiveServiceImpl.startBuildingTranslationFileArchive(
                    projectSlug, versionSlug, localeId,
                    ZanataIdentity.instance().getCredentials().getUsername(),
                    zipFilePrepHandle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void cancelFileDownload() {
        if (zipFilePrepHandle != null) {
            zipFilePrepHandle.cancel(true);
        }
        conversation.close();
    }

    final DecimalFormat PERCENT_FORMAT = new DecimalFormat("###.##");

    public String getCompletedPercentage() {
        if (zipFilePrepHandle != null) {
            double completedPercent =
                    (double) zipFilePrepHandle.getCurrentProgress()
                            / (double) zipFilePrepHandle.getMaxProgress() * 100;
            return PERCENT_FORMAT.format(completedPercent) + "%";
        }
        return "0%";
    }

    public AsyncTaskHandle<String> getZipFilePrepHandle() {
        return this.zipFilePrepHandle;
    }
}
