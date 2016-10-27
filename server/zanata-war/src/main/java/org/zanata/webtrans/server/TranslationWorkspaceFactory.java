/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.server;

import com.ibm.icu.util.ULocale;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;

import javax.inject.Inject;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TranslationWorkspaceFactory {

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private LocaleService localeServiceImpl;

    private WorkspaceContext validateAndGetWorkspaceContext(
            WorkspaceId workspaceId) throws NoSuchWorkspaceException {
        String projectSlug =
                workspaceId.getProjectIterationId().getProjectSlug();
        String iterationSlug =
                workspaceId.getProjectIterationId().getIterationSlug();
        HProjectIteration projectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);

        if (projectIteration == null) {
            throw new NoSuchWorkspaceException("Invalid workspace Id");
        }
        HProject project = projectIteration.getProject();
        if (project.getStatus() == EntityStatus.OBSOLETE) {
            throw new NoSuchWorkspaceException("Project is obsolete");
        }
        if (projectIteration.getStatus() == EntityStatus.OBSOLETE) {
            throw new NoSuchWorkspaceException("Project Iteration is obsolete");
        }
        HLocale locale =
                localeServiceImpl.getByLocaleId(workspaceId.getLocaleId());
        if (locale == null) {
            throw new NoSuchWorkspaceException("Invalid Workspace Locale");
        }
        if (!locale.isActive()) {
            throw new NoSuchWorkspaceException("Locale '"
                    + locale.retrieveDisplayName() + "' disabled in server");
        }

        String workspaceName =
                project.getName() + " (" + projectIteration.getSlug() + ")";
        String localeDisplayName =
                ULocale.getDisplayName(workspaceId.getLocaleId().toJavaName(),
                        ULocale.ENGLISH);

        return new WorkspaceContext(workspaceId, workspaceName,
                localeDisplayName);
    }

    public TranslationWorkspace createWorkspace(WorkspaceId workspaceId)
            throws NoSuchWorkspaceException {
        WorkspaceContext workspaceContext =
                validateAndGetWorkspaceContext(workspaceId);
        return new TranslationWorkspaceImpl(workspaceContext);
    }

}
