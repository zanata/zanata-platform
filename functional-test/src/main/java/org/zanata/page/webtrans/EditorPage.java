package org.zanata.page.webtrans;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class EditorPage extends BasePage
{
   private final By glossaryTableBy = By.id("gwt-debug-glossaryResultTable");
   private final By glossaryNoResultBy = By.id("gwt-debug-glossaryNoResult");

   @FindBy(id = "gwt-debug-transUnitTable")
   private WebElement transUnitTable;

   public EditorPage(WebDriver driver)
   {
      super(driver);
   }

   public EditorPage searchGlossary(final String term)
   {
      waitForTenSec().until(new Predicate<WebDriver>()
      {
         @Override
         public boolean apply(WebDriver input)
         {
            return input.findElements(glossaryNoResultBy).size() == 1 || input.findElements(glossaryTableBy).size() == 1;
         }
      });
      WebElement searchBox = getDriver().findElement(By.id("gwt-debug-glossaryTextBox"));
      searchBox.clear();
      searchBox.sendKeys(term);
      WebElement searchButton = getDriver().findElement(By.id("gwt-debug-glossarySearchButton"));
      searchButton.click();
      return this;
   }

   /**
    * First row is header: SourceTerm, TargetTerm, Action, Details.
    *
    * @return a table representing the searchResultTable
    */
   public List<List<String>> getGlossaryResultTable()
   {
      return waitForTenSec().until(new Function<WebDriver, List<List<String>>>()
      {
         @Override
         public List<List<String>> apply(WebDriver input)
         {
            if (input.findElements(glossaryNoResultBy).size() == 1)
            {
               return Collections.emptyList();
            }
            List<List<String>> resultTable = WebElementUtil.getTwoDimensionList(input, glossaryTableBy);
            log.info("glossary result: {}", resultTable);
            return resultTable;
         }
      });
   }


}
