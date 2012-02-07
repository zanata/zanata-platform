/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.webtrans.server;

import org.zanata.action.ProjectHome;
import org.zanata.action.ProjectIterationHome;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProjectIteration;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ProjectIterationUpdate;
import org.zanata.webtrans.shared.rpc.ProjectUpdate;
            workspace.publish(event);
         }
      }
   }

   @Observer(ProjectHome.PROJECT_UPDATE)
   public void projectUpdate(HIterationProject project)
   {
      log.info("Project {0} updated", project.getSlug());
      ImmutableSet<TranslationWorkspace> workspaceSet = ImmutableSet.copyOf(workspaceMap.values());
      for (TranslationWorkspace workspace : workspaceSet)
      {
         if (workspace.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getProjectSlug().equals(project.getSlug()))
         {
            ProjectUpdate event = new ProjectUpdate(project.getSlug(), project.getStatus());
            workspace.publish(event);
         }
      }
   }

   @Observer(ProjectIterationHome.PROJECT_ITERATION_UPDATE)
   public void projectIterationUpdate(HProjectIteration projectIteration)
   {
      log.info("Project iteration {0} updated", projectIteration.getSlug());
      ImmutableSet<TranslationWorkspace> workspaceSet = ImmutableSet.copyOf(workspaceMap.values());
      for (TranslationWorkspace workspace : workspaceSet)
      {
         if (workspace.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getProjectSlug().equals(projectIteration.getProject().getSlug()) && 
 workspace.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getIterationSlug().equals(projectIteration.getSlug()))
         {
            ProjectIterationUpdate event = new ProjectIterationUpdate(projectIteration.getProject().getSlug(), projectIteration.getProject().getStatus(), projectIteration.getSlug(), projectIteration.getStatus());

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public interface TranslationWorkspaceManager
{

   public TranslationWorkspace getOrRegisterWorkspace(WorkspaceId workspaceId) throws NoSuchWorkspaceException;

}