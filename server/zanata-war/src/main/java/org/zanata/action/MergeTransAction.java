package org.zanata.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.ZanataIdentity;
import org.zanata.ui.CopyAction;
import org.zanata.util.FacesNavigationUtil;
import org.zanata.ui.faces.FacesMessages;

/**
 * Handles user interaction from merge_trans_modal.xhtml. - start merge
 * translation process. - cancel merge translation process. - gives progress
 * data of merge translation. - provides projects and versions for user
 * selection.
 *
 * see merge_trans_modal.xhtml for all actions.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("mergeTransAction")
@ViewScoped
@Model
@Transactional
public class MergeTransAction extends CopyAction implements Serializable {

    private String targetProjectSlug;
    private String targetVersionSlug;
    private String sourceProjectSlug;
    private String sourceVersionSlug;
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
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private FacesMessages jsfMessages;
    @Inject
    private ZanataIdentity identity;
    private HProjectIteration targetVersion;
    private HProject sourceProject;

    public void setTargetProjectSlug(String targetProjectSlug) {
        this.targetProjectSlug = targetProjectSlug;
        if (sourceProjectSlug == null) {
            setSourceProjectSlug(targetProjectSlug);
        }
    }

    /**
     * This should only true during first instantiation to make sure
     * sourceProject from selection are the same as targetProject on load.
     *
     * See pages.xml for setter of mergeTransAction.targetProjectSlug.
     */
    public void setSourceProjectSlug(String sourceProjectSlug) {
        if (!StringUtils.equals(this.sourceProjectSlug, sourceProjectSlug)) {
            this.sourceProjectSlug = sourceProjectSlug;
            refreshSourceProject();
            this.sourceVersionSlug = null;
            if (getSourceProject() != null
                    && !getSourceProject().getProjectIterations().isEmpty()) {
                this.sourceVersionSlug = getSourceProject()
                        .getProjectIterations().get(0).getSlug();
            }
        }
    }

    private void refreshSourceProject() {
        sourceProject = null;
    }

    public HProjectIteration getTargetVersion() {
        if (targetVersion == null && StringUtils.isNotEmpty(targetProjectSlug)
                && StringUtils.isNotEmpty(targetVersionSlug)) {
            targetVersion = projectIterationDAO.getBySlug(targetProjectSlug,
                    targetVersionSlug);
        }
        return targetVersion;
    }

    @Nullable
    public HProject getSourceProject() {
        if (sourceProject == null
                && StringUtils.isNotEmpty(sourceProjectSlug)) {
            sourceProject = projectDAO.getBySlug(sourceProjectSlug);
        }
        return sourceProject;
    }

    public List<HProjectIteration> getSourceVersions() {
        List<HProjectIteration> versions =
                getSourceProject().getProjectIterations();
        if (versions.isEmpty()) {
            return Collections.emptyList();
        }
        List<HProjectIteration> results = Lists.newArrayList();
        // remove obsolete version and target version if both are the same
        // project
        for (HProjectIteration version : versions) {
            if (version.getStatus().equals(EntityStatus.OBSOLETE)) {
                continue;
            }
            if (StringUtils.equals(sourceProjectSlug, targetProjectSlug)
                    && version.getSlug().equals(targetVersionSlug)) {
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
     */
    public List<HProject> getProjects() {
        boolean canMergeFromAllProjects = identity
                .hasPermission(getTargetVersion().getProject(), "merge-trans");
        if (canMergeFromAllProjects) {
            return projectDAO.getOffsetList(0, Integer.MAX_VALUE, false, true,
                    true);
        }
        return Lists.newArrayList();
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
        mergeTranslationsManager.start(sourceProjectSlug, sourceVersionSlug,
                targetProjectSlug, targetVersionSlug, !keepExistingTranslation);
        FacesNavigationUtil.handlePageNavigation(null, "merge-translation");
    }
    // Check if copy-trans, copy version or merge-trans is running for the
    // target version

    public boolean isCopyActionsRunning() {
        return mergeTranslationsManager.isRunning(targetProjectSlug,
                targetVersionSlug)
                || copyVersionManager.isCopyVersionRunning(targetProjectSlug,
                        targetVersionSlug)
                || copyTransManager.isCopyTransRunning(getTargetVersion());
    }

    @Override
    public boolean isInProgress() {
        return mergeTranslationsManager.isRunning(targetProjectSlug,
                targetVersionSlug);
    }

    @Override
    public String getProgressMessage() {
        MergeTranslationsTaskHandle handle = getHandle();
        if (handle != null) {
            return msgs.format("jsf.iteration.mergeTrans.progress.message",
                    handle.getCurrentProgress(), handle.getTotalTranslations());
        }
        return "";
    }

    @Override
    public void onComplete() {
        jsfMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.mergeTrans.completed.message",
                        sourceProjectSlug, sourceVersionSlug, targetProjectSlug,
                        targetVersionSlug));
    }

    public void cancel() {
        mergeTranslationsManager.cancel(targetProjectSlug, targetVersionSlug);
        jsfMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.mergeTrans.cancel.message",
                        sourceProjectSlug, sourceVersionSlug, targetProjectSlug,
                        targetVersionSlug));
    }

    @Override
    protected MergeTranslationsTaskHandle getHandle() {
        return mergeTranslationsManager.getProcessHandle(targetProjectSlug,
                targetVersionSlug);
    }

    public String getTargetProjectSlug() {
        return this.targetProjectSlug;
    }

    public String getTargetVersionSlug() {
        return this.targetVersionSlug;
    }

    public void setTargetVersionSlug(final String targetVersionSlug) {
        this.targetVersionSlug = targetVersionSlug;
    }

    public String getSourceProjectSlug() {
        return this.sourceProjectSlug;
    }

    public String getSourceVersionSlug() {
        return this.sourceVersionSlug;
    }

    public void setSourceVersionSlug(final String sourceVersionSlug) {
        this.sourceVersionSlug = sourceVersionSlug;
    }

    public boolean isKeepExistingTranslation() {
        return this.keepExistingTranslation;
    }

    public void
            setKeepExistingTranslation(final boolean keepExistingTranslation) {
        this.keepExistingTranslation = keepExistingTranslation;
    }
}
