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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.languages.LanguagesPage

class AddLanguagePage(driver: WebDriver) : BasePage(driver) {

    private val saveButton = By.id("btn-new-language-save")
    private val localeId = By.className("react-autosuggest__input")
    private val suggestList = By.className("react-autosuggest__suggestions-list")
    private val suggestRow = By.className("react-autosuggest__suggestion")
    private val enabledByDefaultCheckbox = By.id("chk-new-language-enabled")
    private val pluralsWarning = By.id("new-language-pluralforms-warning")
    private val languageOption = By.name("new-language-displayName")
    private val newLanguageName = By.id("displayName")
    private val newLanguageNativeName = By.id("nativeName")
    private val pluralForms = By.id("pluralForms")

    /**
     * Retrieve the locale code for the new language
     * @return String language locale code
     */
    val newLanguageCode: String
        get() = getAttribute(localeId, "value")

    /**
     * Enter a string into the language search field
     * @param language string to enter
     * @return new AddLanguagePage
     */
    fun enterSearchLanguage(language: String): AddLanguagePage {
        log.info("Enter language {}", language)
        enterText(localeId, language)
        // Pause for a moment, as quick actions can break here
        slightPause()
        return AddLanguagePage(driver)
    }

    /**
     * Enter a string into the language search field
     * @param language string to enter
     * @return new AddLanguagePage
     */
    fun enterLanguageName(language: String): AddLanguagePage {
        log.info("Enter language name {}", language)
        enterText(newLanguageName, language)
        // Pause for a moment, as quick actions can break here
        slightPause()
        return AddLanguagePage(driver)
    }

    /**
     * Enter a string into the language search field
     * @param language string to enter
     * @return new AddLanguagePage
     */
    fun enterLanguageNativeName(language: String): AddLanguagePage {
        log.info("Enter language native name {}", language)
        enterText(newLanguageNativeName, language)
        // Pause for a moment, as quick actions can break here
        slightPause()
        return AddLanguagePage(driver)
    }

    /**
     * Enter a string into the language search field
     * @param language string to enter
     * @return new AddLanguagePage
     */
    fun enterLanguagePlurals(language: String): AddLanguagePage {
        log.info("Enter plurals {}", language)
        enterText(pluralForms, language)
        // Pause for a moment, as quick actions can break here
        slightPause()
        return AddLanguagePage(driver)
    }

    /**
     * Select a language from the search dropdown
     * @param language option to select
     * @return new AddLanguagePage
     */
    fun selectSearchLanguage(language: String): AddLanguagePage {
        log.info("Select language {}", language)
        waitForAMoment().withMessage("language to be clicked")
                .until {
                    val suggestions = existingElement(suggestList)
                            .findElements(suggestRow)
                    var clickedLanguage = false
                    for (row in suggestions) {
                        if (existingElement(row, languageOption).text
                                        .contains(language)) {
                            row.click()
                            clickedLanguage = true
                            break
                        }
                    }
                    clickedLanguage
                }
        return AddLanguagePage(driver)
    }

    /**
     * Wait for the plurals warning to show
     * @return new AddLanguagePage
     */
    fun expectPluralsWarning(): AddLanguagePage {
        log.info("Expect plurals warning")
        waitForPageSilence()
        readyElement(pluralsWarning)
        return AddLanguagePage(driver)
    }

    /**
     * Click Enable by default if not already enabled
     * @return new AddLanguagePage
     */
    @Suppress("unused")
    fun enableLanguageByDefault(): AddLanguagePage {
        log.info("Click Enable by default")
        if (!existingElement(enabledByDefaultCheckbox).isSelected) {
            driver.findElement(enabledByDefaultCheckbox).click()
        }
        return AddLanguagePage(driver)
    }

    /**
     * Click Disable by default if not already disabled
     * @return new AddLanguagePage
     */
    fun disableLanguageByDefault(): AddLanguagePage {
        log.info("Click Disable by default")
        if (existingElement(enabledByDefaultCheckbox).isSelected) {
            driver.findElement(enabledByDefaultCheckbox).click()
            //            clickElement(enabledByDefaultCheckbox);
        }
        return AddLanguagePage(driver)
    }

    /**
     * Retrieve the name for the new language
     * @return String language name
     */
    @Suppress("unused")
    fun getNewLanguageName(): String {
        return getAttribute(newLanguageName, "value")
    }

    /**
     * Retrieve the native name for the new language
     * @return String native language name
     */
    @Suppress("unused")
    fun getNewLanguageNativeName(): String {
        return getAttribute(newLanguageNativeName, "value")
    }

    /**
     * Press the Save button
     * @return new LanguagesPage
     */
    fun saveLanguage(): LanguagesPage {
        log.info("Click Save")
        clickAndCheckErrors(readyElement(saveButton))
        return LanguagesPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(AddLanguagePage::class.java)
    }
}
