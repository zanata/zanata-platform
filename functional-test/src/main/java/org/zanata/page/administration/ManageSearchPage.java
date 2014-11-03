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
        log.info("Click Select All");
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
        log.info("Query all actions selected");
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
        log.info("Click Perform Actions");
        getDriver().findElement(By.id("form:reindex")).click();
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // The Abort button will display
                return input.findElement(By.id("form:cancel")).isDisplayed();
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
                return input.findElement(By.id("form:reindex")).isDisplayed();
            }
        });
        return new ManageSearchPage(getDriver());
    }

    public ManageSearchPage abort() {
        log.info("Click Abort");
        getDriver().findElement(abortButtonBy).click();
        return new ManageSearchPage(getDriver());
    }


    public boolean noOperationsRunningIsDisplayed() {
        log.info("Query No Operations");
        return getDriver().findElement(By.id("noOperationsRunning")).isDisplayed();
    }

    public boolean completedIsDisplayed() {
        log.info("Query is action completed");
        return getDriver().findElement(By.id("completed")).isDisplayed();
    }

    public boolean abortedIsDisplayed() {
        log.info("Query is action aborted");
        return getDriver().findElement(By.id("aborted")).isDisplayed();
    }

}
