/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

/**
 * @author Damian Jansen  [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TranslationMemoryEditPage(driver: WebDriver) : BasePage(driver) {

    private val idField = By.id("tmForm:slug:input:slug")
    private val descriptionField = By.id("tmForm:description:input:description")
    private val saveButton = By.id("tmForm:save")
    private val cancelButton = By.id("tmForm:cancel")

    /**
     * Enter an ID for the translation memory
     * @param id to enter
     * @return new TranslationMemoryEditPage
     */
    fun enterMemoryID(id: String): TranslationMemoryEditPage {
        log.info("Enter TM ID {}", id)
        enterText(idField, id)
        return TranslationMemoryEditPage(driver)
    }

    /**
     * Enter a description for the translation memory
     * @param description to enter
     * @return new TranslationMemoryEditPage
     */
    fun enterTMDescription(description: String): TranslationMemoryEditPage {
        log.info("Enter TM description {}", description)
        enterText(descriptionField, description)
        return TranslationMemoryEditPage(driver)
    }

    /**
     * Press the Save button
     * @return new TranslationMemoryPage
     */
    fun saveTM(): TranslationMemoryPage {
        log.info("Click Save")
        clickElement(saveButton)
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Save button, expecting a failure condition
     * @return new TranslationMemoryEditPage
     */
    fun clickSaveAndExpectFailure(): TranslationMemoryEditPage {
        log.info("Click Save")
        clickElement(saveButton)
        return TranslationMemoryEditPage(driver)
    }

    /**
     * Press the Cancel button
     * @return new TranslationMemoryPage
     */
    @Suppress("unused")
    fun cancelTM(): TranslationMemoryPage {
        log.info("Click Cancel")
        clickElement(cancelButton)
        return TranslationMemoryPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(TranslationMemoryEditPage::class.java)
    }
}
