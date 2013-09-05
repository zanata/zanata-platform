package org.zanata.page.groups;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;


import java.util.List;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class VersionGroupPage extends BasePage
{
   @FindBy(linkText = "Add Project Versions")
   private WebElement addProjectVersionsLink;

   @FindBy(id = "versionAddPanel_container")
   private WebElement addProjectVersionPanel;

   @FindBy(id = "iterationGroupForm:iterationsDataTable")
   private WebElement groupDataTable;
   
   private By versionsInGroupTableBy = By.id("iterationGroupForm:iterationsDataTable");

   public VersionGroupPage(final WebDriver driver)
   {
      super(driver);
   }

   public VersionGroupPage addProjectVersion()
   {
      addProjectVersionsLink.click();
      waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         @Override
         public WebElement apply(WebDriver driver)
         {
            return addProjectVersionPanel.findElement(By.id("projectVersionSearch:searchForm:searchField"));
         }
      });
      return new VersionGroupPage(getDriver());
   }

   public List<List<String>> searchProject(final String projectName, final int expectedResultNum)
   {
      WebElement searchField = addProjectVersionPanel.findElement(By.id("projectVersionSearch:searchForm:searchField"));
      searchField.sendKeys(projectName);

      WebElement searchButton = addProjectVersionPanel.findElement(By.id("projectVersionSearch:searchForm:searchBtn"));
      searchButton.click();

      final By tableBy = By.id("projectVersionSearch:searchResults:resultTable");

      return refreshPageUntil(this, new Function<WebDriver, List<List<String>>>()
      {
         @Override
         public List<List<String>> apply(WebDriver driver)
         {
            // we want to wait until search result comes back. There is no way we can tell whether search result has come back and table refreshed.
            // To avoid the org.openqa.selenium.StaleElementReferenceException (http://seleniumhq.org/exceptions/stale_element_reference.html),
            // we have to set expected result num

            List<List<String>> tableContents = WebElementUtil.getTwoDimensionList(getDriver(), tableBy);

            if (tableContents.size() != expectedResultNum)
            {
               log.debug("waiting for search result refresh...");
               return null;
            }
            return tableContents;
         }
      });
   }

   public VersionGroupPage addToGroup(int rowIndex)
   {
      WebElement table = addProjectVersionPanel.findElement(By.id("projectVersionSearch:searchResults:resultTable"));

      List<WebElement> cells = WebElementUtil.getTableRows(getDriver(), table).get(rowIndex).getCells();
      WebElement actionCell = cells.get(cells.size() - 1);
      if (!actionCell.getText().contains("Already in Group"))
      {
         WebElement selectCheckBox = actionCell.findElement(By.xpath(".//input[@type='checkbox']"));
         if (!selectCheckBox.isSelected())
         {
            selectCheckBox.click();
         }
      }

      WebElement addSelected = addProjectVersionPanel.findElement(By.id("projectVersionSearch:searchResults:addSelectedBtn"));
      addSelected.click();
      return this;
   }

   public VersionGroupPage closeSearchResult(final int expectedRow)
   {
      WebElement closeButton = addProjectVersionPanel.findElement(By.id("projectVersionSearch:searchForm:closeBtn"));
      closeButton.click();
      return refreshPageUntil(this, new Predicate<WebDriver>()
      {
         @Override
         public boolean apply(@javax.annotation.Nullable WebDriver input)
         {
            List<TableRow> tableRows = WebElementUtil.getTableRows(input, versionsInGroupTableBy);
            int size = tableRows.size();
            log.info("table rows: {}", tableRows);
            return size == expectedRow;
         }
      });
   }

   @SuppressWarnings("unused")
   public List<List<String>> getProjectVersionsInGroup()
   {
      return WebElementUtil.getTwoDimensionList(getDriver(), versionsInGroupTableBy);
   }
   
   public String getProjectName(int row)
   {
      List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(), versionsInGroupTableBy);
      WebElement projectLink = tableRows.get(row).getCells().get(0).findElement(By.tagName("a"));
      return projectLink.getText();
   }
   
   public String getProjectVersionName(int row)
   {
      List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(), versionsInGroupTableBy);
      WebElement versionLink = tableRows.get(row).getCells().get(1).findElement(By.tagName("a"));
      return versionLink.getText();
   }
   
   
   public ProjectPage clickOnProjectLinkOnRow(int row)
   {
      List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(), versionsInGroupTableBy);
      WebElement projectLink = tableRows.get(row).getCells().get(0).findElement(By.tagName("a"));
      projectLink.click();
      return new ProjectPage(getDriver());
   }

   public ProjectVersionPage clickOnProjectVersionLinkOnRow(int row)
   {
      List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(), versionsInGroupTableBy);
      WebElement versionLink = tableRows.get(row).getCells().get(1).findElement(By.tagName("a"));
      versionLink.click();
      return new ProjectVersionPage(getDriver());
   }
}
