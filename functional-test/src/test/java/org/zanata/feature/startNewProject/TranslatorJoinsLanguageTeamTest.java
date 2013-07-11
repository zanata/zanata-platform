package org.zanata.feature.startNewProject;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.page.HomePage;
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.administration.ManageLanguageTeamMemberPage;
import org.zanata.util.TableRow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class TranslatorJoinsLanguageTeamTest
{
   private HomePage homePage;
   private ManageLanguagePage manageLanguagePage;
   private ManageLanguageTeamMemberPage manageLanguageTeamMemberPage;

   @Before
   public void beforeMethod()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   public void goToManageLanguagePage()
   {
      manageLanguagePage = homePage.goToAdministration().goToManageLanguagePage();
   }

   public ManageLanguageTeamMemberPage manageLanguage(String locale)
   {
      manageLanguageTeamMemberPage = manageLanguagePage.manageTeamMembersFor(locale);
      return manageLanguageTeamMemberPage;
   }

   public ManageLanguageTeamMemberPage addToLanguage(String person)
   {
      ManageLanguageTeamMemberPage teamMemberPage = manageLanguageTeamMemberPage.clickAddTeamMember();
      List<TableRow> searchResult = teamMemberPage.searchPerson(person);
      return teamMemberPage.addToTeam(searchResult.get(0));
   }

}
