package org.zanata.page.administration;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ManageLanguageTeamMemberPage extends AbstractPage
{
   @FindBy(id = "memberPanel")
   private WebElement memberPanel;

   public static final int USERNAME_COLUMN = 0;

   public ManageLanguageTeamMemberPage(WebDriver driver)
   {
      super(driver);
   }

   private String getMembersInfo()
   {
      WebElement memberInfo = memberPanel.findElement(By.xpath(".//p"));
      return memberInfo.getText();
   }

   public List<String> getMemberUsernames()
   {
      if (getMembersInfo().contains("0 members"))
      {
         log.info("no members yet for this language");
         return Collections.emptyList();
      }
      WebElement languageTable = getDriver().findElement(By.id("memberPanel:threads"));
      List<TableRow> languageMembersTable = WebElementUtil.getTableRows(languageTable);
      List<String> usernameColumn = WebElementUtil.getColumnContents(languageMembersTable, USERNAME_COLUMN);
      log.info("username column: {}", usernameColumn);
      return usernameColumn;
   }

   public ManageLanguageTeamMemberPage joinLanguageTeam()
   {
      // Waiting 10 seconds for an element to be present on the page, checking
      // for its presence once every 1 second.
      WebElement joinLanguageTeamLink = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(By.linkText("Join Language Team"));
         }
      });
      joinLanguageTeamLink.click();
      // we need to wait for this join to finish before returning the page
      waitForTenSec().until(new Function<WebDriver, Boolean>()
      {
         @Override
         public Boolean apply(WebDriver driver)
         {
            List<WebElement> joinLanguageTeam = driver.findElements(By.linkText("Join Language Team"));
            return joinLanguageTeam.isEmpty();
         }
      });
      return new ManageLanguageTeamMemberPage(getDriver());
   }

   public ManageLanguageTeamMemberPage clickAddTeamMember()
   {
      WebElement addTeamMemberLink = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(By.id("addTeamMemberLink"));
         }
      });
      addTeamMemberLink.click();
      return this;
   }

   public List<TableRow> searchPerson(final String personName)
   {
      final WebElement addUserPanel = getDriver().findElement(By.id("userAddPanel_container"));

      WebElement searchInput = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         public WebElement apply(WebDriver driver)
         {
            return addUserPanel.findElement(By.id("searchForm:searchField"));
         }
      });
      searchInput.sendKeys(personName);
      WebElement searchButton = getDriver().findElement(By.id("searchForm:searchBtn"));
      searchButton.click();

      WebElement searchResultTable = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         @Override
         public WebElement apply(WebDriver driver)
         {
            WebElement table = driver.findElement(By.id("resultForm:personTable"));
            List<TableRow> tableRows = WebElementUtil.getTableRows(table);
            //we want to wait until search result comes back
            if (tableRows.isEmpty() || !tableRows.get(0).getCellContents().get(0).contains(personName))
            {
               log.debug("waiting for search result refresh...");
               return null;
            }
            return table;
         }
      });

      return WebElementUtil.getTableRows(searchResultTable);
   }

   public ManageLanguageTeamMemberPage addToTeam(TableRow personRow)
   {
      List<WebElement> cells = personRow.getCells();
      final String personUsername = personRow.getCellContents().get(0);
      log.info("username to be added: {}", personUsername);
      WebElement lastColumn = cells.get(cells.size() - 1);
      if (!lastColumn.getText().contains("Already in Team"))
      {
         WebElement addButton = lastColumn.findElement(By.xpath(".//input[@value='Add']"));
         addButton.click();
         WebElement closeButton = getDriver().findElement(By.id("searchForm:closeBtn"));
         closeButton.click();
         // we need to wait for the page to refresh
         waitForSeconds(getDriver(), 5).until(new Predicate<WebDriver>()
         {
            @Override
            public boolean apply(WebDriver driver)
            {
               WebElement languageTable = driver.findElement(By.id("memberPanel:threads"));
               List<TableRow> languageMembersTable = WebElementUtil.getTableRows(languageTable);
               List<String> usernameColumn = WebElementUtil.getColumnContents(languageMembersTable, USERNAME_COLUMN);
               log.info("username column: {}", usernameColumn);
               return usernameColumn.contains(personUsername);
            }
         });
         return new ManageLanguageTeamMemberPage(getDriver());
      }
      return this;
   }
}
