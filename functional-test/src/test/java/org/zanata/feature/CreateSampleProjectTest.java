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
package org.zanata.feature;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;

import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.webtrans.WebTranPage;
import org.zanata.util.Constants;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LanguageWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateSampleProjectTest
{
   @Test
   public void canCreateProjectAndVersion()
   {
      final String projectId = "plurals";
      final String projectVersion = "master";
      final String projectName = "plural project";

      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      List<String> projects = projectWorkFlow.goToHome().goToProjects().getProjectNamesOnCurrentPage();
      log.info("current projects: {}", projects);

      ProjectPage projectPage = projectWorkFlow.createNewProject(projectId, projectName);

      assertThat(projectPage.getProjectId(), Matchers.equalTo("Project ID: " + projectId));
      assertThat(projectPage.getProjectName(), Matchers.equalTo("Name: plural project"));

      ProjectVersionPage projectVersionPage = projectWorkFlow.createNewProjectVersion(projectPage, projectVersion);

      // can go to project version page
      projectPage = projectWorkFlow.goToProjectByName(projectName);
      projectVersionPage = projectPage.goToActiveVersion(projectVersion);

   }

   @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "This Project ID is not available")
   public void cannotCreateProjectWithSameProjectId() {
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      ProjectPage projectPage = projectWorkFlow.createNewProject("project-a", "project a");
      assertThat(projectPage.getTitle(), Matchers.containsString("Zanata:project a"));

      //second time
      projectWorkFlow.createNewProject("project-a", "project with same slug/project id");
   }

   @Test
   public void canCreateSameVersionIdOnDifferentProjects() {
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      projectWorkFlow.createNewProject("project-b", "project b")
            .clickCreateVersionLink().inputVersionId("master").saveVersion();

      //second time
      ProjectVersionPage projectVersionPage = projectWorkFlow
            .createNewProject("project-c", "project with same version slug/version id")
            .clickCreateVersionLink().inputVersionId("master").saveVersion();

      assertThat(projectVersionPage.getTitle(), Matchers.equalTo("Zanata:project-c:master"));
   }

   @Test
   public void canAddLanguage()
   {
      new LoginWorkFlow().signIn("admin", "admin");
      LanguageWorkFlow workFlow = new LanguageWorkFlow();
      workFlow.addLanguageAndJoin("en-US");
      workFlow.addLanguageAndJoin("pl");
      workFlow.addLanguageAndJoin("zh");

      ManageLanguagePage languagePage = workFlow.goToHome().goToAdministration().goToManageLanguagePage();
      List<String> languageLocales = languagePage.getLanguageLocales();

      assertThat(languageLocales, Matchers.hasItems("en-US", "pl", "zh"));
   }

   @Test(dependsOnMethods = { "canCreateProjectAndVersion", "canAddLanguage" }, invocationTimeOut = Constants.FIFTY_SEC)
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

      log.info("document list table: {}", webTranPage.getDocumentListTableContent());
   }

}
