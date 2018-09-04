/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import kotlin.streams.toList

/**
 * Handles the behaviour of the project language lists
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
object LanguageList {

    private val localeId = By.className("js-locale-id")
    private val localeAlias = By.className("js-locale-alias")
    private val languageActions = By.className("button--group")
    private val dropdown = By.className("i--arrow-down")
    private val aliasInput = By.className("form--inline__input")
    private val setAlias = By.className("form--inline__addon")
    private val addAliasLink = By.linkText("Add alias")
    private val deleteAliasLink = By.className("i--trash")
    private val editAliasLink = By.linkText("Edit alias")

    fun getListedLocales(localeList: WebElement): List<String> {
        return getListElements(localeList)
                .stream()
                .filter { it -> it.isDisplayed }
                .map { li -> li.findElement(localeId).text }.toList()
    }

    private fun getListElements(localeList: WebElement): List<WebElement> {
        return localeList.findElements(By.className("reveal"))
    }

    private fun getLocaleID(localeListElement: WebElement): String {
        return localeListElement.findElement(localeId).text
    }

    private fun toggleEnabled(localeListElement: WebElement) {
        localeListElement.findElement(languageActions)
                .findElements(By.tagName("button"))[0].click()
    }

    fun toggleLanguageInList(localeList: WebElement,
                             locale: String): Boolean {
        val localeItem = getLocaleEntry(localeList, locale)
        try {
            LanguageList.toggleEnabled(localeItem)
        } catch (nsee: NoSuchElementException) {
            return false
        }

        return true
    }

    fun clickActionsDropdown(list: WebElement, locale: String) {
        getLocaleEntry(list, locale).findElement(dropdown).click()
    }

    fun clickAddAlias(list: WebElement, locale: String) {
        getLocaleEntry(list, locale).findElement(addAliasLink).click()
    }

    fun clickEditAlias(list: WebElement, locale: String) {
        getLocaleEntry(list, locale).findElement(editAliasLink).click()
    }

    fun enterAlias(localeList: WebElement, locale: String,
                   alias: String) {
        val field = getLocaleEntry(localeList, locale)
                .findElement(aliasInput)
        field.clear()
        field.sendKeys(alias)
    }

    fun setAlias(localeList: WebElement, locale: String) {
        getLocaleEntry(localeList, locale).findElement(setAlias).click()
    }

    fun unsetAlias(localeList: WebElement, locale: String) {
        getLocaleEntry(localeList, locale).findElement(deleteAliasLink).click()
    }

    fun getAliasForLocale(list: WebElement, locale: String): String {
        return if (!hasAlias(list, locale)) {
            ""
        } else getLocaleEntry(list, locale).findElement(localeAlias)
                .text

    }

    private fun hasAlias(localeList: WebElement, locale: String): Boolean {
        return getLocaleEntry(localeList, locale).findElements(localeAlias)
                .size > 0
    }

    private fun getLocaleEntry(list: WebElement, locale: String): WebElement {
        val listElements = LanguageList
                .getListElements(list)
        for (localeLi in listElements) {
            if (LanguageList.getLocaleID(localeLi) == locale) {
                return localeLi
            }
        }
        throw NoSuchElementException("Unable to find locale $locale")

    }

}

