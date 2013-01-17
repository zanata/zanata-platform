package org.zanata.feature.startNewProject;

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
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class CreateVersionAndAddToProjectTest
{

   @Before
   public void beforeMethod()
   {
      new LoginWorkFlow().signIn("admin", "admin");
   }
   public ProjectPage createNewProjectVersion(String projectName, String versionSlug)
   {
      ProjectVersionPage projectVersionPage = new ProjectWorkFlow().createNewProjectVersion(projectName, versionSlug);
      ProjectsPage projectsPage = projectVersionPage.goToPage("Projects", ProjectsPage.class);
      return projectsPage.goToProject(projectName);
   }
}
