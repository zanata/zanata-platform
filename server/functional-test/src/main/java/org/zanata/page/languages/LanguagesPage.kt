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
package org.zanata.page.languages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage
import org.zanata.page.administration.AddLanguagePage
import kotlin.streams.toList

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class LanguagesPage(driver: WebDriver) : BasePage(driver) {
    private val addLanguageButton = By.id("btn-language-add-new")
    private val defaultLabel = By.className("ant-tag")

    private val rows: List<WebElement>
        get() = readyElement(existingElement(By.id("languages-form")),
                By.id("languages-table"))
                .findElements(By.name("language-entry"))

    val languageLocales: List<String>
        get() {
            log.info("Query list of languages")
            return rows.stream().map<String> { it -> this.getShortLocale(it) }
                    .toList()
        }
    // return list of tr

    private fun findRowByLocale(localeId: String): WebElement {
        for (row in rows) {
            if (getShortLocale(row) == localeId) {
                return row
            }
        }
        throw RuntimeException("Did not find locale $localeId")
    }

    private fun getShortLocale(row: WebElement): String {
        val span = row.findElement(By.name("language-name"))
        return span.text.substring(0, span.text.indexOf('[')).trim { it <= ' ' }
    }

    fun clickAddNewLanguage(): AddLanguagePage {
        log.info("Click add new language button")
        clickElement(addLanguageButton)
        return AddLanguagePage(driver)
    }

    fun gotoLanguagePage(localeId: String): LanguagePage {
        log.info("Click language {}", localeId)
        val element = findRowByLocale(localeId)
                .findElement(By.id("language-name-$localeId"))
        if (element != null) {
            element.click()
            return LanguagePage(driver)
        }
        throw RuntimeException("Did not find locale $localeId")
    }

    fun languageIsEnabledByDefault(localeId: String): Boolean {
        log.info("Query is language enabled by default {}", localeId)
        // Search for enabledByDefaultLabel label
        val tags = findRowByLocale(localeId)
                .findElements(defaultLabel)
        for (tag in tags) {
            if (tag.text.contains("DEFAULT")) {
                return true
            }
        }
        return false
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(LanguagesPage::class.java)
    }
}

