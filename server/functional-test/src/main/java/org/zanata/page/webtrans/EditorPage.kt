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
package org.zanata.page.webtrans

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.ArrayList
import org.apache.commons.lang3.StringUtils
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.zanata.page.BasePage
import org.zanata.page.editor.ReactEditorPage
import org.zanata.page.projectversion.VersionLanguagesPage
import org.zanata.util.WebElementUtil
import com.google.common.base.Joiner

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class EditorPage(driver: WebDriver) : BasePage(driver) {
    private val glossaryTable = By.id("gwt-debug-glossaryResultTable")
    private val glossaryNoResult = By.id("gwt-debug-glossaryNoResult")
    private val glossarySearchInput = By.id("gwt-debug-glossaryTextBox")
    private val transUnitTable = By.id("gwt-debug-transUnitTable")
    private val editorFilterField = By.id("gwt-debug-editor-filter-box")
    private val configurationPanel = By.className("i--settings")
    private val validationBox = By.className("gwt-DisclosurePanel")
    private val validationOptions = By.xpath("//a[@title=\'Validation options\']")
    private val validationOptionsView = By.id("validationOptionsView")
    private val alphaEditorButton = By.linkText("Try the new alpha editor")

    /**
     * There is usually a long poll waiting for GWTEventService events from the
     * server.
     *
     * @return
     */
    override val expectedBackgroundRequests: Int
        get() = 1

    /**
     * First row is header: SourceTerm, TargetTerm, Action, Details.
     *
     * @return a table representing the searchResultTable
     */
    val glossaryResultTable: List<List<String>>
        get() {
            log.info("Query glossary results")
            return waitForAMoment().withMessage("glossary results is not empty")
                    .until { webDriver ->
                        val resultTable: List<List<String>>
                        if (webDriver.findElements(glossaryNoResult).size == 1) {
                            resultTable = emptyList()
                        } else {
                            resultTable = WebElementUtil
                                    .getTwoDimensionList(webDriver, glossaryTable)
                        }
                        log.info("glossary result: {}", resultTable)
                        resultTable
                    }
        }

    val statistics: String
        get() {
            log.info("Query statistics")
            return readyElement(By.id("gwt-debug-statistics-label")).text
        }

    val messageSources: List<String>
        get() {
            log.info("Query list of text flow sources")
            return WebElementUtil.elementsToText(readyElement(transUnitTable)
                    .findElements(By.className("gwt-HTML")))
        }

    /**
     * Get the validation error messages for the currently translated row
     *
     * @return error string
     */
    val validationMessageCurrentTarget: String
        get() {
            log.info("Query validation messages on current item")
            waitForAMoment().withMessage("validation messages are not empty")
                    .until { !targetValidationBox.text.isEmpty() }
            return targetValidationBox.text
        }

    /**
     * Query whether the first validation messages box is displayed
     *
     * @return is/not displayed
     */
    val isValidationMessageCurrentTargetVisible: Boolean
        get() {
            log.info("Query is validation message panel displayed")
            return targetValidationBox.isDisplayed
        }

    val filterQuery: String
        get() {
            log.info("Query filter text")
            return readyElement(editorFilterField).getAttribute("value")
        }
    // Find the right side column for the selected row

    private// Right column
    val translationTargetColumn: WebElement
        get() = readyElement(By.className("selected"))
                .findElements(By.className("transUnitCol"))[1]
    // Find the validation messages / errors box

    private val targetValidationBox: WebElement
        get() = existingElement(translationTargetColumn, validationBox)

    val translationHistoryCompareTabtext: String
        get() {
            log.info("Query history tab text")
            return compareTab.text
        }

    val comparisonTextDiff: List<String>
        get() {
            log.info("Query diff from history compare")
            val diffs = ArrayList<String>()
            for (element in compareTabEntries) {
                for (diffElement in element
                        .findElements(By.className("diff-insert"))) {
                    diffs.add("++" + diffElement.text)
                }
                for (diffElement in element
                        .findElements(By.className("diff-delete"))) {
                    diffs.add("--" + diffElement.text)
                }
            }
            return diffs
        }

    private val translationHistoryBox: WebElement
        get() = existingElement(existingElement(By.id("gwt-debug-transHistory")),
                By.id("gwt-debug-transHistoryTabPanel"))

    private val translationHistoryList: List<WebElement>
        get() = translationHistoryBox
                .findElement(By.className("gwt-TabLayoutPanelContent"))
                .findElement(By.className("list--slat"))
                .findElements(By.className("l--pad-v-1"))

    private val compareTab: WebElement
        get() = translationHistoryBox
                .findElement(By.className("gwt-TabLayoutPanelTabs"))
                .findElements(By.className("gwt-TabLayoutPanelTabInner"))[1]

    private// Second tab
    val compareTabEntries: List<WebElement>
        get() = translationHistoryBox
                .findElements(By.className("gwt-TabLayoutPanelContent"))[1]
                .findElements(By.className("textFlowEntry"))

    enum class Validations {
        HTML,
        JAVAVARIABLES,
        NEWLINE,
        POSITIONAL,
        PRINTF,
        TABS,
        XML

    }

    fun searchGlossary(term: String): EditorPage {
        log.info("Search glossary for {}", term)
        waitForAMoment().withMessage("glossary list is ready")
                .until { webDriver ->
                    webDriver
                            .findElements(glossaryNoResult).size == 1 || webDriver.findElements(glossaryTable).size == 1
                }
        readyElement(glossarySearchInput).clear()
        enterText(readyElement(glossarySearchInput), term)
        clickElement(By.id("gwt-debug-glossarySearchButton"))
        return EditorPage(driver)
    }

    /**
     * Get content of a text flow source at given row. This assumes the text
     * flow has singular form (i.e. no plural). If a test requires to access
     * plural content, this can be changed.
     *
     * @param rowIndex
     * row index
     * @return content of the source
     */
    fun getMessageSourceAtRowIndex(rowIndex: Int): String {
        log.info("Query text flow source at {}", rowIndex)
        return getCodeMirrorContent(rowIndex.toLong(), SOURCE_ID_FMT,
                Plurals.SourceSingular)
    }

    fun getMessageSourceAtRowIndex(rowIndex: Int, plural: Plurals): String {
        log.info("Query text flow source at {}", rowIndex)
        return getCodeMirrorContent(rowIndex.toLong(), SOURCE_ID_FMT, plural)
    }

    /**
     * Get content of a text flow target at given row. This assumes the text
     * flow has singular form (i.e. no plural). If a test requires to access
     * plural content, this can be changed.
     *
     * @param rowIndex
     * row index
     * @return content of the target
     */
    fun getMessageTargetAtRowIndex(rowIndex: Int): String {
        log.info("Query text flow target at {}", rowIndex)
        return getCodeMirrorContent(rowIndex.toLong(), TARGET_ID_FMT,
                Plurals.TargetSingular)
    }

    private fun getCodeMirrorContent(rowIndex: Long,
                                     idFormat: String, plurals: Plurals): String {
        return waitForAMoment().withMessage("gwt contents available")
                .until { webDriver ->
                    // code mirror will turn text into list of <pre>.
                    val cmTextLines = webDriver
                            .findElement(By.id(String.format(idFormat, rowIndex,
                                    plurals.index())))
                            .findElements(By.tagName("pre"))
                    val contents = WebElementUtil.elementsToText(cmTextLines)
                    Joiner.on("\n").skipNulls().join(contents)
                }
    }

    @Suppress("unused")
    fun setSyntaxHighlighting(option: Boolean): EditorPage {
        log.info("Set syntax highlight to {}", option)
        openConfigurationPanel()
        val highlight = readyElement(By.id(EDITOR_SYNTAXHIGHLIGHT))
        if (highlight.isSelected != option) {
            highlight.click()
        }
        return EditorPage(driver)
    }

    private fun openConfigurationPanel(): Boolean? {
        log.info("Click to open Configuration options")
        waitForAMoment().withMessage("config settings button is enabled")
                .until {
                    driver.findElement(By.className("i--settings")).isEnabled
                }
        Actions(driver).click(readyElement(configurationPanel))
                .perform()
        return waitForAMoment().withMessage("config panel is displayed")
                .until { driver ->
                    driver.findElement(By.className(
                            "gwt-TabLayoutPanelContentContainer")).isDisplayed
                }
    }

    /**
     * Get content from a target using the non-CodeMirror configuration
     *
     * @param rowIndex
     * @return row target content
     */
    fun getBasicTranslationTargetAtRowIndex(rowIndex: Int): String {
        log.info("Query text flow source at {}", rowIndex)
        return getContentAtRowIndex(rowIndex.toLong(), TARGET_ID_FMT,
                Plurals.TargetSingular)
    }

    fun getBasicTranslationTargetAtRowIndex(rowIndex: Int,
                                            plurals: Plurals): String {
        log.info("Query text flow source at {}", rowIndex)
        return getContentAtRowIndex(rowIndex.toLong(), TARGET_ID_FMT, plurals)
    }

    fun expectBasicTranslationAtRowIndex(rowIndex: Int,
                                         expected: String): Boolean {
        log.info("Wait for text flow target at {} to be {}", rowIndex,
                expected)
        return waitForAMoment()
                .withMessage("expect translation in row " + rowIndex.toString())
                .until { getBasicTranslationTargetAtRowIndex(rowIndex) == expected }
    }

    private fun getContentAtRowIndex(rowIndex: Long,
                                     idFormat: String, plural: Plurals): String {
        return readyElement(
                By.id(String.format(idFormat, rowIndex, plural.index())))
                .getAttribute("value")
    }

    /**
     * Translate a target using the non-CodeMirror field
     *
     * @param rowIndex
     * @param text
     * @return updated EditorPage
     */
    fun translateTargetAtRowIndex(rowIndex: Int,
                                  text: String): EditorPage {
        log.info("Enter at {} the text {}", rowIndex, text)
        setTargetContent(rowIndex.toLong(), text, TARGET_ID_FMT, Plurals.SourceSingular)
        return EditorPage(driver)
    }

    private fun setTargetContent(rowIndex: Long, text: String,
                                 idFormat: String, plural: Plurals) {
        val we = readyElement(
                By.id(String.format(idFormat, rowIndex, plural.index())))
        we.click()
        we.clear()
        we.sendKeys(text)
    }

    /**
     * Simulate a paste from the user's clipboard into the indicated row
     *
     * @param rowIndex
     * row to enter text
     * @param text
     * text to be entered
     * @return new EditorPage
     */
    fun pasteIntoRowAtIndex(rowIndex: Long,
                            text: String): EditorPage {
        log.info("Paste at {} the text {}", rowIndex, text)
        val we = readyElement(By.id(String.format(TARGET_ID_FMT,
                rowIndex, Plurals.SourceSingular.index())))
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
        we.click()
        we.sendKeys(Keys.LEFT_CONTROL.toString() + "v")
        return EditorPage(driver)
    }

    fun approveTranslationAtRow(rowIndex: Int): EditorPage {
        log.info("Click Approve on row {}", rowIndex)
        readyElement(By.id(String.format(APPROVE_BUTTON_ID_FMT, rowIndex)))
                .click()
        slightPause()
        return EditorPage(driver)
    }

    fun saveAsFuzzyAtRow(rowIndex: Int): EditorPage {
        log.info("Click Fuzzy on row {}", rowIndex)
        readyElement(By.id(String.format(FUZZY_BUTTON_ID_FMT, rowIndex)))
                .click()
        return EditorPage(driver)
    }

    @Suppress("unused")
    fun getMessageTargetAtRowIndex(rowIndex: Int, plurals: Plurals): String {
        log.info("Query text flow target at {}", rowIndex)
        return getCodeMirrorContent(rowIndex.toLong(), TARGET_ID_FMT, plurals)
    }

    /**
     * Wait for a delayed validation error panel to display
     */
    fun expectValidationErrorsVisible() {
        log.info("Wait for validation message panel displayed")
        waitForPageSilence()
        assertThat(isValidationMessageCurrentTargetVisible)
                .`as`("validation message panel displayed")
                .isTrue()
    }

    /**
     * Click on the validation error box to view details
     *
     * @return new EditorPage
     */
    fun openValidationBox(): EditorPage {
        log.info("Click to open Validation panel")
        clickElement(existingElement(targetValidationBox, By.tagName("a")))
        waitForAMoment().withMessage("contains 'Unexpected' or 'Target'")
                .until {
                    StringUtils.containsAny(validationMessageCurrentTarget,
                            "Unexpected", "Target")
                }
        return EditorPage(driver)
    }

    /**
     * Opens the validation options sidebar
     *
     * @return new EditorPage
     */
    fun openValidationOptions(): EditorPage {
        log.info("Click to open Validation options panel")
        Actions(driver)
                .click(readyElement(existingElement(By.id("container")),
                        validationOptions))
                .perform()
        existingElement(validationOptionsView)
        return EditorPage(driver)
    }

    /**
     * Check if a validation option is available
     *
     * @param validation
     * the option to check
     * @return new EditorPage
     */
    fun isValidationOptionAvailable(validation: Validations): Boolean {
        log.info("Query is validation option {} available", validation)
        return readyElement(By.xpath(
                "//*[@title=\'" + getValidationTitle(validation) + "\']"))
                .findElement(By.tagName("input")).isEnabled
    }

    /**
     * Check if a validation option is selected
     *
     * @param validation
     * the option to check
     * @return new EditorPage
     */
    fun isValidationOptionSelected(validation: Validations): Boolean {
        log.info("Query is validation option {} selected", validation)
        return existingElement(
                existingElement(By.xpath("//*[@title=\'"
                        + getValidationTitle(validation) + "\']")),
                By.tagName("input")).isSelected
    }

    /**
     * Click a validation option
     *
     * @param validation
     * the option to click
     * @return new EditorPage
     */
    fun clickValidationCheckbox(validation: Validations): EditorPage {
        log.info("Click validation checkbox {}", validation)
        clickElement(readyElement(
                existingElement(By.xpath("//*[@title=\'"
                        + getValidationTitle(validation) + "\']")),
                By.tagName("input")))
        return EditorPage(driver)
    }

    private fun getValidationTitle(validation: Validations): String {
        when (validation) {
            EditorPage.Validations.HTML -> return "Check that XML/HTML tags are consistent"

            EditorPage.Validations.JAVAVARIABLES -> return "Check that java style ({x}) variables are consistent"

            EditorPage.Validations.NEWLINE -> return "Check for consistent leading and trailing newline (\\n)"

            EditorPage.Validations.POSITIONAL -> return "Check that positional printf style (%n\$x) variables are consistent"

            EditorPage.Validations.PRINTF -> return "Check that printf style (%x) variables are consistent"

            EditorPage.Validations.TABS -> return "Check whether source and target have the same number of tabs."

            EditorPage.Validations.XML -> return "Check that XML entity are complete"

            else -> throw RuntimeException("Unknown validation!")
        }
    }

    fun inputFilterQuery(query: String): EditorPage {
        log.info("Enter filter query {}", query)
        readyElement(editorFilterField).clear()
        enterText(readyElement(editorFilterField), query + Keys.ENTER, true,
                false, false)
        return EditorPage(driver)
    }
    // Click the History button for the selected row - row id must be known

    fun clickShowHistoryForRow(row: Int): EditorPage {
        log.info("Click history button on row {}", row)
        readyElement(translationTargetColumn,
                By.id("gwt-debug-target-$row-history")).click()
        waitForAMoment().withMessage("translation history box is displayed")
                .until { translationHistoryBox.isDisplayed }
        return EditorPage(driver)
    }

    fun getHistoryEntryAuthor(entry: Int): String {
        log.info("Query author, action on history entry {}", entry)
        return translationHistoryList[entry]
                .findElements(By.className("gwt-InlineHTML"))[0]
                .findElement(By.className("txt--meta"))
                .findElement(By.tagName("a"))
                .text
    }

    fun getHistoryEntryContent(entry: Int): String {
        log.info("Query content on history entry {}", entry)
        return translationHistoryList[entry]
                .findElements(By.className("gwt-InlineHTML"))[1]
                .findElement(By.className("cm-s-default")).text
    }

    fun clickCompareOn(entry: Int): EditorPage {
        log.info("Click Compare on history entry {}", entry)
        waitForAMoment().withMessage("compare button is displayed").until {
            try {
                return@until translationHistoryList
            } catch (ioobe: IndexOutOfBoundsException) {
                return@until false
            }
        }
        clickElement(translationHistoryList[entry].findElement(By.linkText("Compare")))
        slightPause()
        return EditorPage(driver)
    }

    fun clickCompareVersionsTab(): EditorPage {
        log.info("Click on Compare versionsList tab")
        compareTab.click()
        readyElement(translationHistoryBox, By.className("html-face"))
        return EditorPage(driver)
    }

    fun getComparisonTextInRow(row: Int): String {
        log.info("Query comparison text in row {}", row)
        return compareTabEntries[row].findElement(By.tagName("pre"))
                .text
    }

    fun clickVersionBreadcrumb(versionName: String): VersionLanguagesPage {
        readyElement(By.linkText(versionName)).click()
        slightPause()
        return VersionLanguagesPage(driver)
    }

    fun pressAlphaEditorButton(): ReactEditorPage {
        log.info("Pressing Alpha Editor button")
        clickElement(alphaEditorButton)
        return ReactEditorPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(EditorPage::class.java)

        // first %d is row index, second %d is plural form index (i.e. 0 or 1)
        private const val SOURCE_ID_FMT = "gwt-debug-%d-source-panel-%d-container"
        // first %d is row index, second %d is plural form index (i.e. 0-6)
        private const val TARGET_ID_FMT = "gwt-debug-%d-target-%d"
        private const val EDITOR_SYNTAXHIGHLIGHT = "gwt-debug-syntax-highlight-chk-input"
        // buttons id format
        private const val APPROVE_BUTTON_ID_FMT = "gwt-debug-target-%d-save-approve"
        private const val FUZZY_BUTTON_ID_FMT = "gwt-debug-target-%d-save-fuzzy"
    }
}
