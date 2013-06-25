package org.zanata.feature.startNewProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.LoggingTooltipExtension;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.workflow.ClientPushWorkFlow;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.util.concurrent.SimpleTimeLimiter;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class, LoggingTooltipExtension.class})
public class PushPodirPluralProjectTest
{
   private final static Logger tooltipLog = Logger.getLogger(PushPodirPluralProjectTest.class.getName());

   @Extension
   public ConcordionExtension extension = new LoggingTooltipExtension(PushPodirPluralProjectTest.class.getName(), Level.INFO, false);

   private ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
   private File projectRootPath;

   public String getUserConfigPath()
   {
      return ClientPushWorkFlow.getUserConfigPath("admin");
   }

   public String getUserConfigContent(String path) throws IOException
   {
      List<String> lines = Files.readLines(new File(path), Charset.defaultCharset());
      return Joiner.on("\n").join(lines);
   }

   public String getProjectLocation(String project)
   {
      projectRootPath = clientPushWorkFlow.getProjectRootPath(project);
      tooltipLog.info("project root path:" + projectRootPath.getAbsolutePath());
      return projectRootPath.getAbsolutePath();
   }

   public List<String> push(String command, String configPath) throws Exception
   {
      return clientPushWorkFlow.callWithTimeout(projectRootPath, command + configPath);
   }



   public boolean isPushSuccessful(List<String> output)
   {
      return clientPushWorkFlow.isPushSuccessful(output);
   }

   public String resultByLines(List<String> output)
   {
      return Joiner.on("\n").join(output);
   }

}
