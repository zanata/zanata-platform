package org.zanata.feature;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.util.TableRow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class VersionGroupTest
{

   @Test
   public void canCreateVersionGroup() {
      VersionGroupsPage versionGroupsPage = new LoginWorkFlow().signIn("admin", "admin").goToGroups();

      List<String> groupNames = versionGroupsPage.getGroupNames();

      assertThat(groupNames, Matchers.<String>emptyIterable());

      versionGroupsPage = versionGroupsPage.createNewGroup().inputGroupId("group1").inputGroupName("group one").saveGroup();

      groupNames = versionGroupsPage.getGroupNames();

      assertThat(groupNames, Matchers.contains("group one"));
   }

   @Test
   public void canAddProjectVersionsToGroup() {
      canCreateVersionGroup();
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
