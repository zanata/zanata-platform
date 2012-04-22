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
package org.zanata.feature;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.action.ProjectAction;
import org.zanata.action.LoginAction;
import org.zanata.page.HomePage;
import org.zanata.page.ProjectPage;
import org.zanata.page.ProjectVersionPage;

import static org.hamcrest.MatcherAssert.*;

public class CreateProjectTest
{

   @Test
   public void canCreateProjectAndVersion() {
      final String projectId = "plurals";
      final String projectVersion = "master";
      final String projectName = "plural project";

      HomePage homePage = new LoginAction().signIn("admin", "admin");
      ProjectAction projectAction = new ProjectAction();
      ProjectPage projectPage = projectAction.createNewProject(homePage, projectId, projectName);

      assertThat(projectPage.getProjectId(), Matchers.equalTo("Project ID: " + projectId));
      assertThat(projectPage.getProjectName(), Matchers.equalTo("Name: plural project"));

      ProjectVersionPage projectVersionPage = projectAction.createNewProjectVersion(projectPage, projectVersion);

      //can go to project version page
      projectPage = projectAction.goToProjectByName(projectVersionPage, projectName);
      projectVersionPage = projectPage.goToActiveVersion(projectVersion);
   }
}
