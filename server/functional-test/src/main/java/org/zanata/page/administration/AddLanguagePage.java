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

public class AddLanguagePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AddLanguagePage.class);
    private By saveButton = By.id("btn-new-language-save");
    private By cancelButton = By.id("btn-new-language-cancel");
    private By localeId = By.className("react-autosuggest__input");
    private By suggestList =
            By.className("react-autosuggest__suggestions-list");
    private By suggestRow = By.className("react-autosuggest__suggestion");
    private By enabledByDefaultCheckbox = By.id("chk-new-language-enabled");
    private By pluralsWarning = By.id("new-language-pluralforms-warning");

    public AddLanguagePage(final WebDriver driver) {
        super(driver);
    }

    public AddLanguagePage enterSearchLanguage(String language) {
        log.info("Enter language {}", language);
        enterText(readyElement(localeId), language);
        // Pause for a moment, as quick actions can break here
        slightPause();
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage selectSearchLanguage(final String language) {
        log.info("Select language {}", language);
        waitForAMoment().until((Predicate<WebDriver>) driver -> {
            List<WebElement> suggestions =
                    existingElement(suggestList).findElements(suggestRow);
            boolean clickedLanguage = false;
            for (WebElement row : suggestions) {
                if (row.findElement(By.name("new-language-displayName"))
                        .getText().contains(language)) {
                    row.click();
                    clickedLanguage = true;
                    break;
                }
            }
            return clickedLanguage;
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
            clickElement(enabledByDefaultCheckbox);
        }
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage disableLanguageByDefault() {
        log.info("Click Disable by default");
        if (readyElement(enabledByDefaultCheckbox).isSelected()) {
            clickElement(enabledByDefaultCheckbox);
        }
        return new AddLanguagePage(getDriver());
    }

    public String getNewLanguageName() {
        return existingElement(By.id("new-language-name"))
                .getAttribute("value");
    }

    public String getNewLanguageNativeName() {
        return existingElement(By.id("new-language-nativeName"))
                .getAttribute("value");
    }

    public String getNewLanguageCode() {
        return existingElement(localeId).getAttribute("value");
    }

    public LanguagesPage saveLanguage() {
        log.info("Click Save");
        clickAndCheckErrors(readyElement(saveButton));
        return new LanguagesPage(getDriver());
    }
}
