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
package org.zanata.page.administration;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.util.Checkbox;
import org.zanata.util.WebElementUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddLanguagePage extends BasePage {

    private By saveButton = By.id("addLanguageForm:save-button");
    private By enabledByDefaultCheckbox = By.id("addLanguageForm:enabled");
    private By languageInfo = By.id("addLanguageForm:output");
    private By languageInfoItem = By.className("txt--meta");
    private By pluralsWarning = By.id("addLanguageForm:localeNameMsgs");

    public AddLanguagePage(final WebDriver driver) {
        super(driver);
    }

    public AddLanguagePage enterSearchLanguage(String language) {
        log.info("Enter language {}", language);
        WebElementUtil.searchAutocomplete(getDriver(),
                "localeAutocomplete", language);
        // Pause for a moment, as quick actions can break here
        slightPause();
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage selectSearchLanguage(final String language) {
        log.info("Select language {}", language);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                List<WebElement> searchResults =
                        WebElementUtil.getSearchAutocompleteResults(
                                driver,
                                "addLanguageForm",
                                "localeAutocomplete");
                boolean clickedLanguage = false;
                for (WebElement searchResult : searchResults) {
                    if (searchResult.getText().contains(language)) {
                        searchResult.click();
                        clickedLanguage = true;
                        break;
                    }
                }
                return clickedLanguage;
            }
        });
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage expectPluralsWarning() {
        log.info("Expect plurals warning");
        waitForPageSilence();
        readyElement(pluralsWarning);
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage enableLanguageByDefault() {
        log.info("Click Enable by default");
        if (!readyElement(enabledByDefaultCheckbox).isSelected()) {
            readyElement(enabledByDefaultCheckbox).click();
        }
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage disableLanguageByDefault() {
        log.info("Click Disable by default");
        if (readyElement(enabledByDefaultCheckbox).isSelected()) {
            readyElement(enabledByDefaultCheckbox).click();
        }

        return new AddLanguagePage(getDriver());
    }

    public Map<String, String> getLanguageDetails() {
        log.info("Query language details");
        Map<String, String> map = new HashMap();
        // Wait for the fields to be populated
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !existingElement(languageInfo)
                        .findElements(By.className("l--push-top-half"))
                        .get(0).findElement(languageInfoItem)
                        .getText().isEmpty();
            }
        });
        for (WebElement item : existingElement(languageInfo)
                .findElements(By.className("l--push-top-half"))) {
            String name = item.getText();
            String value = item.findElement(languageInfoItem).getText();
            // Truncate name at value
            int cutoff = name.lastIndexOf(value);
            name = name.substring(0, cutoff).trim();
            map.put(name, value);
        }
        return map;
    }

    public LanguagesPage saveLanguage() {
        log.info("Click Save");
        clickAndCheckErrors(readyElement(saveButton));
        return new LanguagesPage(getDriver());
    }
}
