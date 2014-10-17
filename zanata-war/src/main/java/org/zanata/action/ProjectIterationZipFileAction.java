package org.zanata.action;

import java.io.Serializable;

import lombok.Getter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationArchiveService;

@Name("projectIterationZipFileAction")
@Scope(ScopeType.CONVERSATION)
public class ProjectIterationZipFileAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @In
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @Getter
    private AsyncTaskHandle<String> zipFilePrepHandle;

    @In
    private ZanataIdentity identity;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private TranslationArchiveService translationArchiveServiceImpl;

    @Begin(join = true)
    public void prepareIterationZipFile(boolean isPoProject,
            String projectSlug, String versionSlug, String localeId) {

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
                    projectSlug, versionSlug, localeId, Identity.instance()
                            .getCredentials().getUsername(), zipFilePrepHandle);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @End
    public void cancelFileDownload() {
        zipFilePrepHandle.cancel(true);
    }
}
