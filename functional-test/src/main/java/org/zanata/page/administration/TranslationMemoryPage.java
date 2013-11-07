/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class TranslationMemoryPage extends BasePage {
    private static final int ID_COLUMN = 0;
    private static final int DESCRIPTION_COLUMN = 1;
    private static final int ENTRIES_COLUMN = 2;
    private static final int IMPORT_COLUMN = 4;
    private static final int EXPORT_COLUMN = 5;
    private static final int ACTIONS_COLUMN = 6;

    @FindBy(id = "createTmLink")
    private WebElement createTmLink;

    @FindBy(id = "main_content:form:tmTable")
    private WebElement tmList;

    private By tmListBy = By.id("main_content:form:tmTable");

    public TranslationMemoryPage(WebDriver driver) {
        super(driver);
    }

    public TranslationMemoryEditPage clickCreateNew() {
        createTmLink.click();
        return new TranslationMemoryEditPage(getDriver());
    }

    public TranslationMemoryPage clickImport(String tmName) {
        WebElement importButton =
                findRowByTMName(tmName).getCells().get(IMPORT_COLUMN)
                        .findElement(By.tagName("a"));
        importButton.click();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage enterImportFileName(String importFileName) {
        getDriver().findElement(By.name("uploadedFile")).sendKeys(
                importFileName);
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickUploadButtonAndAcknowledge() {
        getDriver().findElement(By.name("uploadBtn")).click();
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public Alert expectFailedUpload() {
        getDriver().findElement(By.name("uploadBtn")).click();
        return switchToAlert();
    }

    public TranslationMemoryPage clickClearTMAndAccept(String tmName) {
        clickTMAction(tmName, 0).accept();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickClearTMAndCancel(String tmName) {
        clickTMAction(tmName, 0).dismiss();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickDeleteTmAndAccept(String tmName) {
        clickTMAction(tmName, 1).accept();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickDeleteTmAndCancel(String tmName) {
        clickTMAction(tmName, 1).dismiss();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage dismissError() {
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public List<String> getListedTranslationMemorys() {
        return WebElementUtil.getColumnContents(getDriver(), tmListBy,
                ID_COLUMN);
    }

    public String getDescription(String tmName) {
        return findRowByTMName(tmName).getCells().get(DESCRIPTION_COLUMN)
                .getText();
    }

    public String getNumberOfEntries(String tmName) {
        return findRowByTMName(tmName).getCells().get(ENTRIES_COLUMN).getText();
    }

    public String waitForExpectedNumberOfEntries(final String tmName,
            final String expected) {
        return waitForTenSec().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver driver) {
                return expected.equals(getNumberOfEntries(tmName)) ? getNumberOfEntries(tmName)
                        : null;
            }
        });
    }

    public boolean canDelete(String tmName) {
        return findRowByTMName(tmName).getCells().get(ACTIONS_COLUMN)
                .findElements(By.tagName("input")).get(1).isEnabled();
    }

    /*
     * Get a row from the TM table that corresponds with tmName
     */
    private TableRow findRowByTMName(final String tmName) {
        TableRow matchedRow =
                waitForTenSec().until(new Function<WebDriver, TableRow>() {
                    @Override
                    public TableRow apply(WebDriver driver) {
                        List<TableRow> tableRows =
                                WebElementUtil
                                        .getTableRows(getDriver(), tmList);
                        Optional<TableRow> matchedRow =
                                Iterables.tryFind(tableRows,
                                        new Predicate<TableRow>() {
                                            @Override
                                            public boolean
                                                    apply(TableRow input) {
                                                List<String> cellContents =
                                                        input.getCellContents();
                                                String localeCell =
                                                        cellContents.get(
                                                                ID_COLUMN)
                                                                .trim();
                                                return localeCell
                                                        .equalsIgnoreCase(tmName);
                                            }
                                        });

                        // Keep looking for the TM entry until timeout
                        return matchedRow.isPresent() ? matchedRow.get() : null;
                    }
                });
        return matchedRow;
    }

    private Alert clickTMAction(String tmName, int position) {
        findRowByTMName(tmName).getCells().get(ACTIONS_COLUMN)
                .findElements(By.tagName("input")).get(position).click();
        return switchToAlert();
    }
}
