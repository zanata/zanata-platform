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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class EditorPage extends BasePage {
    // first %d is row index, second %d is plural form index (i.e. 0 or 1)
    private static final String SOURCE_ID_FMT =
            "gwt-debug-%d-source-panel-%d-container";
    private static final int SINGULAR = 0;
    private static final int PLURAL = 1;

    // first %d is row index, second %d is plural form index (i.e. 0-6)
    private static final String TARGET_ID_FMT = "gwt-debug-%d-target-%d";

    private final By glossaryTableBy = By.id("gwt-debug-glossaryResultTable");
    private final By glossaryNoResultBy = By.id("gwt-debug-glossaryNoResult");

    @FindBy(id = "gwt-debug-transUnitTable")
    private WebElement transUnitTable;

    public EditorPage(WebDriver driver) {
        super(driver);
    }

    public EditorPage searchGlossary(final String term) {
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
     * TODO pahuang rename method name
     *
     * @param rowIndex
     *            row index
     * @return content of the source
     */
    public String getTranslationSourceAtRowIndex(final int rowIndex) {
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT, SINGULAR);
    }

    public String getMessageSourceAtRowIndex(int rowIndex, int pluralIndex) {
        Preconditions.checkArgument(
                pluralIndex == SINGULAR || pluralIndex == PLURAL,
                "plural index must be 0 or 1");
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT, pluralIndex);
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
    public String getTranslationTargetAtRowIndex(final int rowIndex) {
        return getCodeMirrorContent(rowIndex, TARGET_ID_FMT, SINGULAR);
    }

    private String getCodeMirrorContent(final long rowIndex,
            final String idFormat, final int pluralIndex) {
        return waitForTenSec().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver input) {
                // code mirror will turn text into list of <pre>.
                List<WebElement> cmTextLines = input.findElement(
                        By.id(String.format(idFormat, rowIndex, pluralIndex)))
                        .findElements(By.tagName("pre"));
                List<String> contents =
                        WebElementUtil.elementsToText(cmTextLines);
                return Joiner.on("\n").skipNulls().join(contents);
            }
        });
    }

    public EditorPage setSyntaxHighlighting(boolean option) {
        openConfigurationPanel();
        if (getDriver().findElement(By.id("gwt-uid-144")).isSelected() != option) {
            getDriver().findElement(By.id("gwt-uid-144")).click();
        }
        return new EditorPage(getDriver());
    }

    private Boolean openConfigurationPanel() {
        getDriver().findElement(By.className("icon-cog")).click();
        return waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return input.findElement(
                        By.className("gwt-TabLayoutPanelContentContainer"))
                        .isDisplayed();
            }
        });

    }

    public String getBasicTranslationTargetAtRowIndex(final int rowIndex) {
        return getContentAtRowIndex(rowIndex, TARGET_ID_FMT, SINGULAR);
    }

    /**
     * Get content from a target using the non-CodeMirror configuration
     * @param rowIndex
     * @return row target content
     */
    private String getContentAtRowIndex(final long rowIndex,
                                        final String idFormat,
                                        final int pluralIndex) {
        return waitForTenSec().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver input) {
                return input.findElement(By.id(String.format(idFormat, rowIndex, pluralIndex))).getAttribute("value");
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
        setTargetContent(rowIndex, text, TARGET_ID_FMT, SINGULAR);
        return new EditorPage(getDriver());
    }

    private void setTargetContent(final long rowIndex, final String text,
            final String idFormat, final int pluralIndex) {
        WebElement we = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(
                        By.id(String.format(idFormat, rowIndex, pluralIndex)));
            }
        });
        we.click();
        we.sendKeys(text);
    }

    /**
     * Press the Approve button for the currently selected translation row
     * @return new Editor page object
     */
    public EditorPage approveSelectedTranslation() {
        WebElement approve = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(By.className("selected"))
                        .findElement(By.className("icon-install"));
            }
        });
        approve.click();
        return new EditorPage(getDriver());
    }

    public EditorPage setSyntaxHighlighting(boolean option) {
        openConfigurationPanel();
        if (getDriver().findElement(By.id("gwt-uid-144")).isSelected() != option) {
            getDriver().findElement(By.id("gwt-uid-144")).click();
        }
        return new EditorPage(getDriver());
    }

    private Boolean openConfigurationPanel() {
        getDriver().findElement(By.className("icon-cog")).click();
        return waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return input.findElement(
                        By.className("gwt-TabLayoutPanelContentContainer"))
                        .isDisplayed();
            }
        });

    }

    public String getBasicTranslationTargetAtRowIndex(final int rowIndex) {
        return getContentAtRowIndex(rowIndex, TARGET_ID_FMT, SINGULAR);
    }

    /**
     * Get content from a target using the non-CodeMirror configuration
     * @param rowIndex
     * @return row target content
     */
    private String getContentAtRowIndex(final long rowIndex,
            final String idFormat,
            final int pluralIndex) {
        return waitForTenSec().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver input) {
                return input.findElement(By.id(String.format(idFormat, rowIndex, pluralIndex))).getAttribute("value");
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
        setTargetContent(rowIndex, text, TARGET_ID_FMT, SINGULAR);
        return new EditorPage(getDriver());
    }

    private void setTargetContent(final long rowIndex, final String text,
            final String idFormat, final int pluralIndex) {
        WebElement we = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(
                        By.id(String.format(idFormat, rowIndex, pluralIndex)));
            }
        });
        we.click();
        we.clear();
        we.sendKeys(text);
    }

    /**
     * Press the Approve button for the currently selected translation row
     * @return new Editor page object
     */
    public EditorPage approveSelectedTranslation() {
        WebElement approve = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(By.className("selected"))
                        .findElement(By.className("icon-install"));
            }
        });
        approve.click();
        return new EditorPage(getDriver());
    }

    /**
     * Press the Save as Fuzzy button for the currently selected translation row
     * @return new Editor page object
     */
    public EditorPage saveAsFuzzySelectedTranslation() {
        WebElement approve = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return input.findElement(By.className("selected"))
                        .findElement(By.className("icon-flag-1"));
            }
        });
        approve.click();
        return new EditorPage(getDriver());
    }

    public String getMessageTargetAtRowIndex(int rowIndex, int pluralIndex) {
        Preconditions.checkArgument(pluralIndex >= 0 && pluralIndex <= 6,
                "plural index must be in range [0,6]");
        return getCodeMirrorContent(rowIndex, TARGET_ID_FMT, pluralIndex);
    }
}
