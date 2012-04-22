/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.action;

import org.zanata.page.AbstractPage;
import org.zanata.page.HomePage;
import org.zanata.page.ProjectPage;
import org.zanata.page.ProjectVersionPage;
import org.zanata.page.ProjectsPage;

public class ProjectAction extends AbstractAction
{
   public ProjectPage createNewProject(HomePage homePage, String projectId, String projectName)
   {
      return homePage.goToProjects()
            .clickOnCreateProjectLink()
            .inputProjectId(projectId)
            .inputProjectName(projectName)
            .saveProject();
   }

   public ProjectVersionPage createNewProjectVersion(ProjectPage projectPage, String projectVersion)
   {
      return projectPage.clickCreateVersionLink()
            .inputVersionId(projectVersion)
            .saveVersion();
   }

   public <P extends AbstractPage> ProjectPage goToProjectByName(P page, String projectName)
   {
      ProjectsPage projects = page.goToPage("Projects", ProjectsPage.class);
      return projects.goToProject(projectName);
   }
}
