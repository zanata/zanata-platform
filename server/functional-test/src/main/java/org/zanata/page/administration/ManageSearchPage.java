/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
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
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ManageSearchPage.class);
    private static final int SELECT_ALL_COLUMN = 0;
    private By classesTable = By.id("form:actions");
    private By abortButton = By.id("form:cancel");
    private By selectAllButton = By.id("form:selectAll");
    private By performButton = By.id("form:reindex");
    private By cancelButton = By.linkText("Abort");
    private By noOpsLabel = By.id("noOperationsRunning");
    private By abortedLabel = By.id("aborted");
    private By completedLabel = By.id("completed");

    public ManageSearchPage(WebDriver driver) {
        super(driver);
    }

    public ManageSearchPage selectAllActionsFor(String clazz) {
        List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(),
                readyElement(classesTable));
        for (TableRow tableRow : tableRows) {
            if (tableRow.getCellContents().contains(clazz)) {
                WebElement allActionsChkBox =
                        tableRow.getCells().get(SELECT_ALL_COLUMN)
                                .findElement(By.tagName("input"));
                Checkbox.of(allActionsChkBox).check();
            }
        }
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage clickSelectAll() {
        log.info("Click Select All");
        clickElement(selectAllButton);
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
                WebElementUtil.getTableRows(getDriver(), readyElement(
                        existingElement(classesTable), By.tagName("table")));
        for (TableRow tableRow : tableRows) {
            // column 2, 3, 4 are checkboxes for purge, reindex and optimize
            for (int i = 1; i <= 3; i++) {
                WebElement checkBox = tableRow.getHeaders().get(i)
                        .findElement(By.tagName("input"));
                if (!Checkbox.of(checkBox).checked()) {
                    return false;
                }
            }
        }
        return true;
    }

    public ManageSearchPage performSelectedActions() {
        log.info("Click Perform Actions");
        clickElement(performButton);
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            // The Abort button will display
            return readyElement(cancelButton).isDisplayed();
        });
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage expectActionsToFinish() {
        log.info("Wait: all actions are finished");
        // once the button re-appears, it means the reindex is done.
        readyElement(performButton);
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage abort() {
        log.info("Click Abort");
        clickElement(abortButton);
        return new ManageSearchPage(getDriver());
    }

    public boolean noOperationsRunningIsDisplayed() {
        log.info("Query No Operations");
        return readyElement(noOpsLabel).isDisplayed();
    }

    public boolean completedIsDisplayed() {
        log.info("Query is action completed");
        return readyElement(completedLabel).isDisplayed();
    }

    public boolean abortedIsDisplayed() {
        log.info("Query is action aborted");
        return readyElement(abortedLabel).isDisplayed();
    }
}
