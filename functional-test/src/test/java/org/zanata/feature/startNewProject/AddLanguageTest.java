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
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.administration.ManageLanguageTeamMemberPage;
import org.zanata.workflow.LoginWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
public class AddLanguageTest
{
   private HomePage homePage;
   private ManageLanguagePage manageLanguagePage;

   @Before
   public void beforeMethod()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   public ManageLanguagePage goToManageLanguagePage()
   {
      manageLanguagePage = homePage.goToAdministration().goToManageLanguagePage();
      return manageLanguagePage;
   }

   public ManageLanguagePage addNewLanguage(String locale)
   {
      List<String> locales = manageLanguagePage.getLanguageLocales();
      if (locales.contains(locale))
      {
         log.warn("{} has already been added, enabling by default", locale);
         manageLanguagePage = manageLanguagePage.enableLanguageByDefault(locale);
      }
      else
      {
         //continue to add the new language
         manageLanguagePage = manageLanguagePage.addNewLanguage().enableLanguageByDefault().inputLanguage(locale).saveLanguage();
      }
      return manageLanguagePage;
   }

   public ManageLanguageTeamMemberPage joinLanguageAsAdmin(String locale)
   {
      ManageLanguageTeamMemberPage teamMemberPage = manageLanguagePage.manageTeamMembersFor(locale);
      if (teamMemberPage.getMemberUsernames().contains("admin"))
      {
         log.warn("admin has already joined the language [{}]", locale);
         return teamMemberPage;
      }
      else
      {
         return teamMemberPage.joinLanguageTeam();
      }
   }
}
