/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.versionGroup;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.utility.DashboardPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class })
@Category(ConcordionTest.class)
public class VersionGroupBasicTest
{

   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();
   private final ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
   private DashboardPage dashboardPage;
   private VersionGroupPage versionGroupPage;

   @Before
   public void before()
   {
      dashboardPage = new LoginWorkFlow().signIn("admin", "admin");
   }

   public VersionGroupsPage createNewVersionGroup(String groupId, String groupName, String groupDesc, String groupStatus)
   {
      VersionGroupsPage versionGroupsPage = dashboardPage.goToGroups();
      return versionGroupsPage.createNewGroup().inputGroupId(groupId).inputGroupName(groupName)
            .inputGroupDescription(groupDesc).selectStatus(groupStatus).saveGroup();
   }

   public CreateVersionGroupPage groupIDAlreadyExists(String groupId, String groupName, String groupDesc,
         String groupStatus)
   {
      VersionGroupsPage versionGroupsPage = dashboardPage.goToGroups();
      List<String> groupNames = versionGroupsPage.getGroupNames();
      assertThat("Group does not exist, preconditions not met", groupNames.contains(groupName));
      return versionGroupsPage.createNewGroup().inputGroupId(groupId).inputGroupName(groupName)
            .inputGroupDescription(groupDesc).selectStatus(groupStatus).saveGroupFailure();
   }

   public CreateVersionGroupPage invalidCharacters(String groupId)
   {
      VersionGroupsPage versionGroupsPage = dashboardPage.goToGroups();
      // we toggle the status here to trigger and wait for the validation of group id to happen
      return versionGroupsPage.createNewGroup().inputGroupId(groupId).selectStatus("OBSOLETE").selectStatus("ACTIVE");
   }

   public boolean groupNamesContain(VersionGroupsPage versionGroupsPage, String groupName)
   {
      return versionGroupsPage.getGroupNames().contains(groupName);
   }

   /**
    * This assumes there will be ONE error on screen.
    *
    * @param createVersionGroupPage page
    * @return the error
    */
   public String getFirstGroupsError(CreateVersionGroupPage createVersionGroupPage)
   {
      return createVersionGroupPage.getErrors(1).get(0);
   }

   public VersionGroupsPage toggleObsolete(VersionGroupsPage versionGroupsPage)
   {
      return versionGroupsPage.toggleObsolete(true);
   }

   public VersionGroupsPage groups()
   {
      VersionGroupsPage versionGroupsPage = projectWorkFlow.goToHome().goToGroups();
      return versionGroupsPage;
   }

   public void createProjectAndVersion(String projectId, String projectName, String version)
   {
      ProjectPage projectPage = projectWorkFlow.createNewProject(projectId, projectName);
      projectPage.clickCreateVersionLink().inputVersionId(version).selectStatus("READONLY").selectStatus("ACTIVE")
            .saveVersion();
   }

   public List<List<String>> searchProjectToAddToVersionGroup(String searchTerm)
   {
      versionGroupPage = versionGroupPage.addProjectVersion();
      return versionGroupPage.searchProject(searchTerm, 2);
   }

   public VersionGroupPage addProjectToVersionGroup(int row)
   {
      return versionGroupPage.addToGroup(row - 1).closeSearchResult(1);
   }

   public void clickGroupName(VersionGroupsPage groupsPage, String groupName)
   {
      versionGroupPage = groupsPage.goToGroup(groupName);
   }
}
