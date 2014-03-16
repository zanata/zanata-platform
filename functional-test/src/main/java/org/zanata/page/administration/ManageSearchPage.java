package org.zanata.page.administration;

import com.google.common.base.Predicate;
import java.util.List;

import com.google.common.base.Throwables;
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
    private static final int SELECT_ALL_COLUMN = 0;
    private By classesTableBy = By.id("form:classList");
    private By abortButtonBy = By.id("form:cancel");


    public ManageSearchPage(WebDriver driver) {
        super(driver);
    }


    public ManageSearchPage selectAllActionsFor(String clazz) {
        List<TableRow> tableRows =
            WebElementUtil.getTableRows(getDriver(), classesTableBy);
        for (TableRow tableRow : tableRows) {
            if (tableRow.getCellContents().contains(clazz)) {
                WebElement allActionsChkBox =
                    tableRow.getCells().get(SELECT_ALL_COLUMN).findElement(By.tagName("input"));
                Checkbox.of(allActionsChkBox).check();
            }
        }

        return this;
    }

    public ManageSearchPage clickSelectAll() {
        getDriver().findElement(By.id("form:selectAll")).click();
        // It seems that if the Select All and Perform buttons are clicked too
        // quickly in succession, the operation will fail
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            throw Throwables.propagate(ie);
        }
        return new ManageSearchPage(getDriver());
    }

    public boolean allActionsSelected() {
        List<TableRow> tableRows =
                WebElementUtil.getTableRows(getDriver(), classesTableBy);
        for (TableRow tableRow : tableRows) {
            // column 2, 3, 4 are checkboxes for purge, reindex and optimize
            for (int i = 2; i <= 4; i++) {
                WebElement checkBox = tableRow.getCells().get(i).findElement(By.tagName("input"));
                if (!Checkbox.of(checkBox).checked()) {
                    return false;
                }
            }
        }
        return true;
    }

    public ManageSearchPage performSelectedActions() {
        getDriver().findElement(By.id("form:reindex")).click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // The Abort button will display
                return input.findElement(By.id("form:cancel")).isDisplayed();
            }
        });
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage waitForActionsToFinish() {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // once the button re-appears, it means the reindex is done.
                return input.findElement(By.id("form:reindex")).isDisplayed();
            }
        });
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage abort() {
        getDriver().findElement(abortButtonBy).click();
        return new ManageSearchPage(getDriver());
    }


    public boolean noOperationsRunningIsDisplayed() {
        return getDriver().findElement(By.id("noOperationsRunning")).isDisplayed();
    }

    public boolean completedIsDisplayed() {
        return getDriver().findElement(By.id("completed")).isDisplayed();
    }

    public boolean abortedIsDisplayed() {
        return getDriver().findElement(By.id("aborted")).isDisplayed();
    }

}
