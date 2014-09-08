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
package org.zanata.page.projectversion.versionsettings;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.projectversion.VersionBasePage;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * This class represents the project version settings tab for languages.
 *
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class VersionLanguagesTab extends VersionBasePage {

    @FindBy(id = "languageAutocomplete-autocomplete__input")
    private WebElement addNewLanguageField;

    public VersionLanguagesTab(WebDriver driver) {
        super(driver);
    }

    /**
     * Click the inherit project settings languages checkbox
     *
     * @return new language settings tab
     */
    public VersionLanguagesTab clickInheritCheckbox() {
        log.info("Click Inherit check box");
        getDriver().findElement(By.id("settings-languages-form"))
                .findElement(By.className("form__checkbox"))
                .click();
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab waitForLocaleListVisible() {
        log.info("Wait for locale list visible");
        waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return getDriver()
                        .findElement(By.id("settings-languages-form"))
                        .findElement(By.className("list--slat"))
                        .isDisplayed();
            }
        });
        return new VersionLanguagesTab(getDriver());
    }

    /**
     * Get a list of locales enabled in this version
     *
     * @return String list of language/locale names
     */
    public List<String> getEnabledLocaleList() {
        log.info("Query enabled locales list");
        List<String> rows =Lists.transform(getEnabledLocaleListElement(),
                new Function<WebElement, String>() {
                    @Override
                    public String apply(WebElement li) {
                        return li.getText();
                    }
                });
        return rows;
    }

    private List<WebElement> getEnabledLocaleListElement() {
        return getDriver().findElement(By.id("settings-languages-form"))
                .findElements(By.xpath(".//ul/li[@class='reveal--list-item']"));
    }

    public void waitForLanguagesContains(String language) {
        log.info("Wait for languages contains {}", language);
        waitForLanguageEntryExpected(language, true);
    }

    public void waitForLanguagesNotContains(String language) {
        log.info("Wait for languages does not contain {}", language);
        waitForLanguageEntryExpected(language, false);
    }

    private void waitForLanguageEntryExpected(final String language,
                                              final boolean exists) {
        waitForTenSec().until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return getEnabledLocaleList().contains(language) == exists;
            }
        });
    }

    public VersionLanguagesTab enterSearchLanguage(String localeQuery) {
        log.info("Enter search language {}", localeQuery);
        WebElementUtil.searchAutocomplete(getDriver(), "languageAutocomplete",
                localeQuery);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab removeLocale(final String localeId) {
        log.info("Click Remove on {}", localeId);
        boolean removedLocale = false;
        for (WebElement localeLi : getEnabledLocaleListElement()) {
            String displayedLocaleId =
                    localeLi.findElement(By.xpath(".//span")).getText()
                            .replace("[", "").replace("]", "");
            if (displayedLocaleId.equals(localeId)) {
                localeLi.findElement(By.tagName("a")).click();
                removedLocale = true;
                break;
            }
        }
        Preconditions.checkState(removedLocale, "can not remove locale: %s",
                localeId);

        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return !getEnabledLocaleList().contains(localeId);
            }
        });

        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab addLocale(final String localeId) {
        log.info("Click to add {}", localeId);
        String message = "can not find locale - " + localeId;
        waitForTenSec().withMessage(message).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                List<WebElement> searchResults =
                        WebElementUtil.getSearchAutocompleteResults(driver,
                                "settings-languages-form",
                                "languageAutocomplete");

                for (WebElement searchResult : searchResults) {
                    if (searchResult.getText().contains(localeId)) {
                        searchResult.click();
                        return true;
                    }
                }
                return false;
            }
        });

        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return getEnabledLocaleList().contains(localeId);
            }
        });

        return new VersionLanguagesTab(getDriver());
    }

}
