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
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.util.LanguageList;

import java.util.List;

/**
 * This class represents the project language settings page.
 *
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ProjectLanguagesTab extends ProjectBasePage {

    private By activeLocales = By.id("active-locales-list");
    private By inactiveLocales = By.id("available-locales-list");
    private By disabledLocalesFilter = By.id(
            "settings-languages-form:available-locales-filter-input");

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
        return LanguageList.getListedLocales(waitForWebElement(activeLocales));
    }

    public ProjectLanguagesTab expectEnabledLocaleListCount(final int count) {
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getEnabledLocaleList().size() == count;
            }
        });
        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Get a list of disabled locales in this project
     *
     * @return String list of language/locale names
     */
    public List<String> getDisabledLocaleList() {
        log.info("Query enabled locales");
        return LanguageList.getListedLocales(waitForWebElement(inactiveLocales));
    }

    public ProjectLanguagesTab expectDisabledLocaleListCount(final int count) {
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDisabledLocaleList().size() == count;
            }
        });
        return new ProjectLanguagesTab(getDriver());
    }

    private List<WebElement> getDisabledLocaleListElement() {
        return waitForWebElement(inactiveLocales)
                .findElements(By.className("reveal"));
    }

    public ProjectLanguagesTab waitForLocaleListVisible() {
        log.info("Wait for locale list visible");
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return waitForWebElement(activeLocales).isDisplayed();
            }
        });
        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Enter text into the language search field
     * @param languageQuery text to search for
     * @return new language settings tab
     */
    public ProjectLanguagesTab enterSearchLanguage(String languageQuery) {
        log.info("Enter language search {}", languageQuery);
        waitForWebElement(disabledLocalesFilter).sendKeys(languageQuery);
        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Add a language to the languages list.
     *
     * @param searchLocaleId language to select
     * @return new language settings, anticipating the language has been added.
     */
    public ProjectLanguagesTab addLanguage(final String searchLocaleId) {
        log.info("Click Enable on {}", searchLocaleId);
        String message = "can not find locale - " + searchLocaleId;
        waitForAMoment().withMessage(message).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return LanguageList.toggleLanguageInList(
                        getDriver().findElement(inactiveLocales),
                        searchLocaleId);
            }
        });
        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return getEnabledLocaleList().contains(searchLocaleId);
            }
        });

        return new ProjectLanguagesTab(getDriver());
    }

    /**
     * Click the removal link for a language.
     *
     * @param searchLocaleId language to remove
     * @return new language settings tab
     */
    public ProjectLanguagesTab removeLocale(final String searchLocaleId) {
        log.info("Click Disable on {}", searchLocaleId);
        String message = "can not find locale - " + searchLocaleId;
        waitForAMoment().withMessage(message).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return LanguageList.toggleLanguageInList(
                        getDriver().findElement(activeLocales),
                        searchLocaleId);
            }
        });
        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return !getEnabledLocaleList().contains(searchLocaleId);
            }
        });

        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab clickLanguageActionsDropdown(String locale) {
        LanguageList.clickActionsDropdown(waitForWebElement(activeLocales),
                locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab clickAddAlias(String locale) {
        LanguageList.clickAddAlias(waitForWebElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab enterAliasForLocale(String locale, String alias) {
        LanguageList.enterAlias(waitForWebElement(activeLocales),
                locale, alias);
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectLanguagesTab saveLocaleAlias(String locale) {
        LanguageList.setAlias(waitForWebElement(activeLocales), locale);
        return new ProjectLanguagesTab(getDriver());
    }

    public String getAlias(String locale) {
        return LanguageList.getAliasForLocale(waitForWebElement(activeLocales),
                locale);
    }
}
