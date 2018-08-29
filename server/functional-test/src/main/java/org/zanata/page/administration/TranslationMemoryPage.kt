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
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage
import org.zanata.page.CorePage

import java.util.ArrayList
import org.apache.commons.lang3.StringUtils.isBlank

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TranslationMemoryPage(driver: WebDriver) : BasePage(driver) {

    private val listItemCount = By.className("badge")
    private val listItemDescription = By.className("list__item__meta")
    private val dropDownMenu = By.id("moreActions")
    private val createTmLink = By.id("createTmLink")
    private val tmList = By.id("tmList")
    private val filenameInput = By.name("uploadedFile")
    private val uploadButton = By.id("tm-import-button")
    private val listDropDownMenu = By.className("dropdown__toggle")
    private val listImportButton = By.linkText("Import")
    private val listClearButton = By.linkText("Clear")
    private val listDeleteButton = By.linkText("Delete")
    private val deleteConfirmation = By.id("deleteTMModal")
    private val clearConfirmation = By.id("clearTMModal")
    private val uploadNotification = By.id("uploadTMNotification")
    private val okConfirmation = By.id("confirm-ok-button")
    private val cancelConfirmation = By.id("confirm-cancel-button")

    /**
     * Determine if the Upload button is enabled
     * @return boolean button is enabled
     */
    val isImportButtonEnabled: Boolean
        get() = existingElement(uploadButton).isEnabled

    /**
     * Retrieve a list of the TM entries
     * @return String list of TM names
     */
    val listedTranslationMemorys: List<String>
        get() {
            log.info("Query translation memory names")
            val names = ArrayList<String>()
            for (listElement in getTMList()) {
                names.add(getListEntryName(listElement))
            }
            return names
        }

    /**
     * Press the Create New button in the dropdown menu
     * @return new TranslationMemoryEditPage
     */
    fun clickCreateNew(): TranslationMemoryEditPage {
        log.info("Click Create New")
        clickElement(dropDownMenu)
        clickLinkAfterAnimation(createTmLink)
        return TranslationMemoryEditPage(driver)
    }

    /**
     * Press the dropdown menu for a specific TM entry
     * @param tmName of entry to press the menu for
     * @return
     */
    fun clickOptions(tmName: String): TranslationMemoryPage {
        log.info("Click Options dropdown for {}", tmName)
        clickElement(readyElement(findRowByTMName(tmName), listDropDownMenu))
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Import menu option for a specific TM entry
     * The dropdown option menu should be opened before this action
     * @param tmName of entry to press Import for
     * @return new TranslationMemoryPage
     */
    fun clickImport(tmName: String): TranslationMemoryPage {
        log.info("Click Import")
        clickElement(readyElement(findRowByTMName(tmName), listImportButton))
        return TranslationMemoryPage(driver)
    }

    /**
     * Enter a filename of a TM to import directly into the import dialog
     * @param importFileName of file to import
     * @return new TranslationMemoryPage
     */
    fun enterImportFileName(importFileName: String): TranslationMemoryPage {
        log.info("Enter import TM filename {}", importFileName)
        // Don't clear, inject text, do not check value
        enterText(readyElement(filenameInput), importFileName, false, true,
                false)
        slightPause()
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Upload button on the import dialog
     * @return new TranslationMemoryPage
     */
    fun clickUploadButtonAndAcknowledge(): TranslationMemoryPage {
        log.info("Click and accept Upload button")
        clickElement(uploadButton)
        val notificationDialog = readyElement(uploadNotification)
        slightPause()
        clickElement(readyElement(notificationDialog, okConfirmation))
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Clear button, then press the Accept button, for a specific TM
     * @param tmName of TM to clear
     * @return new TranslationMemoryPage
     */
    fun clickClearTMAndAccept(tmName: String): TranslationMemoryPage {
        log.info("Click and accept Clear {}", tmName)
        clickElement(readyElement(findRowByTMName(tmName), listClearButton))
        clickElement(readyElement(existingElement(clearConfirmation), okConfirmation))
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Clear button, then press the Cancel button, for a specific TM
     * @param tmName of TM to press clear and cancel for
     * @return new TranslationMemoryPage
     */
    fun clickClearTMAndCancel(tmName: String): TranslationMemoryPage {
        log.info("Click and Cancel Clear {}", tmName)
        clickElement(readyElement(findRowByTMName(tmName), listClearButton))
        clickElement(readyElement(existingElement(clearConfirmation), cancelConfirmation))
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Delete button, then press the Accept button, for a specific TM
     * @param tmName of TM to delete
     * @return new TranslationMemoryPage
     */
    fun clickDeleteTmAndAccept(tmName: String): TranslationMemoryPage {
        log.info("Click and accept Delete {}", tmName)
        clickElement(readyElement(findRowByTMName(tmName), listDeleteButton))
        slightPause()
        clickElement(readyElement(existingElement(deleteConfirmation), okConfirmation))
        return TranslationMemoryPage(driver)
    }

    /**
     * Press the Delete button, then press the Cancel button, for a specific TM
     * @param tmName of TM to press delete and cancel for
     * @return new TranslationMemoryPage
     */
    fun clickDeleteTmAndCancel(tmName: String): TranslationMemoryPage {
        log.info("Click and cancel Delete {}", tmName)
        clickElement(readyElement(findRowByTMName(tmName), listDeleteButton))
        clickElement(readyElement(existingElement(deleteConfirmation), cancelConfirmation))
        return TranslationMemoryPage(driver)
    }

    /**
     * Dismiss the Import Error dialog
     * @return new TranslationMemoryPage
     */
    @Suppress("unused")
    fun dismissError(): TranslationMemoryPage {
        log.info("Dismiss error dialog")
        clickElement(readyElement(existingElement(uploadNotification), okConfirmation))
        return TranslationMemoryPage(driver)
    }

    /**
     * Query a specific TM description
     * @param tmName name of TM to query
     * @return description String
     */
    fun getDescription(tmName: String): String {
        log.info("Query description {}", tmName)
        return getText(existingElement(findRowByTMName(tmName), listItemDescription))
    }

    /**
     * Query a specific TM's number of entries
     * @param tmName name of TM to query
     * @return number of TM entries as String
     */
    fun getNumberOfEntries(tmName: String): String {
        log.info("Query number of entries {}", tmName)
        waitForPageSilence()
        return getListEntryCount(findRowByTMName(tmName))
    }

    // TODO Remove this when stable
    fun expectNumberOfEntries(number: Int, tmName: String) {
        waitForAMoment().withMessage("Workaround: wait for number of entries")
                .until { Integer.valueOf(getNumberOfEntries(tmName)) == number }
    }

    /**
     * Query a TM for the delete button being enabled
     * @param tmName name of TM to query
     * @return boolean delete button is available
     */
    fun canDelete(tmName: String): Boolean {
        log.info("Query can delete {}", tmName)
        val disabled = getAttribute(existingElement(findRowByTMName(tmName),
                listDeleteButton), "disabled")
        return isBlank(disabled) || disabled == "false"
    }

    /*
     * Check to see if the TM list is empty
     */
    private fun noTmsCreated(): Boolean {
        for (element in readyElement(tmList)
                .findElements(CorePage.paragraph)) {
            if (getText(element) == NO_MEMORIES) {
                return true
            }
        }
        return false
    }

    /*
     * Get a row from the TM table that corresponds with tmName
     */
    private fun findRowByTMName(tmName: String): WebElement {
        for (listElement in getTMList()) {
            if (getListEntryName(listElement) == tmName) {
                return listElement
            }
        }
        throw RuntimeException("Unable to find TM row $tmName")
    }

    // Get a web element list of all TM entries
    private fun getTMList(): List<WebElement> {
        if (noTmsCreated()) {
            log.info("TM list is empty")
            return ArrayList()
        }
        return readyElement(readyElement(tmList), By.className("list--stats"))
                .findElements(By.className("list__item--actionable"))
    }

    // Get the name substring of a TM entry
    private fun getListEntryName(listElement: WebElement): String {
        val title = getText(existingElement(listElement,
                CorePage.h3Header)).trim { it <= ' ' }
        return title
                .substring(0, title.lastIndexOf(getListEntryCount(listElement)))
                .trim { it <= ' ' }
    }

    // Get the entry count substring for a TM entry
    private fun getListEntryCount(listElement: WebElement): String {
        return getText(existingElement(
                existingElement(listElement,
                        CorePage.h3Header), listItemCount))
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(TranslationMemoryPage::class.java)

        const val ID_UNAVAILABLE = "This Id is not available"
        const val UPLOAD_ERROR = "There was an error uploading the file"
        const val NO_MEMORIES = "No Translation Memories have been created."
    }
}
