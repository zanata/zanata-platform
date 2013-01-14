package org.zanata.feature;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.LoggingTooltipExtension;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.util.Constants;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class, LoggingTooltipExtension.class})
public class PushPodirPluralProjectTest
{
   private final static Logger log = Logger.getLogger(PushPodirPluralProjectTest.class.getName());

   @Extension
   public ConcordionExtension extension = new LoggingTooltipExtension(PushPodirPluralProjectTest.class.getName(), Level.INFO, false);

   private ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
   private int exitCode;
   private String project;

   public String projectRootPath(String project)
   {
      log.info("blah blah");
      this.project = project;
      return clientPushWorkFlow.getProjectRootPath(project).getAbsolutePath();
   }

   @Test(timeout = 10)
   public void push()
   {
      exitCode = clientPushWorkFlow.mvnPush(project);
   }

   public boolean pushSucceed()
   {
      return exitCode == 0;
   }


   public void canPush() throws IOException
   {
//      canCreateProjectAndVersion("plurals", "master", "plural project");
//      canAddLanguage("en-US");
      int exitCode = clientPushWorkFlow.mvnPush("plural");

      assertThat(exitCode, Matchers.equalTo(0));

      ProjectVersionPage projectVersionPage = new ProjectWorkFlow().goToProjectByName("plural project").goToVersion("master");
      assertThat(projectVersionPage.getTranslatableLocales(), Matchers.hasItems("en-US", "pl", "zh"));
   }
}
