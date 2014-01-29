package org.zanata.page.webtrans;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.activation.CommandObject;
import javax.annotation.Nullable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

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
     *
     * @param rowIndex
     *            row index
     * @return content of the source
     */
    public String getTranslationSourceAtRowIndex(final int rowIndex) {
        return getCodeMirrorContent(rowIndex, SOURCE_ID_FMT, SINGULAR);
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
}
