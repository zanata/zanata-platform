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
package org.zanata.page.projectversion.versionsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.projectversion.VersionBasePage
import org.zanata.util.LanguageList

import org.assertj.core.api.Assertions.assertThat

/**
 * This class represents the project version settings tab for languages.
 *
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class VersionLanguagesTab(driver: WebDriver) : VersionBasePage(driver) {
    private val languagesSettingForm = By.id("settings-languages-form")
    private val activeLocales = By.id("activeLocales-list")
    private val disabledLocales = By.id("availableLocales-list")
    private val activeLocalesFilter = By.id("settings-languages-form:activeLocales-filter-input")
    private val disabledLocalesFilter = By.id("settings-languages-form:availableLocales-filter-input")

    /**
     * Get a list of locales enabled in this version
     *
     * @return String list of language/locale names
     */
    val enabledLocaleList: List<String>
        get() {
            log.info("Query enabled locales list")
            return LanguageList.getListedLocales(existingElement(activeLocales))
        }

    /**
     * Get a list of locales available for this version
     *
     * @return String list of language/locale names
     */
    private val availableLocaleList: List<String>
        get() {
            log.info("Query available locales")
            return LanguageList.getListedLocales(existingElement(disabledLocales))
        }

    /**
     * Click the inherit project settings languages checkbox
     *
     * @return new language settings tab
     */
    @Suppress("unused")
    fun clickInheritCheckbox(): VersionLanguagesTab {
        log.info("Click Inherit check box")
        readyElement(readyElement(languagesSettingForm),
                By.className("form__checkbox")).click()
        return VersionLanguagesTab(driver)
    }

    fun expectLocaleListVisible(): VersionLanguagesTab {
        log.info("Wait for locale list visible")
        waitForPageSilence()
        val el = readyElement(languagesSettingForm)
                .findElement(By.className("list--slat"))
        assertThat(el.isDisplayed).describedAs("displayed").isTrue()
        return VersionLanguagesTab(driver)
    }

    fun expectEnabledLocaleListCount(count: Int): VersionLanguagesTab {
        waitForAMoment().withMessage(count.toString() + " items in locale list")
                .until { enabledLocaleList.size == count }
        return VersionLanguagesTab(driver)
    }

    fun expectAvailableLocaleListCount(count: Int): VersionLanguagesTab {
        waitForAMoment().withMessage(count.toString() + " items in locale list")
                .until { availableLocaleList.size == count }
        return VersionLanguagesTab(driver)
    }

    fun expectLanguagesContains(language: String): VersionLanguagesTab {
        log.info("Wait for languages contains {}", language)
        waitForPageSilence()
        assertThat(enabledLocaleList).`as`("enabled locales list")
                .contains(language)
        return VersionLanguagesTab(driver)
    }

    @Suppress("unused")
    fun waitForLanguagesNotContains(language: String): VersionLanguagesTab {
        log.info("Wait for languages does not contain {}", language)
        waitForLanguageEntryExpected(language, false)
        return VersionLanguagesTab(driver)
    }

    private fun waitForLanguageEntryExpected(language: String,
                                             exists: Boolean) {
        waitForAMoment()
                .withMessage(language + (" exists is " + exists.toString()))
                .until { enabledLocaleList.contains(language) == exists }
    }

    fun filterDisabledLanguages(localeQuery: String): VersionLanguagesTab {
        log.info("Filter disabled languages for: {}", localeQuery)
        readyElement(disabledLocalesFilter).clear()
        enterText(readyElement(disabledLocalesFilter), localeQuery)
        return VersionLanguagesTab(driver)
    }

    fun filterEnabledLanguages(localeQuery: String): VersionLanguagesTab {
        log.info("Filter enabled languages for: {}", localeQuery)
        readyElement(activeLocalesFilter).clear()
        enterText(readyElement(activeLocalesFilter), localeQuery)
        return VersionLanguagesTab(driver)
    }

    @Suppress("unused")
    fun removeLocale(localeId: String): VersionLanguagesTab {
        log.info("Click Disable on {}", localeId)
        val message = "can not find locale - $localeId"
        waitForAMoment().withMessage(message)
                .until { driver ->
                    LanguageList
                            .toggleLanguageInList(
                                    driver.findElement(activeLocales),
                                    localeId)
                }
        refreshPageUntil(this,
                "Wait for the locale list to not contain $localeId"
        ) { !enabledLocaleList.contains(localeId) }
        return VersionLanguagesTab(driver)
    }

    fun addLocale(localeId: String): VersionLanguagesTab {
        log.info("Click Enable on {}", localeId)
        val message = "can not find locale - $localeId"
        waitForAMoment().withMessage(message)
                .until { driver ->
                    LanguageList
                            .toggleLanguageInList(
                                    driver.findElement(disabledLocales),
                                    localeId)
                }
        refreshPageUntil(this,
                "Wait for the locale list to contain $localeId"
        ) { enabledLocaleList.contains(localeId) }
        return VersionLanguagesTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionLanguagesTab::class.java)
    }
}
