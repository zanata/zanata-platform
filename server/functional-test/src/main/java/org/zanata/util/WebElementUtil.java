/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.zanata.page.WebDriverFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class WebElementUtil {

    private WebElementUtil() {
    }

    /**
     * This method potentially will suffer from StaleElementException if the
     * WebElements given are dynamic elements on the page. If so consider using
     * #elementsToText(org.openqa.selenium.WebDriver, org.openqa.selenium.By)
     * instead.
     *
     * @param webElements
     *            collection of WebElement
     * @return text representation of the elements
     */
    public static List<String>
            elementsToText(Collection<WebElement> webElements) {
        return ImmutableList.copyOf(webElements.stream()
                .map(WebElementUtil::getTrimmedText)
                .iterator());
    }

    public static List<String> elementsToText(WebDriver driver, final By by) {
        return waitForAMoment(driver)
                .until(input -> {
                    if (input == null) {
                        throw new RuntimeException("Driver is null");
                    }
                    List<WebElement> elements = input.findElements(by);
                    return ImmutableList.copyOf(elements.stream()
                            .map(WebElementUtil::getTrimmedText).iterator());
                });
    }

    public static String getInnerHTML(WebDriver driver, WebElement element) {
        return (String) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].innerHTML;", element);
    }

    public static List<String> elementsToInnerHTML(WebDriver driver,
            Collection<WebElement> webElements) {
        return ImmutableList.copyOf(webElements.stream()
                .map(it -> getInnerHTML(driver, it))
                .iterator());
    }

    public static List<TableRow> getTableRows(WebDriver driver,
            final By byQueryForTable) {
        return waitForAMoment(driver)
                .until(webDriver -> {
                    if (webDriver == null) {
                        throw new RuntimeException("Driver is null");
                    }
                    final WebElement table =
                            webDriver.findElement(byQueryForTable);
                    List<WebElement> rows =
                            table.findElements(By.xpath(".//tbody[1]/tr"));
                    return ImmutableList.copyOf(rows.stream().map(TableRow::new)
                            .iterator());
                });
    }

    public static List<TableRow> getTableRows(WebDriver driver,
            final WebElement table) {
        return waitForAMoment(driver)
                .until(webDriver -> {
                    if (webDriver == null) {
                        throw new RuntimeException("Driver is null");
                    }
                    List<WebElement> rows =
                            table.findElements(By.xpath(".//tbody[1]/tr"));
                    return ImmutableList.copyOf(rows.stream().map(
                            TableRow::new).iterator());
                });
    }

    public static ImmutableList<List<String>>
            transformToTwoDimensionList(List<TableRow> tableRows) {
        return ImmutableList.copyOf(tableRows.stream().map(row -> {
            if (row == null) {
                throw new RuntimeException("Source table is null");
            }
            return row.getCellContents();
        }).iterator());
    }

    public static FluentWait<WebDriver> waitForSeconds(WebDriver webDriver,
            int durationInSec) {
        return new WebDriverLogWait(WebDriverFactory.INSTANCE, durationInSec)
                // if this happens, just wait (and try again)
                .ignoring(StaleElementReferenceException.class);
    }

    public static FluentWait<WebDriver> waitForAMoment(WebDriver webDriver) {
        return waitForSeconds(webDriver,
                WebDriverFactory.INSTANCE.getWebDriverWait());
    }

    public static List<String> getColumnContents(WebDriver driver, final By by,
            final int columnIndex) {
        return waitForAMoment(driver)
                .until(webDriver -> {
                    if (webDriver == null) {
                        throw new RuntimeException("Driver is null");
                    }
                    WebElement table;
                    try {
                        table = webDriver.findElement(by);
                    } catch (NoSuchElementException noElement) {
                        // Some pages don't show a table, if there's no
                        // items to show
                        return Collections.emptyList();
                    }
                    List<WebElement> rows =
                            table.findElements(By.xpath(".//tbody[1]/tr"));
                    return ImmutableList.copyOf(rows.stream()
                            .map(TableRow::new)
                            .map(row -> {
                                List<String> cellContents =
                                        row.getCellContents();
                                Preconditions.checkElementIndex(columnIndex,
                                        cellContents.size(), "column index");
                                return cellContents.get(columnIndex);
                            }).iterator());
                });
    }

    public static List<List<String>> getTwoDimensionList(WebDriver driver,
            final By by) {
        return waitForAMoment(driver)
                .until(webDriver -> {
                    if (webDriver == null) {
                        throw new RuntimeException("Driver is null");
                    }
                    final WebElement table = webDriver.findElement(by);
                    List<WebElement> rows =
                            table.findElements(By.xpath(".//tbody[1]/tr"));
                    List<TableRow> tableRows = rows.stream()
                            .map(TableRow::new)
                            .collect(Collectors.toList());
                    return transformToTwoDimensionList(tableRows);
                });
    }

    public static List<WebElement> getListItems(WebDriver driver, final By by) {
        return waitForAMoment(driver)
                .until(webDriver -> {
                    if (webDriver == null) {
                        throw new RuntimeException("Driver is null");
                    }
                    final WebElement list = webDriver.findElement(by);
                    return list.findElements(By.xpath(".//li"));
                });
    }

    /**
     * This method is used to set JSF rich text editor (KCEditor) content.
     *
     * @param driver
     *            web driver
     * @param richEditorWrapperField
     *            the wrapper div of the editor
     * @param content
     *            content wants to set
     */
    public static void setRichTextEditorContent(WebDriver driver,
            WebElement richEditorWrapperField, String content) {
        // This is how we can change JSF rich text editor content.
        WebElement richTextEditorFrame =
                richEditorWrapperField.findElement(By.tagName("iframe"));
        driver.switchTo().frame(richTextEditorFrame);
        ((JavascriptExecutor) driver)
                .executeScript("document.body.innerHTML=\'" + content + "\'");
        driver.switchTo().defaultContent();
    }

    private static String getTrimmedText(WebElement elem) {
        return elem.getText().trim();
    }

    public static void searchAutocomplete(WebDriver driver, String id,
            String query) {
        final String locator = id + "-autocomplete__input";
        waitForAMoment(driver)
                .until(webDriver -> webDriver
                        .findElement(By.id(locator)).isDisplayed());
        driver.findElement(By.id(locator)).sendKeys(query);
    }

    public static List<WebElement> getSearchAutocompleteResults(
            WebDriver driver, final String formId, final String id) {
        return waitForAMoment(driver)
                .until(webDriver -> {
                    String locator = formId + ":" + id + ":" + id + "-result";
                    return webDriver.findElement(By.id(locator)).findElements(
                            By.className("js-autocomplete__result"));
                });
    }

    public static List<String> getSearchAutocompleteItems(WebDriver driver,
            final String formId, final String id) {
        List<WebElement> results =
                getSearchAutocompleteResults(driver, formId, id);
        return results.stream()
                .map(WebElement::getText).collect(Collectors.toList());
    }

    public static void triggerScreenshot(final String tag) {
        WebDriverFactory.INSTANCE.injectScreenshot(tag);
    }
}
