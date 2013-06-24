package org.zanata.page.webtrans;

import java.util.List;

import javax.annotation.Nullable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class EditorPage extends AbstractPage
{
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
            return getDriver().findElement(By.id("gwt-debug-glossaryResultTable")).isDisplayed();
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
            List<List<String>> resultTable = WebElementUtil.getTwoDimensionList(input, By.id("gwt-debug-glossaryResultTable"));
            log.info("glossary result: {}", resultTable);
            return resultTable;
         }
      });
   }


}
