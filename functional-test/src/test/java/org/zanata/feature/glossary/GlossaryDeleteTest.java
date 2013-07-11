package org.zanata.feature.glossary;

import java.io.File;
import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.base.Joiner;

/**
 * @see <a href="https://tcms.engineering.redhat.com/case/167899/">TCMS case</a>
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class GlossaryDeleteTest
{
   private ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
   private File projectRootPath;
   private EditorPage editorPage;

   public String getUserConfigPath()
   {
      return ClientPushWorkFlow.getUserConfigPath("glossaryadmin");
   }

   public String getProjectLocation(String project)
   {
      projectRootPath = clientPushWorkFlow.getProjectRootPath(project);
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

   public void translate(String locale)
   {

      new LoginWorkFlow().signIn("admin", "admin");
      editorPage = new BasicWorkFlow().goToPage("webtrans/translate?project=about-fedora&iteration=master&localeId=" + locale + "&locale=en#view:doc;doc:About_Fedora", EditorPage.class);
   }

   public void searchGlossary(String term)
   {
      editorPage.searchGlossary(term);
   }

   public boolean hasNoResult()
   {
      return editorPage.getGlossaryResultTable().isEmpty();
   }

   public String getFirstResult()
   {
      // 2 row 2 column is glossary target
      return editorPage.getGlossaryResultTable().get(1).get(1);
   }
}
