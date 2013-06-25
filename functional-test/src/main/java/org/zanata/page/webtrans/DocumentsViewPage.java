package org.zanata.page.webtrans;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DocumentsViewPage extends AbstractPage
{
   @FindBy(id = "gwt-debug-documentListTable")
   private WebElement documentListTable;

   public DocumentsViewPage(final WebDriver driver)
   {
      super(driver);
   }

   public List<List<String>> getDocumentListTableContent()
   {
      return WebElementUtil.getTwoDimensionList(getDriver(), By.id("gwt-debug-documentListTable"));
   }

}
