/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.CopyTransOptionFactory;
import org.zanata.ui.CopyAction;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.DateUtil;
import com.google.common.base.Optional;

/**
 * Copy Trans page action bean.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("copyTransAction")
@RequestScoped
@Model
@Transactional
public class CopyTransAction extends CopyAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private CopyTransManager copyTransManager;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private Messages msgs;

    @Inject
    private CopyTransOptionsModel copyTransOptionsModel;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private org.zanata.ui.faces.FacesMessages jsfMessages;

    @Inject
    @Any
    private ProjectAndVersionSlug projectAndVersionSlug;

    private HProjectIteration projectIteration;

    public boolean isInProgress() {
        return copyTransManager.isCopyTransRunning(getProjectIteration());
    }

    @Override
    public String getProgressMessage() {
        StringBuilder message = new StringBuilder();
        message.append(
                msgs.format("jsf.iteration.CopyTrans.processedItems",
                        getCurrentProgress(), getMaxProgress()))
                .append(", ")
                .append(msgs.format(
                        "jsf.iteration.CopyTrans.estimatedTimeRemaining",
                        getCopyTransEstimatedTimeLeft()));

        return message.toString();
    }

    @Override
    public void onComplete() {
        jsfMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.CopyTrans.Completed",
                        getProjectSlug(), getVersionSlug()));
    }

    public void updateCopyTrans(String action, String value) {
        copyTransOptionsModel.update(action, value);
    }

    public String getProjectSlug() {
        return projectAndVersionSlug.getProjectSlug();
    }

    public void setProjectSlug(String projectSlug) {
        projectAndVersionSlug.setProjectSlug(projectSlug);
    }

    public String getVersionSlug() {
        return projectAndVersionSlug.getVersionSlug();
    }

    public void setVersionSlug(String versionSlug) {
        projectAndVersionSlug.setVersionSlug(versionSlug);
    }

    @Override
    protected CopyTransTaskHandle getHandle() {
        return copyTransManager
                .getCopyTransProcessHandle(getProjectIteration());
    }

    @Nonnull
    private HProjectIteration getProjectIteration() {
        // TODO share code with ProjectVersionService.retrieveAndCheckIteration?
        if (projectIteration == null) {
            projectIteration =
                    projectIterationDAO.getBySlug(getProjectSlug(), getVersionSlug());
            if (projectIteration == null) {
                throw new NoSuchEntityException("Project version '" + getProjectSlug()
                        + ":" + getVersionSlug() + "' not found.");
            }
        }
        return projectIteration;
    }

    public void startCopyTrans() {
        identity.checkPermission(getProjectIteration(), "copy-trans");
        if (isInProgress()) {
            return;
        } else if (getProjectIteration().getDocuments().size() <= 0) {
            facesMessages.addGlobal(msgs.get("jsf.iteration.CopyTrans.NoDocuments"));
            return;
        }

        HCopyTransOptions options = copyTransOptionsModel.getInstance();

        copyTransManager.startCopyTrans(getProjectIteration(), options);
        facesMessages.addGlobal(msgs.get("jsf.iteration.CopyTrans.Started"));
    }

    public void cancel() {
        copyTransManager.cancelCopyTrans(getProjectIteration());
        facesMessages.addGlobal(msgs.get("jsf.iteration.CopyTrans.Cancelled"));
        copyTransOptionsModel.setInstance(CopyTransOptionFactory
            .getImplicitOptions());
    }

    public String getCurrentProgress() {
        CopyTransTaskHandle handle =
                copyTransManager
                        .getCopyTransProcessHandle(getProjectIteration());
        if (handle != null) {
            return String.valueOf(handle.getCurrentProgress());
        }
        return "";
    }

    public String getMaxProgress() {
        CopyTransTaskHandle handle =
                copyTransManager
                        .getCopyTransProcessHandle(getProjectIteration());
        if (handle != null) {
            return String.valueOf(handle.getMaxProgress());
        }
        return "";
    }

    public String getCopyTransEstimatedTimeLeft() {
        CopyTransTaskHandle handle =
                copyTransManager
                        .getCopyTransProcessHandle(getProjectIteration());
        if (handle != null) {
            Optional<Long> estimatedTimeRemaining =
                    handle.getEstimatedTimeRemaining();
            if (estimatedTimeRemaining.isPresent()) {
                return DateUtil
                        .getTimeRemainingDescription(estimatedTimeRemaining
                                .get());
            }
        }
        return "";
    }
}
