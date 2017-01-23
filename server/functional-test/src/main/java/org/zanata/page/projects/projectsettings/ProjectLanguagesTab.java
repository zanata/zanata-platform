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
package org.zanata.page.projects.projectsettings;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.util.LanguageList;
import java.util.List;

/**
 * This class represents the project language settings page.
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectLanguagesTab extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectLanguagesTab.class);
    private By activeLocales = By.id("activeLocales-list");
    private By enabledLocalesFilter =
            By.id("settings-languages-form:activeLocales-filter-input");
    private By disabledLocales = By.id("availableLocales-list");
    private By disabledLocalesFilter =
            By.id("settings-languages-form:availableLocales-filter-input");

    public ProjectLanguagesTab(WebDriver driver) {
        super(driver);
    }

    /**
     * Get a list of locales enabled in this project
     *
     * @return String list of language/locale names
     */
    public List<String> getEnabledLocaleList() {
        log.info("Query enabled locales");
        return LanguageList.getListedLocales(existingElement(activeLocales));
    }

    /**
     * Get a list of locales available for this project
     *
     * @return String list of language/locale names
     */
    public List<String> getAvailableLocaleList() {
        log.info("Query available locales");
        return LanguageList.getListedLocales(existingElement(disabledLocales));
    }

    public ProjectLanguagesTab expectEnabledLocaleListCount(final int count) {
        waitForAMoment()
                .until((Predicate<WebDriver>) input -> getEnabledLocaleList()
                        .size() == count);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab expectAvailableLocaleListCount(final int count) {
        waitForAMoment()
                .until((Predicate<WebDriver>) webDriver -> getAvailableLocaleList()
                        .size() == count);
        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Enter text into the disabled language filter field
     *
     * @param languageQuery
     *            text to filter by
     * @return new language settings tab
     */
    public ProjectLanguagesTab filterDisabledLanguages(String languageQuery) {
        log.info("Filter disabled languages for: {}", languageQuery);
        enterText(readyElement(disabledLocalesFilter), languageQuery);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab filterEnabledLanguages(String languageQuery) {
        log.info("Filter enabled languages for: {}", languageQuery);
        enterText(readyElement(enabledLocalesFilter), languageQuery);
        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Add a language to the languages list.
     *
     * @param searchLocaleId
     *            language to select
     * @return new language settings, anticipating the language has been added.
     */
    public ProjectLanguagesTab addLanguage(final String searchLocaleId) {
        log.info("Click Enable on {}", searchLocaleId);
        String message = "can not find locale - " + searchLocaleId;
        waitForAMoment().withMessage(message)
                .until((Predicate<WebDriver>) driver -> {
                    return LanguageList.toggleLanguageInList(
                            getDriver().findElement(disabledLocales),
                            searchLocaleId);
                });
        refreshPageUntil(this, (Predicate<WebDriver>) driver -> {
            return getEnabledLocaleList().contains(searchLocaleId);
        }, "Wait for the locale list to contain " + searchLocaleId);
        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Click the removal link for a language.
     *
     * @param searchLocaleId
     *            language to remove
     * @return new language settings tab
     */
    public ProjectLanguagesTab removeLocale(final String searchLocaleId) {
        log.info("Click Disable on {}", searchLocaleId);
        String message = "can not find locale - " + searchLocaleId;
        waitForAMoment().withMessage(message)
                .until((Predicate<WebDriver>) driver -> {
                    return LanguageList.toggleLanguageInList(
                            getDriver().findElement(activeLocales),
                            searchLocaleId);
                });
        refreshPageUntil(this,
                (Predicate<WebDriver>) driver -> !getEnabledLocaleList()
                        .contains(searchLocaleId),
                "Wait for the locale list to not contain " + searchLocaleId);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab clickLanguageActionsDropdown(String locale) {
        LanguageList.clickActionsDropdown(readyElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab clickAddAlias(String locale) {
        LanguageList.clickAddAlias(readyElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab clickEditAlias(String locale) {
        LanguageList.clickEditAlias(readyElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab enterAliasForLocale(String locale,
            String alias) {
        LanguageList.enterAlias(readyElement(activeLocales), locale, alias);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab saveLocaleAlias(String locale) {
        LanguageList.setAlias(readyElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab deleteAlias(String locale) {
        LanguageList.unsetAlias(readyElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public String getAlias(String locale) {
        return LanguageList.getAliasForLocale(readyElement(activeLocales),
                locale);
    }
}
