package org.zanata.feature.versionGroup;

import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.HomePage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class VersionGroupTest
{

   private final ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
   private HomePage homePage;
   private VersionGroupPage versionGroupPage;

   @Before
   public void before()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   public VersionGroupsPage createNewVersionGroup(String groupId, String groupName)
   {
      VersionGroupsPage versionGroupsPage = homePage.goToGroups();
      List<String> groupNames = versionGroupsPage.getGroupNames();

      if (groupNames.contains(groupName))
      {
         return versionGroupsPage;
      }

      return versionGroupsPage.createNewGroup().inputGroupId(groupId).inputGroupName(groupName).saveGroup();
   }

   public void createProjectAndVersion(String projectId, String projectName, String version)
   {
      ProjectPage projectPage = projectWorkFlow.createNewProject(projectId, projectName);
      if (!projectPage.getVersions().contains(version))
      {
         projectPage.clickCreateVersionLink().inputVersionId(version).selectStatus("READONLY").selectStatus("ACTIVE").saveVersion();
      }
   }

   public List<TableRow> searchProjectToAddToVersionGroup(String groupName, String searchTerm)
   {
      versionGroupPage = projectWorkFlow.goToHome().goToGroups().goToGroup(groupName);
      versionGroupPage = versionGroupPage.addProjectVersion();
      return versionGroupPage.searchProject(searchTerm);
   }

   public VersionGroupPage addProjectToVersionGroup(List<TableRow> table, int row)
   {

      TableRow versionToBeAdd = table.get(row - 1);
      return versionGroupPage.addToGroup(versionToBeAdd).closeSearchResult();
   }

//   @Test
   public void canAddProjectVersionsToGroup() {
      // given two projects and versions are created
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      ProjectPage projectA = projectWorkFlow.createNewProject("group-project-a", "project a to be grouped");
      projectWorkFlow.createNewProjectVersion("project a to be grouped", "master");

      ProjectPage projectB = projectWorkFlow.createNewProject("group-project-b", "project b to be grouped");
      projectWorkFlow.createNewProjectVersion("project b to be grouped", "master");

      VersionGroupPage versionGroupPage = projectWorkFlow.goToHome().goToGroups().goToGroup("group one");
      List<TableRow> searchResult = versionGroupPage.addProjectVersion().searchProject("group");
      log.info("come back {} rows in search result", searchResult.size());

      //add first row from search result into group
      TableRow versionToBeAdd = searchResult.get(0);
      List<String> projectAndVersion = versionToBeAdd.getCellContents();
      assertThat(projectAndVersion.get(0), Matchers.equalTo("project a to be grouped"));

      VersionGroupPage groupPage = versionGroupPage.addToGroup(versionToBeAdd).closeSearchResult();

      List<List<String>> projectVersions = groupPage.getNotEmptyProjectVersionsInGroup();
      assertThat(projectVersions, Matchers.hasSize(1));
      assertThat(projectVersions.get(0), Matchers.equalTo(projectAndVersion));
   }

}
