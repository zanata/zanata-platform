package org.zanata.page.administration;

import com.google.common.base.Predicate;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.Checkbox;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ManageSearchPage extends BasePage {
    private By classesTableBy = By.id("form:classList");

    public ManageSearchPage(WebDriver driver) {
        super(driver);
    }


    public ManageSearchPage selectAllActionsFor(String clazz) {
        List<TableRow> tableRows =
            WebElementUtil.getTableRows(getDriver(), classesTableBy);
        for (TableRow tableRow : tableRows) {
            if (tableRow.getCellContents().contains(clazz)) {
                WebElement allActionsChkBox =
                    tableRow.getCells().get(0).findElement(By.tagName("input"));
                Checkbox.of(allActionsChkBox).check();
            }
        }

        return this;
    }

    public ManageSearchPage performSelectedActions() {
        getDriver().findElement(By.id("form:reindex")).click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // once the button re-appears, it means the reindex is done.
                input.findElement(By.id("form:reindex"));
                return true;
            }
        });
        return this;
    }
}
