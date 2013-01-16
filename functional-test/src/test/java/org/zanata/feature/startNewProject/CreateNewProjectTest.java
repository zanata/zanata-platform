package org.zanata.feature.startNewProject;

import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.HomePage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.workflow.LoginWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class CreateNewProjectTest
{

   private HomePage homePage;

   @Before
   public void beforeMethod()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   public ProjectPage createNewProject(String projectSlug, String projectName)
   {
      ProjectsPage projectsPage = homePage.goToProjects();
      List<String> projects = projectsPage.getProjectNamesOnCurrentPage();
      CreateNewProjectTest.log.info("current projects: {}", projects);

      if (projects.contains(projectName))
      {
         CreateNewProjectTest.log.warn("{} has already been created. Presumably you are running test manually and more than once.", projectSlug);
         //since we can't create same project multiple times,
         //if we run this test more than once manually, we don't want it to fail
         return projectsPage.goToProject(projectName);
      }
      else
      {
         return projectsPage.clickOnCreateProjectLink().inputProjectId(projectSlug).inputProjectName(projectName).saveProject();
      }
   }
}
