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
import javax.faces.application.FacesMessage;

import org.jboss.seam.annotations.Begin;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.CopyTransOptionFactory;
import org.zanata.ui.CopyAction;
import org.zanata.util.DateUtil;
import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;

/**
 * Copy Trans page action bean.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("copyTransAction")
@org.apache.deltaspike.core.api.scope.ViewAccessScoped /* TODO [CDI] check this: migrated from ScopeType.CONVERSATION */
public class CopyTransAction extends CopyAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private CopyTransManager copyTransManager;

    @Inject
    private ConversationScopeMessages conversationScopeMessages;

    @Inject
    private Messages msgs;

    @Inject
    private CopyTransOptionsModel copyTransOptionsModel;

    @Inject
    private ZanataIdentity identity;

    @Getter
    @Setter
    private String iterationSlug;

    @Getter
    @Setter
    private String projectSlug;

    private HProjectIteration projectIteration;

    @PostConstruct
    public void onCreate() {
        copyTransOptionsModel.setInstance(CopyTransOptionFactory
                .getExplicitOptions());
    }

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
        FacesMessages.instance().add(StatusMessage.Severity.INFO,
                msgs.format("jsf.iteration.CopyTrans.Completed",
                        getProjectSlug(), getIterationSlug()));
    }

    @Begin(join = true)
    public void updateCopyTrans(String action, String value) {
        copyTransOptionsModel.update(action, value);
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
                    projectIterationDAO.getBySlug(projectSlug, iterationSlug);
            if (projectIteration == null) {
                throw new NoSuchEntityException("Project version '" + projectSlug
                        + ":" + iterationSlug + "' not found.");
            }
        }
        return projectIteration;
    }

    public void startCopyTrans() {
        identity.checkPermission(getProjectIteration(), "copy-trans");
        if (isInProgress()) {
            return;
        } else if (getProjectIteration().getDocuments().size() <= 0) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.get("jsf.iteration.CopyTrans.NoDocuments"));
            return;
        }

        HCopyTransOptions options = copyTransOptionsModel.getInstance();

        copyTransManager.startCopyTrans(getProjectIteration(), options);
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.iteration.CopyTrans.Started"));
    }

    public void cancel() {
        copyTransManager.cancelCopyTrans(getProjectIteration());
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.iteration.CopyTrans.Cancelled"));
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
