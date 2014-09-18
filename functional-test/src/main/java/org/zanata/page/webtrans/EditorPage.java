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
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class EditorPage extends BasePage {

    public enum Validations {
        HTML, JAVAVARIABLES, NEWLINE, POSITIONAL, PRINTF, TABS, XML
    }

    // first %d is row index, second %d is plural form index (i.e. 0 or 1)
    private static final String SOURCE_ID_FMT =
            "gwt-debug-%d-source-panel-%d-container";

    // first %d is row index, second %d is plural form index (i.e. 0-6)
    private static final String TARGET_ID_FMT = "gwt-debug-%d-target-%d";

    // buttons id format
    private static final String APPROVE_BUTTON_ID_FMT =
            "gwt-debug-target-%d-save-approve";
    private static final String FUZZY_BUTTON_ID_FMT =
            "gwt-debug-target-%d-save-fuzzy";

    private final By glossaryTableBy = By.id("gwt-debug-glossaryResultTable");
    private final By glossaryNoResultBy = By.id("gwt-debug-glossaryNoResult");

    private By transUnitTableBy = By.id("gwt-debug-transUnitTable");
    @FindBy(id = "gwt-debug-editor-filter-box")
    private WebElement editorFilterField;

    public EditorPage(WebDriver driver) {
        super(driver);
    }

    public EditorPage searchGlossary(final String term) {
        log.info("Search glossary for {}", term);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return input.findElements(glossaryNoResultBy).size() == 1
                        || input.findElements(glossaryTableBy).size() == 1;
            }
        });
        WebElement searchBox =
                getDriver().findElement(By.id("gwt-debug-glossaryTextBox"));
        searchBox.clear();
        searchBox.sendKeys(term);
        WebElement searchButton =
                getDriver()
                        .findElement(By.id("gwt-debug-glossarySearchButton"));
        searchButton.click();
        return this;
    }

    /**
     * First row is header: SourceTerm, TargetTerm, Action, Details.
     *
     * @return a table representing the searchResultTable
     */
    public List<List<String>> getGlossaryResultTable() {
        log.info("Query glossary results");
        return waitForTenSec().until(
                new Function<WebDriver, List<List<String>>>() {
                    @Override
                    public List<List<String>> apply(WebDriver input) {
                        if (input.findElements(glossaryNoResultBy).size() == 1) {
                            return Collections.emptyList();
                        }
                        List<List<String>> resultTable =
                                WebElementUtil.getTwoDimensionList(input,
                                        glossaryTableBy);
                        log.info("glossary result: {}", resultTable);
                        return resultTable;
                    }
                });
    }

    /**
     * Get content of a text flow source at given row.
     * This assumes the text flow has singular form (i.e. no plural).
     * If a test requires to access plural content, this can be changed.
     *
     * @param rowIndex
     *            row index
     * @return content of the source
     */
    public String getMessageSourceAtRowIndex(final int rowIndex) {
        log.info("Query text flow source at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT, Plurals.SourceSingular);
    }

    public String getMessageSourceAtRowIndex(int rowIndex, Plurals plural) {
        log.info("Query text flow source at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT, plural);
    }

    /**
     * Get content of a text flow target at given row.
     * This assumes the text flow has singular form (i.e. no plural).
     * If a test requires to access plural content, this can be changed.
     *
     * @param rowIndex
     *            row index
     * @return content of the target
     */
    public String getMessageTargetAtRowIndex(final int rowIndex) {
        log.info("Query text flow target at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, TARGET_ID_FMT, Plurals.TargetSingular);
    }

    private String getCodeMirrorContent(final long rowIndex,
            final String idFormat, final Plurals plurals) {
        return waitForTenSec().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver input) {
                // code mirror will turn text into list of <pre>.
                List<WebElement> cmTextLines = input.findElement(
                        By.id(String.format(idFormat, rowIndex, plurals.index())))
                        .findElements(By.tagName("pre"));
                List<String> contents =
                        WebElementUtil.elementsToText(cmTextLines);
                return Joiner.on("\n").skipNulls().join(contents);
            }
        });
    }

    public EditorPage setSyntaxHighlighting(boolean option) {
        log.info("Set syntax highlight to {}", option);
        openConfigurationPanel();
        WebElement highlight = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                WebElement element = getDriver()
                        .findElement(By.id("gwt-uid-143"));
                if (element.isDisplayed()) {
                    return element;
                }
                return null;
            }
        });
        if (highlight.isSelected() != option) {
            highlight.click();
        }
        return new EditorPage(getDriver());
    }

    private Boolean openConfigurationPanel() {
        log.info("Click to open Configuration options");
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.className("i--settings"))
                        .isEnabled();
            }
        });
        new Actions(getDriver()).click(
                getDriver().findElement(By.className("i--settings"))).perform();
        return waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return input.findElement(
                        By.className("gwt-TabLayoutPanelContentContainer"))
                        .isDisplayed();
            }
        });

    }

    /**
     * Get content from a target using the non-CodeMirror configuration
     * @param rowIndex
     * @return row target content
     */
    public String getBasicTranslationTargetAtRowIndex(final int rowIndex) {
        log.info("Query text flow source at {}", rowIndex);
        return getContentAtRowIndex(rowIndex, TARGET_ID_FMT, Plurals.TargetSingular);
    }

    public String getBasicTranslationTargetAtRowIndex(int rowIndex, Plurals plurals) {
        log.info("Query text flow source at {}", rowIndex);
        return getContentAtRowIndex(rowIndex, TARGET_ID_FMT, plurals);
    }

    public boolean expectBasicTranslationAtRowIndex(final int rowIndex,
                                                    final String expected) {
        log.info("Wait for text flow target at {} to be {}", rowIndex, expected);
        return waitForTenSec().until(new  Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return getBasicTranslationTargetAtRowIndex(rowIndex).equals(expected);
            }
        });
    }

    private String getContentAtRowIndex(final long rowIndex,
            final String idFormat,
            final Plurals plural) {
        return waitForTenSec().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver input) {
                return input.findElement(
                        By.id(String.format(idFormat, rowIndex, plural.index())))
                        .getAttribute("value");
            }
        });
    }

    /**
     * Translate a target using the non-CodeMirror field
     * @param rowIndex
     * @param text
     * @return updated EditorPage
     */
    public EditorPage translateTargetAtRowIndex(final int rowIndex, String text) {
        log.info("Enter at {} the text {}", rowIndex, text);
        setTargetContent(rowIndex, text, TARGET_ID_FMT, Plurals.SourceSingular);
        return new EditorPage(getDriver());
    }

    private void setTargetContent(final long rowIndex, final String text,
            final String idFormat, final Plurals plural) {
        WebElement we = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(
                        By.id(String.format(idFormat, rowIndex, plural.index())));
            }
        });
        we.click();
        we.clear();
        we.sendKeys(text);
    }

    /**
     * Simulate a paste from the user's clipboard into the indicated row
     *
     * @param rowIndex row to enter text
     * @param text text to be entered
     * @return new EditorPage
     */
    public EditorPage pasteIntoRowAtIndex(final long rowIndex,
                                          final String text) {
        log.info("Paste at {} the text {}", rowIndex, text);
        WebElement we = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(By.id(String.format(TARGET_ID_FMT,
                        rowIndex, Plurals.SourceSingular.index())));
            }
        });
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
        we.click();
        we.sendKeys(Keys.LEFT_CONTROL + "v");
        return new EditorPage(getDriver());
    }

    public EditorPage approveTranslationAtRow(int rowIndex) {
        log.info("Click Approve on row {}", rowIndex);
        WebElement button = getDriver()
                .findElement(By.id(String.format(APPROVE_BUTTON_ID_FMT, rowIndex)));
        button.click();
        slightPause();
        return this;
    }

    public EditorPage saveAsFuzzyAtRow(int rowIndex) {
        log.info("Click Fuzzy on row {}", rowIndex);
        WebElement button = getDriver()
                .findElement(By.id(String.format(FUZZY_BUTTON_ID_FMT, rowIndex)));
        button.click();
        return this;
    }

    public String getMessageTargetAtRowIndex(int rowIndex, Plurals plurals) {
        log.info("Query text flow target at {}", rowIndex);
        return getCodeMirrorContent(rowIndex, TARGET_ID_FMT, plurals);
    }

    public String getStatistics() {
        log.info("Query statistics");
        return getDriver().findElement(By.id("gwt-debug-statistics-label"))
                .getText();
    }

    public List<String> getMessageSources() {
        log.info("Query list of text flow sources");
        List<WebElement> sources = getDriver().findElement(transUnitTableBy)
                .findElements(By.className("sourceTable"));
        return WebElementUtil.elementsToText(sources);
    }

    /**
     * Get the validation error messages for the currently translated row
     * @return error string
     */
    public String getValidationMessageCurrentTarget() {
        log.info("Query validation messages on current item");
        waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                WebElement webElement = getTargetValidationBox();
                return webElement.isDisplayed()
                        && !webElement.getText().isEmpty();
            }
        });
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
    public void waitForValidationErrorsVisible() {
        log.info("Wait for validation message panel displayed");
        waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return isValidationMessageCurrentTargetVisible();
            }
        });
    }

    /**
     * Click on the validation error box to view details
     *
     * @return new EditorPage
     */
    public EditorPage openValidationBox() {
        log.info("Click to open Validation panel");
        getTargetValidationBox().click();
        waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                String errorText = getValidationMessageCurrentTarget();
                return errorText.contains("Unexpected")
                        || errorText.contains("Target");
            }
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
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("container"))
                        .findElement(By.className("new-zanata"))
                        .findElement(By.xpath("//a[@title='Validation options']"))
                        .isEnabled();
            }
        });
        new Actions(getDriver()).click(
                getDriver().findElement(By.id("container"))
                        .findElement(By.className("new-zanata"))
                        .findElement(By.xpath("//a[@title='Validation options']")))
                .perform();
        waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return getDriver().findElement(By.id("validationOptionsView"))
                        .isDisplayed();
            }
        });
        return new EditorPage(getDriver());
    }

    /**
     * Check if a validation option is available
     *
     * @param validation the option to check
     * @return new EditorPage
     */
    public boolean isValidationOptionAvailable(Validations validation) {
        log.info("Query is validation option {} available", validation);
        return getDriver()
                .findElement(By.xpath("//*[@title='" +
                        getValidationTitle(validation) + "']"))
                .findElement(By.tagName("input"))
                .isEnabled();
    }

    /**
     * Check if a validation option is selected
     *
     * @param validation the option to check
     * @return new EditorPage
     */
    public boolean isValidationOptionSelected(Validations validation) {
        log.info("Query is validation option {} selected", validation);
        return getDriver()
                .findElement(By.xpath("//*[@title='" +
                        getValidationTitle(validation) + "']"))
                .findElement(By.tagName("input"))
                .isSelected();
    }

    /**
     * Click a validation option
     * @param validation
     *            the option to click
     * @return new EditorPage
     */
    public EditorPage clickValidationCheckbox(Validations validation) {
        log.info("Click validation checkbox {}", validation);
        getDriver().findElement(By.xpath("//*[@title='" +
                        getValidationTitle(validation) + "']"))
                .findElement(By.tagName("input"))
                .click();
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
                return "Check that positional printf style " +
                        "(%n$x) variables are consistent";
            case PRINTF:
                return "Check that printf style (%x) variables are consistent";
            case TABS:
                return "Check whether source and target have the same " +
                        "number of tabs.";
            case XML:
                return "Check that XML entity are complete";
            default:
                throw new RuntimeException("Unknown validation!");
        }
    }

    public EditorPage inputFilterQuery(String query) {
        log.info("Enter filter query {}", query);
        editorFilterField.clear();
        editorFilterField.sendKeys(query + Keys.ENTER);
        return this;
    }

    public String getFilterQuery() {
        log.info("Query filter text");
        return editorFilterField.getAttribute("value");
    }

    private WebElement getTranslationTargetColumn() {
        return getDriver().findElement(By.className("selected"))
                .findElements(By.className("transUnitCol"))
                .get(1); // Right column
    }

    private WebElement getTargetValidationBox() {
        return getTranslationTargetColumn()
                .findElement(By.className("gwt-DisclosurePanel"));
    }

}
