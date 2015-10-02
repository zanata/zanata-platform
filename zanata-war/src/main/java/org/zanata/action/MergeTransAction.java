package org.zanata.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.ui.CopyAction;
import org.zanata.ui.faces.FacesMessages;

/**
 * Handles user interaction from merge_trans_modal.xhtml.
 * - start merge translation process.
 * - cancel merge translation process.
 * - gives progress data of merge translation.
 * - provides projects and versions for user selection.
 *
 * see merge_trans_modal.xhtml for all actions.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("mergeTransAction")
@javax.faces.bean.ViewScoped
public class MergeTransAction extends CopyAction implements Serializable {

    @Getter
    private String targetProjectSlug;

    @Getter
    @Setter
    private String targetVersionSlug;

    @Getter
    private String sourceProjectSlug;

    @Getter
    @Setter
    private String sourceVersionSlug;

    @Getter
    @Setter
    private boolean keepExistingTranslation;

    @Inject
    private ProjectDAO projectDAO;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private MergeTranslationsManager mergeTranslationsManager;

    @Inject
    private CopyTransManager copyTransManager;

    @Inject
    private CopyVersionManager copyVersionManager;

    @Inject
    private Messages msgs;

    @Inject /* TODO [CDI] check this: migrated from @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER) */
    private HAccount authenticatedAccount;

    @Inject
    private FacesMessages jsfMessages;

    private HProjectIteration targetVersion;

    private HProject sourceProject;

    public void setTargetProjectSlug(String targetProjectSlug) {
        this.targetProjectSlug = targetProjectSlug;

        /**
         * This should only true during first instantiation to make sure
         * sourceProject from selection are the same as targetProject on load.
         *
         * See pages.xml for setter of mergeTransAction.targetProjectSlug.
         */
        if(sourceProjectSlug == null) {
            setSourceProjectSlug(targetProjectSlug);
        }
    }

    public void setSourceProjectSlug(String sourceProjectSlug) {
        if(!StringUtils.equals(this.sourceProjectSlug, sourceProjectSlug)) {
            this.sourceProjectSlug = sourceProjectSlug;
            refreshSourceProject();
            this.sourceVersionSlug = null;
            if (!getSourceProject().getProjectIterations().isEmpty()) {
                this.sourceVersionSlug =
                    getSourceProject().getProjectIterations().get(0).getSlug();
            }
        }
    }

    private void refreshSourceProject() {
        sourceProject = null;
    }

    public HProjectIteration getTargetVersion() {
        if (targetVersion == null && StringUtils.isNotEmpty(targetProjectSlug)
                && StringUtils.isNotEmpty(targetVersionSlug)) {
            targetVersion =
                    projectIterationDAO.getBySlug(targetProjectSlug,
                            targetVersionSlug);
        }
        return targetVersion;
    }

    public HProject getSourceProject() {
        if(sourceProject == null && StringUtils.isNotEmpty(sourceProjectSlug)) {
            sourceProject = projectDAO.getBySlug(sourceProjectSlug);
        }
        return sourceProject;
    }

    public List<HProjectIteration> getSourceVersions() {
        List<HProjectIteration> versions =
                getSourceProject().getProjectIterations();

        if(versions.isEmpty()) {
            return Collections.emptyList();
        }

        List<HProjectIteration> results = Lists.newArrayList();
        //remove obsolete version and target version if both are the same project
        for (HProjectIteration version : versions) {
            if (version.getStatus().equals(EntityStatus.OBSOLETE)) {
                continue;
            }
            if(StringUtils.equals(sourceProjectSlug, targetProjectSlug) &&
                version.getSlug().equals(targetVersionSlug)) {
                continue;
            }
            results.add(version);
        }
        return results;
    }

    /**
     * Only display user maintained project to merge translation from in this
     * UI. TODO: implement filterable drop down and allow users to select any
     * available project.
     *
     */
    public List<HProject> getProjects() {
        return projectDAO.getProjectsForMaintainer(
                authenticatedAccount.getPerson(), null, 0, Integer.MAX_VALUE);
    }

    public void startMergeTranslations() {
        if (StringUtils.isEmpty(sourceProjectSlug)
                || StringUtils.isEmpty(sourceVersionSlug)
                || StringUtils.isEmpty(targetProjectSlug)
                || StringUtils.isEmpty(targetVersionSlug)) {
            jsfMessages.addGlobal(FacesMessage.SEVERITY_ERROR,
                    msgs.get("jsf.iteration.mergeTrans.noSourceAndTarget"));
            return;
        }
        if (isCopyActionsRunning()) {
            jsfMessages.addGlobal(FacesMessage.SEVERITY_WARN,
                    msgs.get("jsf.iteration.mergeTrans.hasCopyActionRunning"));
            return;
        }
        mergeTranslationsManager.start(sourceProjectSlug,
                sourceVersionSlug, targetProjectSlug, targetVersionSlug,
                !keepExistingTranslation);
        FacesContext context = FacesContext.getCurrentInstance();
        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(context, null, "merge-translation");
    }

    // Check if copy-trans, copy version or merge-trans is running for the
    // target version
    public boolean isCopyActionsRunning() {
        return mergeTranslationsManager.isRunning(
            targetProjectSlug, targetVersionSlug)
                || copyVersionManager.isCopyVersionRunning(targetProjectSlug,
                        targetVersionSlug) ||
                copyTransManager.isCopyTransRunning(getTargetVersion());
    }

    @Override
    public boolean isInProgress() {
        return mergeTranslationsManager.isRunning(
            targetProjectSlug, targetVersionSlug);
    }

    @Override
    public String getProgressMessage() {
        MergeTranslationsTaskHandle handle = getHandle();
        if(handle != null) {
            return msgs.format("jsf.iteration.mergeTrans.progress.message",
                handle.getCurrentProgress(), handle.getTotalTranslations());
        }
        return "";
    }

    @Override
    public void onComplete() {
        jsfMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.mergeTrans.completed.message",
                        sourceProjectSlug, sourceVersionSlug,
                        targetProjectSlug, targetVersionSlug));
    }

    public void cancel() {
        mergeTranslationsManager.cancel(targetProjectSlug,
            targetVersionSlug);
        jsfMessages.addGlobal(
                FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.mergeTrans.cancel.message",
                        sourceProjectSlug, sourceVersionSlug,
                        targetProjectSlug, targetVersionSlug));
    }

    @Override
    protected MergeTranslationsTaskHandle getHandle() {
        return mergeTranslationsManager.getProcessHandle(
            targetProjectSlug, targetVersionSlug);
    }
}
