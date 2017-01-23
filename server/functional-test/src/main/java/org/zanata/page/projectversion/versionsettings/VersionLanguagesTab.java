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
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projectversion.VersionBasePage;
import org.zanata.util.LanguageList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class represents the project version settings tab for languages.
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class VersionLanguagesTab extends VersionBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VersionLanguagesTab.class);
    private By languagesSettingForm = By.id("settings-languages-form");
    private By activeLocales = By.id("activeLocales-list");
    private By disabledLocales = By.id("availableLocales-list");
    private By activeLocalesFilter =
            By.id("settings-languages-form:activeLocales-filter-input");
    private By disabledLocalesFilter =
            By.id("settings-languages-form:availableLocales-filter-input");

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
        readyElement(readyElement(languagesSettingForm),
                By.className("form__checkbox")).click();
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab expectLocaleListVisible() {
        log.info("Wait for locale list visible");
        waitForPageSilence();
        WebElement el = readyElement(languagesSettingForm)
                .findElement(By.className("list--slat"));
        assertThat(el.isDisplayed()).as("displayed").isTrue();
        return new VersionLanguagesTab(getDriver());
    }

    /**
     * Get a list of locales enabled in this version
     *
     * @return String list of language/locale names
     */
    public List<String> getEnabledLocaleList() {
        log.info("Query enabled locales list");
        return LanguageList.getListedLocales(existingElement(activeLocales));
    }

    /**
     * Get a list of locales available for this version
     *
     * @return String list of language/locale names
     */
    public List<String> getAvailableLocaleList() {
        log.info("Query available locales");
        return LanguageList.getListedLocales(existingElement(disabledLocales));
    }

    public VersionLanguagesTab expectEnabledLocaleListCount(final int count) {
        waitForAMoment()
                .until((Predicate<WebDriver>) input -> getEnabledLocaleList()
                        .size() == count);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab expectAvailableLocaleListCount(final int count) {
        waitForAMoment()
                .until((Predicate<WebDriver>) input -> getAvailableLocaleList()
                        .size() == count);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab expectLanguagesContains(String language) {
        log.info("Wait for languages contains {}", language);
        waitForPageSilence();
        assertThat(getEnabledLocaleList()).as("enabled locales list")
                .contains(language);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab waitForLanguagesNotContains(String language) {
        log.info("Wait for languages does not contain {}", language);
        waitForLanguageEntryExpected(language, false);
        return new VersionLanguagesTab(getDriver());
    }

    private void waitForLanguageEntryExpected(final String language,
            final boolean exists) {
        waitForAMoment()
                .until((Function<WebDriver, Boolean>) driver -> getEnabledLocaleList()
                        .contains(language) == exists);
    }

    public VersionLanguagesTab filterDisabledLanguages(String localeQuery) {
        log.info("Filter disabled languages for: {}", localeQuery);
        readyElement(disabledLocalesFilter).clear();
        enterText(readyElement(disabledLocalesFilter), localeQuery);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab filterEnabledLanguages(String localeQuery) {
        log.info("Filter enabled languages for: {}", localeQuery);
        readyElement(activeLocalesFilter).clear();
        enterText(readyElement(activeLocalesFilter), localeQuery);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab removeLocale(final String localeId) {
        log.info("Click Disable on {}", localeId);
        String message = "can not find locale - " + localeId;
        waitForAMoment().withMessage(message)
                .until((Predicate<WebDriver>) driver -> LanguageList
                        .toggleLanguageInList(
                                getDriver().findElement(activeLocales),
                                localeId));
        refreshPageUntil(this,
                (Predicate<WebDriver>) driver -> !getEnabledLocaleList()
                        .contains(localeId),
                "Wait for the locale list to not contain " + localeId);
        return new VersionLanguagesTab(getDriver());
    }

    public VersionLanguagesTab addLocale(final String localeId) {
        log.info("Click Enable on {}", localeId);
        String message = "can not find locale - " + localeId;
        waitForAMoment().withMessage(message)
                .until((Predicate<WebDriver>) driver -> LanguageList
                        .toggleLanguageInList(
                                getDriver().findElement(disabledLocales),
                                localeId));
        refreshPageUntil(this,
                (Predicate<WebDriver>) driver -> getEnabledLocaleList()
                        .contains(localeId),
                "Wait for the locale list to contain " + localeId);
        return new VersionLanguagesTab(getDriver());
    }
}
