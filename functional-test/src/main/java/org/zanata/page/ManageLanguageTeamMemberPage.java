package org.zanata.page;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ManageLanguageTeamMemberPage extends AbstractPage
{
   @FindBy(xpath = "//div[contains(@id, 'memberPanel_body')]")
   private WebElement memberPanelBody;

   public static final int USERNAME_COLUMN = 0;

   public ManageLanguageTeamMemberPage(WebDriver driver)
   {
      super(driver);
   }

   public String getLanguage()
   {
      WebElement language = memberPanelBody.findElement(By.xpath(".//h1"));
      return language.getText();
   }

   public String getMembersInfo()
   {
      WebElement memberInfo = memberPanelBody.findElement(By.xpath(".//p"));
      return memberInfo.getText();
   }

   public List<String> getMemberUsernames()
   {
      if (getMembersInfo().contains("0 members"))
      {
         log.info("no members yet for this language");
         return Collections.emptyList();
      }
      List<TableRow> languageMembersTable = WebElementUtil.getTableRows(getDriver(), By.xpath("//table"));
      return WebElementUtil.getColumnContents(languageMembersTable, USERNAME_COLUMN);
   }
}
