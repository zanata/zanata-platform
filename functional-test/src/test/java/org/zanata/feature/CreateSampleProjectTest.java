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

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.zanata.page.ProjectPage;
import org.zanata.page.ProjectVersionPage;
import org.zanata.page.WebTranPage;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LanguageWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

public class CreateSampleProjectTest
{
   private static final Logger LOGGER = LoggerFactory.getLogger(CreateSampleProjectTest.class);
   @Test
   public void canCreateProjectAndVersion()
   {
      final String projectId = "plurals";
      final String projectVersion = "master";
      final String projectName = "plural project";

      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      ProjectPage projectPage = projectWorkFlow.createNewProject(projectId, projectName);

      assertThat(projectPage.getProjectId(), Matchers.equalTo("Project ID: " + projectId));
      assertThat(projectPage.getProjectName(), Matchers.equalTo("Name: plural project"));

      ProjectVersionPage projectVersionPage = projectWorkFlow.createNewProjectVersion(projectPage, projectVersion);

      // can go to project version page
      projectPage = projectWorkFlow.goToProjectByName(projectName);
      projectVersionPage = projectPage.goToActiveVersion(projectVersion);

   }

   @Test
   public void canAddLanguage()
   {
      new LoginWorkFlow().signIn("admin", "admin");
      LanguageWorkFlow workFlow = new LanguageWorkFlow();
      workFlow.addLanguageAndJoin("en-US");
      workFlow.addLanguageAndJoin("pl");
      workFlow.addLanguageAndJoin("zh");
   }

   @Test(dependsOnMethods = { "canCreateProjectAndVersion", "canAddLanguage" })
   public void canPush() throws IOException
   {
      ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
      int exitCode = clientPushWorkFlow.mvnPush("plural");

      assertThat(exitCode, Matchers.equalTo(0));

      ProjectVersionPage projectVersionPage = new ProjectWorkFlow().goToProjectByName("plural project").goToActiveVersion("master");
      assertThat(projectVersionPage.getTranslatableLocales(), Matchers.hasItems("en-US", "pl", "zh"));
   }

   @Test(enabled = false)
   public void canSeeDocumentList() {
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectVersionPage projectVersionPage = new ProjectWorkFlow().goToProjectByName("plural project").goToActiveVersion("master");
      WebTranPage webTranPage = projectVersionPage.translate("pl");

      LOGGER.info("document list table: {}", webTranPage.getDocumentListTableContent());
   }

}
