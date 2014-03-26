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
import javax.faces.application.FacesMessage;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.async.tasks.CopyTransTask;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.ui.ProgressBar;
import org.zanata.util.DateUtil;
import org.zanata.util.ZanataMessages;
import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;

/**
 * Copy Trans page action bean.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("copyTransAction")
public class CopyTransAction implements Serializable, ProgressBar {
    private static final long serialVersionUID = 1L;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private CopyTransManager copyTransManager;

    @In
    private ConversationScopeMessages conversationScopeMessages;

    @In
    private ZanataMessages zanataMessages;

    @Getter
    @Setter
    private String iterationSlug;

    @Getter
    @Setter
    private String projectSlug;

    private HCopyTransOptions options;

    private HProjectIteration projectIteration;

    @Override
    public boolean isInProgress() {
        return copyTransManager.isCopyTransRunning(getProjectIteration());
    }

    @Override
    public int getCompletedPercentage() {
        CopyTransTask.CopyTransTaskHandle handle =
                copyTransManager
                        .getCopyTransProcessHandle(getProjectIteration());
        if (handle != null) {
            int completedPercent =
                    handle.getCurrentProgress() * 100 / handle.getMaxProgress();
            if (completedPercent == 100) {
                conversationScopeMessages
                        .putMessage(
                                FacesMessage.SEVERITY_INFO,
                                zanataMessages
                                        .getMessage("jsf.iteration.CopyTrans.Completed"));
            }
            return completedPercent;
        } else {
            return 0;
        }
    }

    public HProjectIteration getProjectIteration() {
        if (this.projectIteration == null) {
            this.projectIteration =
                    projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        }
        return this.projectIteration;
    }

    public void initialize() {
        HProject project = this.getProjectIteration().getProject();
        if (project.getDefaultCopyTransOpts() != null) {
            options = project.getDefaultCopyTransOpts();
        } else {
            options = new HCopyTransOptions();
        }
    }

    @Restrict("#{s:hasPermission(copyTransAction.projectIteration, 'copy-trans')}")
    public
            void startCopyTrans() {
        if (isInProgress()) {
            return;
        } else if (getProjectIteration().getDocuments().size() <= 0) {
            conversationScopeMessages.putMessage(FacesMessage.SEVERITY_INFO,
                    zanataMessages
                            .getMessage("jsf.iteration.CopyTrans.NoDocuments"));
            return;
        }

        copyTransManager.startCopyTrans(getProjectIteration(), options);
        conversationScopeMessages.putMessage(FacesMessage.SEVERITY_INFO,
                zanataMessages.getMessage("jsf.iteration.CopyTrans.Started"));
    }

    public void cancel() {
        copyTransManager.cancelCopyTrans(getProjectIteration());
        conversationScopeMessages.putMessage(FacesMessage.SEVERITY_INFO,
                zanataMessages.getMessage("jsf.iteration.CopyTrans.Cancelled"));
    }

    public String getDocumentsProcessed() {
        CopyTransTask.CopyTransTaskHandle handle =
                copyTransManager
                        .getCopyTransProcessHandle(getProjectIteration());
        if (handle != null) {
            return String.valueOf(handle.getDocumentsProcessed());
        }
        return "";
    }

    public String getCopyTransEstimatedTimeLeft() {
        CopyTransTask.CopyTransTaskHandle handle =
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
