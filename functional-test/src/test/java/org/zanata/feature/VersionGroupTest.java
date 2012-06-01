package org.zanata.feature;

import java.util.List;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.page.ProjectVersionPage;
import org.zanata.page.VersionGroupPage;
import org.zanata.page.VersionGroupsPage;
import org.zanata.util.TableRow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test
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

   @Test(dependsOnMethods = "canCreateVersionGroup")
   public void canAddProjectVersionsToGroup() {
      // given two projects and versions are created
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      projectWorkFlow.createNewProject("group-project-a", "project to be grouped")
            .clickCreateVersionLink().inputVersionId("master").saveVersion();

      projectWorkFlow.createNewProject("group-project-b", "project with same version slug/version id")
            .clickCreateVersionLink().inputVersionId("master").saveVersion();

      VersionGroupPage versionGroupPage = projectWorkFlow.goToHome().goToGroups().goToGroup("group one");
      List<TableRow> searchResult = versionGroupPage.addProjectVersion().searchProject("group-project");
      log.info("come back {} rows in search result", searchResult.size());

      TableRow versionToBeAdd = searchResult.get(0);
      List<String> projectAndVersion = versionToBeAdd.getCellContents();
      VersionGroupPage groupPage = versionGroupPage.addToGroup(versionToBeAdd).closeSearchResult();

      List<List<String>> projectVersions = groupPage.getNotEmptyProjectVersionsInGroup();
      assertThat(projectVersions, Matchers.hasSize(1));
      assertThat(projectVersions.get(0), Matchers.equalTo(projectAndVersion));
   }

}
