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
package org.zanata.workflow;

import org.zanata.page.AbstractPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.util.Constants;

public class ProjectWorkFlow extends AbstractWebWorkFlow
{
   public ProjectPage createNewProject(String projectId, String projectName)
   {
      return goToHome().goToProjects().clickOnCreateProjectLink().inputProjectId(projectId).inputProjectName(projectName).saveProject();
   }

   public ProjectVersionPage createNewProjectVersion(ProjectPage projectPage, String projectVersion)
   {
      return projectPage.clickCreateVersionLink().inputVersionId(projectVersion).saveVersion();
   }

   public <P extends AbstractPage> ProjectPage goToProjectByName(String projectName)
   {
      ProjectsPage projects = goToHome().goToPage(Constants.projectsLink.value(), ProjectsPage.class);
      return projects.goToProject(projectName);
   }
}
