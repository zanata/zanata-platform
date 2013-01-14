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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ManageLanguageTeamMemberPage extends AbstractPage
{
   @FindBy(id = "main_body_content")
   private WebElement memberPanelBody;

   public static final int USERNAME_COLUMN = 0;

   public ManageLanguageTeamMemberPage(WebDriver driver)
   {
      super(driver);
   }

   private String getMembersInfo()
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
      List<TableRow> languageMembersTable = WebElementUtil.getTableRows(memberPanelBody.findElement(By.xpath(".//table")));
      return WebElementUtil.getColumnContents(languageMembersTable, USERNAME_COLUMN);
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
            return driver.findElement(By.linkText("Add Team Member"));
         }
      });
      addTeamMemberLink.click();
      return this;
   }

   public List<TableRow> searchPerson(final String personName)
   {
      WebElement searchInput = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(By.xpath("//input[@type='text' and contains(@id, 'personSearch')]"));
         }
      });
      searchInput.sendKeys(personName);
      WebElement searchButton = getDriver().findElement(By.xpath("//input[@type='button' and @value='Search']"));
      searchButton.click();

//      final WebElement searchResultDiv = getDriver().findElement(By.id("personSearch:searchResults"));
      WebElement searchResultTable = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         @Override
         public WebElement apply(WebDriver driver)
         {
            WebElement table = driver.findElement(By.xpath("//table[contains(@id, ':personTable')]"));
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
      WebElement lastColumn = cells.get(cells.size() - 1);
      if (!lastColumn.getText().contains("Already in Team"))
      {
         WebElement addButton = lastColumn.findElement(By.xpath(".//input[@value='Add']"));
         addButton.click();
         WebElement closeButton = getDriver().findElement(By.xpath("//input[@type='button' and @value='Search']"));
         closeButton.click();
         return new ManageLanguageTeamMemberPage(getDriver());
      }
      return this;
   }
}
