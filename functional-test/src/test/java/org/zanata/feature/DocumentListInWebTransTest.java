package org.zanata.feature;

import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.HomePage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.webtrans.DocumentsViewPage;
import org.zanata.workflow.LoginWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class DocumentListInWebTransTest
{

   private HomePage homePage;
   private ProjectVersionPage projectVersionPage;

   public boolean signInAs(String username, String password)
   {
      homePage = new LoginWorkFlow().signIn(username, password);

      return homePage.hasLoggedIn();
   }

   public ProjectVersionPage goToProjectVersion(String projectName, String versionSlug)
   {
      ProjectPage projectPage = homePage.goToProjects().goToProject(projectName);
      projectVersionPage = projectPage.goToVersion(versionSlug);
      return projectVersionPage;
   }

   public DocumentsViewPage translate(String locale)
   {
      return projectVersionPage.translate(locale);
   }

   public List<List<String>> getDocumentListTableContents(DocumentsViewPage documentsViewPage)
   {
      return documentsViewPage.getDocumentListTableContent();
   }

   public String getTableCellValue(List<List<String>> table, int row, int column)
   {
      return table.get(row - 1).get(column - 1);
   }
}
