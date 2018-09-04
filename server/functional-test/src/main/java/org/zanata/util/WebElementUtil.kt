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
package org.zanata.util

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import org.zanata.page.WebDriverFactory
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import kotlin.streams.toList

object WebElementUtil {

    /**
     * This method potentially will suffer from StaleElementException if the
     * WebElements given are dynamic elements on the page. If so consider using
     * #elementsToText(org.openqa.selenium.WebDriver, org.openqa.selenium.By)
     * instead.
     *
     * @param webElements
     * collection of WebElement
     * @return text representation of the elements
     */
    fun elementsToText(webElements: Collection<WebElement>): List<String> {
        return ImmutableList.copyOf(webElements.stream()
                .map<String> { it -> getTrimmedText(it) }
                .iterator())
    }

    fun elementsToText(driver: WebDriver, by: By): List<String> {
        return waitForAMoment(driver).until { input ->
            if (input == null) {
                throw RuntimeException("Driver is null")
            }
            val elements = input.findElements(by)
            elements.stream().map<String> { it -> getTrimmedText(it) }.toList()
        }
    }

    private fun getInnerHTML(driver: WebDriver, element: WebElement): String {
        return (driver as JavascriptExecutor)
                .executeScript("return arguments[0].innerHTML;", element) as String
    }

    @Suppress("unused")
    fun elementsToInnerHTML(driver: WebDriver,
                            webElements: Collection<WebElement>): List<String> {
        return ImmutableList.copyOf(webElements.stream()
                .map { it -> getInnerHTML(driver, it) }
                .iterator())
    }

    fun getTableRows(driver: WebDriver,
                     byQueryForTable: By): List<TableRow> {
        return waitForAMoment(driver)
                .until { webDriver ->
                    if (webDriver == null) {
                        throw RuntimeException("Driver is null")
                    }
                    val table = webDriver.findElement(byQueryForTable)
                    val rows = table.findElements(By.xpath(".//tbody[1]/tr"))
                    ImmutableList.copyOf(rows.stream().map<TableRow> { TableRow(it) }
                            .iterator())
                }
    }

    fun getTableRows(driver: WebDriver,
                     table: WebElement): List<TableRow> {
        return waitForAMoment(driver)
                .until { webDriver ->
                    if (webDriver == null) {
                        throw RuntimeException("Driver is null")
                    }
                    val rows = table.findElements(By.xpath(".//tbody[1]/tr"))
                    rows.stream().map<TableRow> { it -> TableRow(it) }.toList()
                }
    }

    private fun transformToTwoDimensionList(tableRows: List<TableRow>): ImmutableList<List<String>> {
        return ImmutableList.copyOf(tableRows.stream().map { row ->
            if (row == null) {
                throw RuntimeException("Source table is null")
            }
            row.cellContents
        }.iterator())
    }

    private fun waitForSeconds(webDriver: WebDriver,
                               durationInSec: Int): FluentWait<WebDriver> {
        return WebDriverLogWait(WebDriverFactory.INSTANCE, durationInSec.toLong())
                // if this happens, just wait (and try again)
                .ignoring(StaleElementReferenceException::class.java)
    }

    fun waitForAMoment(webDriver: WebDriver): FluentWait<WebDriver> {
        return waitForSeconds(webDriver,
                WebDriverFactory.INSTANCE.webDriverWaitTime)
    }

    fun getColumnContents(driver: WebDriver, by: By,
                          columnIndex: Int): List<String> {
        return waitForAMoment(driver)
                .until<List<String>> { webDriver ->
                    if (webDriver == null) {
                        throw RuntimeException("Driver is null")
                    }
                    val table: WebElement
                    try {
                        table = webDriver.findElement(by)
                    } catch (noElement: NoSuchElementException) {
                        // Some pages don't show a table, if there's no
                        // items to show
                        return@until emptyList<String>()
                    }

                    val rows = table.findElements(By.xpath(".//tbody[1]/tr"))
                    ImmutableList.copyOf(rows.stream()
                            .map<TableRow> { TableRow(it) }
                            .map { row ->
                                val cellContents = row.cellContents
                                Preconditions.checkElementIndex(columnIndex,
                                        cellContents.size, "column index")
                                cellContents[columnIndex]
                            }.iterator())
                }
    }

    fun getTwoDimensionList(driver: WebDriver,
                            by: By): List<List<String>> {
        return waitForAMoment(driver)
                .until { webDriver ->
                    if (webDriver == null) {
                        throw RuntimeException("Driver is null")
                    }
                    val table = webDriver.findElement(by)
                    val rows = table.findElements(By.xpath(".//tbody[1]/tr"))
                    val tableRows = rows.stream()
                            .map<TableRow> { TableRow(it) }.toList()
                    transformToTwoDimensionList(tableRows)
                }
    }

    fun getListItems(driver: WebDriver, by: By): List<WebElement> {
        return waitForAMoment(driver)
                .until { webDriver ->
                    if (webDriver == null) {
                        throw RuntimeException("Driver is null")
                    }
                    val list = webDriver.findElement(by)
                    list.findElements(By.xpath(".//li"))
                }
    }

    /**
     * This method is used to set JSF rich text editor (KCEditor) content.
     *
     * @param driver
     * web driver
     * @param richEditorWrapperField
     * the wrapper div of the editor
     * @param content
     * content wants to set
     */
    fun setRichTextEditorContent(driver: WebDriver,
                                 richEditorWrapperField: WebElement, content: String) {
        // This is how we can change JSF rich text editor content.
        val richTextEditorFrame = richEditorWrapperField.findElement(By.tagName("iframe"))
        driver.switchTo().frame(richTextEditorFrame)
        (driver as JavascriptExecutor)
                .executeScript("document.body.innerHTML=\'$content\'")
        driver.switchTo().defaultContent()
    }

    private fun getTrimmedText(elem: WebElement): String {
        return elem.text.trim { it <= ' ' }
    }

    fun searchAutocomplete(driver: WebDriver, id: String,
                           query: String) {
        val locator = "$id-autocomplete__input"
        waitForAMoment(driver)
                .until { webDriver ->
                    webDriver
                            .findElement(By.id(locator)).isDisplayed
                }
        driver.findElement(By.id(locator)).sendKeys(query)
    }

    fun getSearchAutocompleteResults(
            driver: WebDriver, formId: String, id: String): List<WebElement> {
        return waitForAMoment(driver)
                .until { webDriver ->
                    val locator = "$formId:$id:$id-result"
                    webDriver.findElement(By.id(locator)).findElements(
                            By.className("js-autocomplete__result"))
                }
    }

    fun getSearchAutocompleteItems(driver: WebDriver,
                                   formId: String, id: String): List<String> {
        val results = getSearchAutocompleteResults(driver, formId, id)
        return results.stream().map<String> { it.text }.toList()
    }

    fun triggerScreenshot(tag: String) {
        WebDriverFactory.INSTANCE.injectScreenshot(tag)
    }
}
