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
package org.zanata.page.projects.projectsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.projects.ProjectBasePage
import org.zanata.util.LanguageList

/**
 * This class represents the project language settings page.
 *
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectLanguagesTab(driver: WebDriver) : ProjectBasePage(driver) {
    private val activeLocales = By.id("activeLocales-list")
    private val enabledLocalesFilter = By.id("settings-languages-form:activeLocales-filter-input")
    private val disabledLocales = By.id("availableLocales-list")
    private val disabledLocalesFilter = By.id("settings-languages-form:availableLocales-filter-input")

    /**
     * Get a list of locales enabled in this project
     *
     * @return String list of language/locale names
     */
    val enabledLocaleList: List<String>
        get() {
            log.info("Query enabled locales")
            return LanguageList.getListedLocales(existingElement(activeLocales))
        }

    /**
     * Get a list of locales available for this project
     *
     * @return String list of language/locale names
     */
    private val availableLocaleList: List<String>
        get() {
            log.info("Query available locales")
            return LanguageList.getListedLocales(existingElement(disabledLocales))
        }

    fun expectEnabledLocaleListCount(count: Int): ProjectLanguagesTab {
        waitForAMoment().withMessage("enabled locales is " + count.toString())
                .until { enabledLocaleList.size == count }
        return ProjectLanguagesTab(driver)
    }

    fun expectAvailableLocaleListCount(count: Int): ProjectLanguagesTab {
        waitForAMoment().withMessage("available locales is " + count.toString())
                .until { availableLocaleList.size == count }
        return ProjectLanguagesTab(driver)
    }

    /**
     * Enter text into the disabled language filter field
     *
     * @param languageQuery
     * text to filter by
     * @return new language settings tab
     */
    fun filterDisabledLanguages(languageQuery: String): ProjectLanguagesTab {
        log.info("Filter disabled languages for: {}", languageQuery)
        enterText(readyElement(disabledLocalesFilter), languageQuery)
        return ProjectLanguagesTab(driver)
    }

    fun filterEnabledLanguages(languageQuery: String): ProjectLanguagesTab {
        log.info("Filter enabled languages for: {}", languageQuery)
        enterText(readyElement(enabledLocalesFilter), languageQuery)
        return ProjectLanguagesTab(driver)
    }

    /**
     * Add a language to the languages list.
     *
     * @param searchLocaleId
     * language to select
     * @return new language settings, anticipating the language has been added.
     */
    fun addLanguage(searchLocaleId: String): ProjectLanguagesTab {
        log.info("Click Enable on {}", searchLocaleId)
        val message = "can not find locale - $searchLocaleId"
        waitForAMoment().withMessage(message).until { driver ->
            LanguageList.toggleLanguageInList(
                    driver.findElement(disabledLocales),
                    searchLocaleId)
        }
        refreshPageUntil(this,
                "Wait for the locale list to contain $searchLocaleId") {
            enabledLocaleList.contains(searchLocaleId)
        }
        return ProjectLanguagesTab(driver)
    }

    /**
     * Click the removal link for a language.
     *
     * @param searchLocaleId
     * language to remove
     * @return new language settings tab
     */
    fun removeLocale(searchLocaleId: String): ProjectLanguagesTab {
        log.info("Click Disable on {}", searchLocaleId)
        val message = "can not find locale - $searchLocaleId"
        waitForAMoment().withMessage(message)
                .until { driver ->
                    LanguageList.toggleLanguageInList(
                            driver.findElement(activeLocales),
                            searchLocaleId)
                }
        refreshPageUntil(this,
                "Wait for the locale list to not contain $searchLocaleId") {
            !enabledLocaleList.contains(searchLocaleId)
        }
        return ProjectLanguagesTab(driver)
    }

    fun clickLanguageActionsDropdown(locale: String): ProjectLanguagesTab {
        LanguageList.clickActionsDropdown(readyElement(activeLocales), locale)
        return ProjectLanguagesTab(driver)
    }

    fun clickAddAlias(locale: String): ProjectLanguagesTab {
        LanguageList.clickAddAlias(readyElement(activeLocales), locale)
        return ProjectLanguagesTab(driver)
    }

    fun clickEditAlias(locale: String): ProjectLanguagesTab {
        LanguageList.clickEditAlias(readyElement(activeLocales), locale)
        return ProjectLanguagesTab(driver)
    }

    fun enterAliasForLocale(locale: String,
                            alias: String): ProjectLanguagesTab {
        LanguageList.enterAlias(readyElement(activeLocales), locale, alias)
        return ProjectLanguagesTab(driver)
    }

    fun saveLocaleAlias(locale: String): ProjectLanguagesTab {
        LanguageList.setAlias(readyElement(activeLocales), locale)
        return ProjectLanguagesTab(driver)
    }

    fun deleteAlias(locale: String): ProjectLanguagesTab {
        LanguageList.unsetAlias(readyElement(activeLocales), locale)
        return ProjectLanguagesTab(driver)
    }

    fun getAlias(locale: String): String {
        return LanguageList.getAliasForLocale(readyElement(activeLocales),
                locale)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectLanguagesTab::class.java)
    }
}
