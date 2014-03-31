package org.zanata.action;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.tasks.ZipFileBuildTask;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.AsyncTaskManagerService;

@Name("projectIterationZipFileAction")
@Scope(ScopeType.CONVERSATION)
public class ProjectIterationZipFileAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @In
    private AsyncTaskManagerService asyncTaskManagerServiceImpl;

    @Getter
    private AsyncTaskHandle<String> zipFilePrepHandle;

    @In
    private ZanataIdentity identity;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @Begin(join = true)
    public void prepareIterationZipFile(boolean isPoProject,
            String projectSlug, String versionSlug, String localeId) {

        identity.checkPermission("download-all",
                projectIterationDAO.getBySlug(projectSlug, versionSlug));

        if (zipFilePrepHandle != null && !zipFilePrepHandle.isDone()) {
            // Cancel any other processes for this conversation
            zipFilePrepHandle.forceCancel();
        }

        // Start a zip file task
        ZipFileBuildTask task =
                new ZipFileBuildTask(projectSlug, versionSlug, localeId,
                        Identity.instance().getCredentials().getUsername(),
                        isPoProject);

        String taskId = asyncTaskManagerServiceImpl.startTask(task);
        zipFilePrepHandle = asyncTaskManagerServiceImpl.getHandle(taskId);
    }

    @End
    public void cancelFileDownload() {
        zipFilePrepHandle.cancel();
    }
}
