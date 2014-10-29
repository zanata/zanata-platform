package org.zanata.page.administration;

import com.google.common.base.Predicate;
import java.util.List;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ManageSearchPage extends BasePage {

    private static final int SELECT_ALL_COLUMN = 0;

    private By classesTable = By.id("form:classList");
    private By abortButton = By.id("form:cancel");
    private By selectAllButton = By.id("form:selectAll");
    private By performButton = By.id("form:reindex");
    private By cancelButton = By.id("form:cancel");
    private By noOpsLabel = By.id("noOperationsRunning");
    private By abortedLabel = By.id("aborted");
    private By completedLabel = By.id("completed");

    public ManageSearchPage(WebDriver driver) {
        super(driver);
    }

    public ManageSearchPage selectAllActionsFor(String clazz) {
        List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(),
                waitForWebElement(classesTable));
        for (TableRow tableRow : tableRows) {
            if (tableRow.getCellContents().contains(clazz)) {
                WebElement allActionsChkBox =
                    tableRow.getCells().get(SELECT_ALL_COLUMN).findElement(By.tagName("input"));
                Checkbox.of(allActionsChkBox).check();
            }
        }

        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage clickSelectAll() {
        log.info("Click Select All");
        waitForWebElement(selectAllButton).click();
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
        log.info("Query all actions selected");
        List<TableRow> tableRows =
                WebElementUtil.getTableRows(getDriver(), classesTable);
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
        log.info("Click Perform Actions");
        waitForWebElement(performButton).click();
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // The Abort button will display
                return waitForWebElement(cancelButton).isDisplayed();
            }
        });
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage waitForActionsToFinish() {
        log.info("Wait: all actions are finished");
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // once the button re-appears, it means the reindex is done.
                return waitForWebElement(performButton).isDisplayed();
            }
        });
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage abort() {
        log.info("Click Abort");
        waitForWebElement(abortButton).click();
        return new ManageSearchPage(getDriver());
    }


    public boolean noOperationsRunningIsDisplayed() {
        log.info("Query No Operations");
        return waitForWebElement(noOpsLabel).isDisplayed();
    }

    public boolean completedIsDisplayed() {
        log.info("Query is action completed");
        return waitForWebElement(completedLabel).isDisplayed();
    }

    public boolean abortedIsDisplayed() {
        log.info("Query is action aborted");
        return waitForWebElement(abortedLabel).isDisplayed();
    }

}
