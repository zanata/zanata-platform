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
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class CreateNewProjectTest
{

   @Before
   public void beforeMethod()
   {
      new LoginWorkFlow().signIn("admin", "admin");
   }

   public ProjectPage createNewProject(String projectSlug, String projectName)
   {
      return new ProjectWorkFlow().createNewProject(projectSlug, projectName);
   }
}
