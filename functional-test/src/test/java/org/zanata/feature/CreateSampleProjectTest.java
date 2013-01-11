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

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.administration.ManageLanguageTeamMemberPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.webtrans.WebTranPage;
import org.zanata.util.TableRow;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LanguageWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class})
public class CreateSampleProjectTest
{

   private final ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
   private final LanguageWorkFlow languageWorkFlow = new LanguageWorkFlow();

   @Before
   public void beforeMethod()
   {
      new LoginWorkFlow().signIn("admin", "admin");
   }

   public ProjectPage createNewProject(String projectSlug, String projectName)
   {
      return projectWorkFlow.createNewProject(projectSlug, projectName);
   }

   public ProjectPage createNewProjectVersion(String projectName, String versionSlug)
   {
      projectWorkFlow.createNewProjectVersion(projectName, versionSlug);
      return projectWorkFlow.goToProjectByName(projectName);
   }

   public List<String> getLanguages()
   {
      return languageWorkFlow.goToHome().goToAdministration().goToManageLanguagePage().getLanguageLocales();
   }

   public void addNewLanguage(String locale)
   {
      languageWorkFlow.addLanguage(locale);
   }

   public List<String> manageLanguage(String locale)
   {
      return languageWorkFlow.goToHome().goToAdministration().goToManageLanguagePage().manageTeamMembersFor(locale).getMemberUsernames();
   }

   public void joinLanguageAsAdmin(String locale)
   {
      languageWorkFlow.goToHome().goToAdministration().goToManageLanguagePage().manageTeamMembersFor(locale).joinLanguageTeam();
   }

   public void addToLanguage(String person, String locale)
   {
      ManageLanguageTeamMemberPage teamMemberPage = languageWorkFlow.goToHome().goToAdministration().goToManageLanguagePage().manageTeamMembersFor(locale).clickAddTeamMember();
      List<TableRow> searchResult = teamMemberPage.searchPerson(person);
      if (searchResult.size() > 0)
      {
         teamMemberPage.addToTeam(searchResult.get(0));
      }
      else
      {
         throw new RuntimeException(person + " not found");
      }
   }

//   @Test(expected = RuntimeException.class)
   public void cannotCreateProjectWithSameProjectId() {
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      ProjectPage projectPage = projectWorkFlow.createNewProject("project-a", "project a");
      assertThat(projectPage.getTitle(), Matchers.containsString("Zanata:project a"));

      //second time
      projectWorkFlow.createNewProject("project-a", "project with same slug/project id");
   }

//   @Test
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

//   @Test(timeout = Constants.FIFTY_SEC)
   public void canPush() throws IOException
   {
//      canCreateProjectAndVersion("plurals", "master", "plural project");
//      canAddLanguage("en-US");
      ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
      int exitCode = clientPushWorkFlow.mvnPush("plural");

      assertThat(exitCode, Matchers.equalTo(0));

      ProjectVersionPage projectVersionPage = new ProjectWorkFlow().goToProjectByName("plural project").goToVersion("master");
      assertThat(projectVersionPage.getTranslatableLocales(), Matchers.hasItems("en-US", "pl", "zh"));
   }

//   @Test
   public void canSeeDocumentList() throws IOException
   {
      canPush();
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectVersionPage projectVersionPage = new ProjectWorkFlow().goToProjectByName("plural project").goToVersion("master");
      WebTranPage webTranPage = projectVersionPage.translate("pl");

      log.info("document list table: {}", webTranPage.getDocumentListTableContent());
   }

}
