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
package org.zanata.page.webtrans;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EditorPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EditorPage.class);

    public enum Validations {
        HTML,
        JAVAVARIABLES,
        NEWLINE,
        POSITIONAL,
        PRINTF,
        TABS,
        XML;

    }

    // first %d is row index, second %d is plural form index (i.e. 0 or 1)
    private static final String SOURCE_ID_FMT =
            "gwt-debug-%d-source-panel-%d-container";
    // first %d is row index, second %d is plural form index (i.e. 0-6)
    private static final String TARGET_ID_FMT = "gwt-debug-%d-target-%d";
    private static final String EDITOR_SYNTAXHIGHLIGHT =
            "gwt-debug-syntax-highlight-chk-input";
    // buttons id format
    private static final String APPROVE_BUTTON_ID_FMT =
            "gwt-debug-target-%d-save-approve";
    private static final String FUZZY_BUTTON_ID_FMT =
            "gwt-debug-target-%d-save-fuzzy";
    private final By glossaryTable = By.id("gwt-debug-glossaryResultTable");
    private final By glossaryNoResult = By.id("gwt-debug-glossaryNoResult");
    private By glossarySearchInput = By.id("gwt-debug-glossaryTextBox");
    private By transUnitTable = By.id("gwt-debug-transUnitTable");
    private By editorFilterField = By.id("gwt-debug-editor-filter-box");
    private By configurationPanel = By.className("i--settings");
    private By validationBox = By.className("gwt-DisclosurePanel");
    private By validationOptions =
            By.xpath("//a[@title=\'Validation options\']");
    private By validationOptionsView = By.id("validationOptionsView");

    public EditorPage(WebDriver driver) {
        super(driver);
    }

    public EditorPage searchGlossary(final String term) {
        log.info("Search glossary for {}", term);
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> webDriver
                .findElements(glossaryNoResult).size() == 1
                || webDriver.findElements(glossaryTable).size() == 1);
        readyElement(glossarySearchInput).clear();
        enterText(readyElement(glossarySearchInput), term);
        clickElement(By.id("gwt-debug-glossarySearchButton"));
        return new EditorPage(getDriver());
    }

    /**
     * There is usually a long poll waiting for GWTEventService events from the
     * server.
     *
     * @return
     */
    @Override
    protected int getExpectedBackgroundRequests() {
        return 1;
    }

    /**
     * First row is header: SourceTerm, TargetTerm, Action, Details.
     *
     * @return a table representing the searchResultTable
     */
    public List<List<String>> getGlossaryResultTable() {
        log.info("Query glossary results");
        return waitForAMoment()
                .until((Function<WebDriver, List<List<String>>>) webDriver -> {
                    if (webDriver.findElements(glossaryNoResult).size() == 1) {
                        return Collections.emptyList();
                    }
                    List<List<String>> resultTable = WebElementUtil
                            .getTwoDimensionList(webDriver, glossaryTable);
                    log.info("glossary result: {}", resultTable);
                    return resultTable;
                });
    }

    /**
     * Get content of a text flow source at given row. This assumes the text
     * flow has singular form (i.e. no plural). If a test requires to access
     * plural content, this can be changed.
     *
     * @param rowIndex
     *            row index
     * @return content of the source
     */
    public String getMessageSourceAtRowIndex(final int rowIndex) {
        log.info("Query text flow source at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT,
                Plurals.SourceSingular);
    }

    public String getMessageSourceAtRowIndex(int rowIndex, Plurals plural) {
        log.info("Query text flow source at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT, plural);
    }

    /**
     * Get content of a text flow target at given row. This assumes the text
     * flow has singular form (i.e. no plural). If a test requires to access
     * plural content, this can be changed.
     *
     * @param rowIndex
     *            row index
     * @return content of the target
     */
    public String getMessageTargetAtRowIndex(final int rowIndex) {
        log.info("Query text flow target at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, TARGET_ID_FMT,
                Plurals.TargetSingular);
    }

    private String getCodeMirrorContent(final long rowIndex,
            final String idFormat, final Plurals plurals) {
        return waitForAMoment()
                .until((Function<WebDriver, String>) webDriver -> {
                    // code mirror will turn text into list of <pre>.
                    List<WebElement> cmTextLines = webDriver
                            .findElement(By.id(String.format(idFormat, rowIndex,
                                    plurals.index())))
                            .findElements(By.tagName("pre"));
                    List<String> contents =
                            WebElementUtil.elementsToText(cmTextLines);
                    return Joiner.on("\n").skipNulls().join(contents);
                });
    }

    public EditorPage setSyntaxHighlighting(boolean option) {
        log.info("Set syntax highlight to {}", option);
        openConfigurationPanel();
        WebElement highlight = readyElement(By.id(EDITOR_SYNTAXHIGHLIGHT));
        if (highlight.isSelected() != option) {
            highlight.click();
        }
        return new EditorPage(getDriver());
    }

    private Boolean openConfigurationPanel() {
        log.info("Click to open Configuration options");
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            return getDriver().findElement(By.className("i--settings"))
                    .isEnabled();
        });
        new Actions(getDriver()).click(readyElement(configurationPanel))
                .perform();
        return waitForAMoment()
                .until((Function<WebDriver, Boolean>) webDriver -> {
                    return webDriver
                            .findElement(By.className(
                                    "gwt-TabLayoutPanelContentContainer"))
                            .isDisplayed();
                });
    }

    /**
     * Get content from a target using the non-CodeMirror configuration
     *
     * @param rowIndex
     * @return row target content
     */
    public String getBasicTranslationTargetAtRowIndex(final int rowIndex) {
        log.info("Query text flow source at {}", rowIndex);
        return getContentAtRowIndex(rowIndex, TARGET_ID_FMT,
                Plurals.TargetSingular);
    }

    public String getBasicTranslationTargetAtRowIndex(int rowIndex,
            Plurals plurals) {
        log.info("Query text flow source at {}", rowIndex);
        return getContentAtRowIndex(rowIndex, TARGET_ID_FMT, plurals);
    }

    public boolean expectBasicTranslationAtRowIndex(final int rowIndex,
            final String expected) {
        log.info("Wait for text flow target at {} to be {}", rowIndex,
                expected);
        return waitForAMoment()
                .until((Function<WebDriver, Boolean>) webDriver -> {
                    return getBasicTranslationTargetAtRowIndex(rowIndex)
                            .equals(expected);
                });
    }

    private String getContentAtRowIndex(final long rowIndex,
            final String idFormat, final Plurals plural) {
        return readyElement(
                By.id(String.format(idFormat, rowIndex, plural.index())))
                        .getAttribute("value");
    }

    /**
     * Translate a target using the non-CodeMirror field
     *
     * @param rowIndex
     * @param text
     * @return updated EditorPage
     */
    public EditorPage translateTargetAtRowIndex(final int rowIndex,
            String text) {
        log.info("Enter at {} the text {}", rowIndex, text);
        setTargetContent(rowIndex, text, TARGET_ID_FMT, Plurals.SourceSingular);
        return new EditorPage(getDriver());
    }

    private void setTargetContent(final long rowIndex, final String text,
            final String idFormat, final Plurals plural) {
        WebElement we = readyElement(
                By.id(String.format(idFormat, rowIndex, plural.index())));
        we.click();
        we.clear();
        we.sendKeys(text);
    }

    /**
     * Simulate a paste from the user's clipboard into the indicated row
     *
     * @param rowIndex
     *            row to enter text
     * @param text
     *            text to be entered
     * @return new EditorPage
     */
    public EditorPage pasteIntoRowAtIndex(final long rowIndex,
            final String text) {
        log.info("Paste at {} the text {}", rowIndex, text);
        WebElement we = readyElement(By.id(String.format(TARGET_ID_FMT,
                rowIndex, Plurals.SourceSingular.index())));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
        we.click();
        we.sendKeys(Keys.LEFT_CONTROL + "v");
        return new EditorPage(getDriver());
    }

    public EditorPage approveTranslationAtRow(int rowIndex) {
        log.info("Click Approve on row {}", rowIndex);
        readyElement(By.id(String.format(APPROVE_BUTTON_ID_FMT, rowIndex)))
                .click();
        slightPause();
        return new EditorPage(getDriver());
    }

    public EditorPage saveAsFuzzyAtRow(int rowIndex) {
        log.info("Click Fuzzy on row {}", rowIndex);
        readyElement(By.id(String.format(FUZZY_BUTTON_ID_FMT, rowIndex)))
                .click();
        return new EditorPage(getDriver());
    }

    public String getMessageTargetAtRowIndex(int rowIndex, Plurals plurals) {
        log.info("Query text flow target at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, TARGET_ID_FMT, plurals);
    }

    public String getStatistics() {
        log.info("Query statistics");
        return readyElement(By.id("gwt-debug-statistics-label")).getText();
    }

    public List<String> getMessageSources() {
        log.info("Query list of text flow sources");
        return WebElementUtil.elementsToText(readyElement(transUnitTable)
                .findElements(By.className("gwt-HTML")));
    }

    /**
     * Get the validation error messages for the currently translated row
     *
     * @return error string
     */
    public String getValidationMessageCurrentTarget() {
        log.info("Query validation messages on current item");
        waitForAMoment()
                .until((Function<WebDriver, Boolean>) webDriver -> !getTargetValidationBox()
                        .getText().isEmpty());
        return getTargetValidationBox().getText();
    }

    /**
     * Query whether the first validation messages box is displayed
     *
     * @return is/not displayed
     */
    public boolean isValidationMessageCurrentTargetVisible() {
        log.info("Query is validation message panel displayed");
        return getTargetValidationBox().isDisplayed();
    }

    /**
     * Wait for a delayed validation error panel to display
     */
    public void expectValidationErrorsVisible() {
        log.info("Wait for validation message panel displayed");
        waitForPageSilence();
        assertThat(isValidationMessageCurrentTargetVisible())
                .as("validation message panel displayed").isTrue();
    }

    /**
     * Click on the validation error box to view details
     *
     * @return new EditorPage
     */
    public EditorPage openValidationBox() {
        log.info("Click to open Validation panel");
        getTargetValidationBox().click();
        waitForAMoment().until((Function<WebDriver, Boolean>) webDriver -> {
            String errorText = getValidationMessageCurrentTarget();
            return errorText.contains("Unexpected")
                    || errorText.contains("Target");
        });
        return new EditorPage(getDriver());
    }

    /**
     * Opens the validation options sidebar
     *
     * @return new EditorPage
     */
    public EditorPage openValidationOptions() {
        log.info("Click to open Validation options panel");
        new Actions(getDriver())
                .click(readyElement(existingElement(By.id("container")),
                        validationOptions))
                .perform();
        existingElement(validationOptionsView);
        return new EditorPage(getDriver());
    }

    /**
     * Check if a validation option is available
     *
     * @param validation
     *            the option to check
     * @return new EditorPage
     */
    public boolean isValidationOptionAvailable(Validations validation) {
        log.info("Query is validation option {} available", validation);
        return readyElement(By.xpath(
                "//*[@title=\'" + getValidationTitle(validation) + "\']"))
                        .findElement(By.tagName("input")).isEnabled();
    }

    /**
     * Check if a validation option is selected
     *
     * @param validation
     *            the option to check
     * @return new EditorPage
     */
    public boolean isValidationOptionSelected(Validations validation) {
        log.info("Query is validation option {} selected", validation);
        return existingElement(
                existingElement(By.xpath("//*[@title=\'"
                        + getValidationTitle(validation) + "\']")),
                By.tagName("input")).isSelected();
    }

    /**
     * Click a validation option
     *
     * @param validation
     *            the option to click
     * @return new EditorPage
     */
    public EditorPage clickValidationCheckbox(Validations validation) {
        log.info("Click validation checkbox {}", validation);
        clickElement(readyElement(
                existingElement(By.xpath("//*[@title=\'"
                        + getValidationTitle(validation) + "\']")),
                By.tagName("input")));
        return new EditorPage(getDriver());
    }

    private String getValidationTitle(Validations validation) {
        switch (validation) {
        case HTML:
            return "Check that XML/HTML tags are consistent";

        case JAVAVARIABLES:
            return "Check that java style ({x}) variables are consistent";

        case NEWLINE:
            return "Check for consistent leading and trailing newline (\\n)";

        case POSITIONAL:
            return "Check that positional printf style (%n$x) variables are consistent";

        case PRINTF:
            return "Check that printf style (%x) variables are consistent";

        case TABS:
            return "Check whether source and target have the same number of tabs.";

        case XML:
            return "Check that XML entity are complete";

        default:
            throw new RuntimeException("Unknown validation!");

        }
    }

    public EditorPage inputFilterQuery(String query) {
        log.info("Enter filter query {}", query);
        readyElement(editorFilterField).clear();
        enterText(readyElement(editorFilterField), query + Keys.ENTER, true,
                false, false);
        return new EditorPage(getDriver());
    }

    public String getFilterQuery() {
        log.info("Query filter text");
        return readyElement(editorFilterField).getAttribute("value");
    }
    // Find the right side column for the selected row

    private WebElement getTranslationTargetColumn() {
        return
        // Right column
        readyElement(By.className("selected"))
                .findElements(By.className("transUnitCol")).get(1);
    }
    // Find the validation messages / errors box

    private WebElement getTargetValidationBox() {
        return existingElement(getTranslationTargetColumn(), validationBox);
    }
    // Click the History button for the selected row - row id must be known

    public EditorPage clickShowHistoryForRow(int row) {
        log.info("Click history button on row {}", row);
        readyElement(getTranslationTargetColumn(),
                By.id("gwt-debug-target-" + row + "-history")).click();
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            return getTranslationHistoryBox().isDisplayed();
        });
        return new EditorPage(getDriver());
    }

    public String getHistoryEntryAuthor(int entry) {
        log.info("Query author, action on history entry {}", entry);
        return getTranslationHistoryList().get(entry)
                .findElements(By.className("gwt-InlineHTML"))
                .get(0)
                .findElement(By.className("txt--meta"))
                .findElement(By.tagName("a"))
                .getText();
    }

    public String getHistoryEntryContent(int entry) {
        log.info("Query content on history entry {}", entry);
        return getTranslationHistoryList().get(entry)
                .findElements(By.className("gwt-InlineHTML")).get(1)
                .findElement(By.className("cm-s-default")).getText();
    }

    public EditorPage clickCompareOn(final int entry) {
        log.info("Click Compare on history entry {}", entry);
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            try {
                return getTranslationHistoryList().get(entry)
                        .findElement(By.linkText("Compare")).isDisplayed();
            } catch (IndexOutOfBoundsException ioobe) {
                return false;
            }
        });
        getTranslationHistoryList().get(entry)
                .findElement(By.linkText("Compare")).click();
        slightPause();
        return new EditorPage(getDriver());
    }

    public String getTranslationHistoryCompareTabtext() {
        log.info("Query history tab text");
        return getCompareTab().getText();
    }

    public EditorPage clickCompareVersionsTab() {
        log.info("Click on Compare versions tab");
        getCompareTab().click();
        readyElement(getTranslationHistoryBox(), By.className("html-face"));
        return new EditorPage(getDriver());
    }

    public String getComparisonTextInRow(int row) {
        log.info("Query comparison text in row {}", row);
        return getCompareTabEntries().get(row).findElement(By.tagName("pre"))
                .getText();
    }

    public List<String> getComparisonTextDiff() {
        log.info("Query diff from history compare");
        List<String> diffs = new ArrayList<>();
        for (WebElement element : getCompareTabEntries()) {
            for (WebElement diffElement : element
                    .findElements(By.className("diff-insert"))) {
                diffs.add("++" + diffElement.getText());
            }
            for (WebElement diffElement : element
                    .findElements(By.className("diff-delete"))) {
                diffs.add("--" + diffElement.getText());
            }
        }
        return diffs;
    }

    private WebElement getTranslationHistoryBox() {
        return existingElement(existingElement(By.id("gwt-debug-transHistory")),
                By.id("gwt-debug-transHistoryTabPanel"));
    }

    private List<WebElement> getTranslationHistoryList() {
        return getTranslationHistoryBox()
                .findElement(By.className("gwt-TabLayoutPanelContent"))
                .findElement(By.className("list--slat"))
                .findElements(By.className("l--pad-v-1"));
    }

    private WebElement getCompareTab() {
        return getTranslationHistoryBox()
                .findElement(By.className("gwt-TabLayoutPanelTabs"))
                .findElements(By.className("gwt-TabLayoutPanelTabInner"))
                .get(1);
    }

    private List<WebElement> getCompareTabEntries() {
        return
        // Second tab
        getTranslationHistoryBox()
                .findElements(By.className("gwt-TabLayoutPanelContent")).get(1)
                .findElements(By.className("textFlowEntry"));
    }
}
