package org.zanata.feature.versionGroup;

import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.HomePage;
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
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class VersionGroupBasicTest
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

   public VersionGroupsPage groups()
   {
      VersionGroupsPage versionGroupsPage = projectWorkFlow.goToHome().goToGroups();
      log.info("title is {}", versionGroupsPage.getTitle());
      return versionGroupsPage;
   }

   public void clickGroupName(VersionGroupsPage groupsPage, String groupName)
   {
      versionGroupPage = groupsPage.goToGroup(groupName);
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

}
