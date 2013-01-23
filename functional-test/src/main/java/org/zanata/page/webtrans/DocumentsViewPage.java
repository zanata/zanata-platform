package org.zanata.page.webtrans;

import java.util.List;

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
   @FindBy(className = "DocumentListTable")
   private WebElement documentListTable;

   private List<TableRow> documentRows;

   public DocumentsViewPage(final WebDriver driver)
   {
      super(driver);
      cacheDocumentRows();
   }

   public List<List<String>> getDocumentListTableContent()
   {
      return WebElementUtil.transformToTwoDimensionList(documentRows);
   }

   private void cacheDocumentRows()
   {
      List<TableRow> tableRows = WebElementUtil.getTableRows(documentListTable);
      //GWT gives us some extra rows
      Iterable<TableRow> documentRows = Iterables.filter(tableRows, new Predicate<TableRow>()
      {
         @Override
         public boolean apply(TableRow input)
         {
            return input.getCellContents().size() > 5;
         }
      });
      this.documentRows = ImmutableList.copyOf(documentRows);
   }
}
