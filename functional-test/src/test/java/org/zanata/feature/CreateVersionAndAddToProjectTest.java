package org.zanata.feature;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.HomePage;
import org.zanata.page.projects.CreateVersionPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.workflow.LoginWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class CreateVersionAndAddToProjectTest
{
   private HomePage homePage;

   @Before
   public void beforeMethod()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }
   public ProjectPage createNewProjectVersion(String projectName, String versionSlug)
   {
      ProjectsPage projectsPage = homePage.goToProjects();
      ProjectPage projectPage = projectsPage.goToProject(projectName);
      if (projectPage.getVersions().contains(versionSlug))
      {
         log.warn("{} has already been created. Presumably you are running test manually and more than once.", versionSlug);
         return projectPage;
      }
      else
      {
         CreateVersionPage createVersionPage = projectPage.clickCreateVersionLink().inputVersionId(versionSlug);
         createVersionPage.selectStatus("READONLY");
         createVersionPage.selectStatus("ACTIVE");
         ProjectVersionPage projectVersionPage = createVersionPage.saveVersion();
         projectsPage = projectVersionPage.goToPage("Projects", ProjectsPage.class);
         return projectsPage.goToProject(projectName);
      }
   }
}
