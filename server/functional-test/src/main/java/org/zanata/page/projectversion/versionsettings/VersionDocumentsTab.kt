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

import java.util.ArrayList

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.projectversion.VersionBasePage

//import org.zanata.util.FluentWaitExtKt.until

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class VersionDocumentsTab(driver: WebDriver) : VersionBasePage(driver) {
    private val uploadButton = By.id("file-upload-component-toggle-button")
    private val startUploadButton = By.id("file-upload-component-start-upload")
    private val cancelUploadButton = By.id("file-upload-component-cancel-upload")
    private val fileUploadInput = By.id("file-upload-component-file-input")
    private val fileUploadDone = By.id("file-upload-component-done-upload")
    private val fileUploadPanel = By.id("file-upload-component")

    val sourceDocumentsList: List<String>
        get() {
            val elements = existingElement(By.id("settings-document_form"))
                    .findElement(By.tagName("ul"))
                    .findElements(By.tagName("label"))
            val namesList = ArrayList<String>()
            for (element in elements) {
                namesList.add(element.text)
            }
            println(namesList)
            return namesList
        }

    val uploadList: List<String>
        get() {
            log.info("Query upload list")
            val filenames = ArrayList<String>()
            for (element in uploadListElements) {
                filenames.add(
                        element.findElement(By.className("list__title")).text)
            }
            return filenames
        }

    private val uploadListElements: List<WebElement>
        get() = executor
                .executeScript("return $(\'div.js-files-panel ul li\')") as List<WebElement>

    val uploadError: String
        get() {
            log.info("Query upload error message")
            return readyElement(existingElement(fileUploadPanel),
                    By.className("message--danger")).text
        }

    fun pressUploadFileButton(): VersionDocumentsTab {
        log.info("Click Upload file button")
        clickLinkAfterAnimation(readyElement(uploadButton))
        return VersionDocumentsTab(driver)
    }

    /**
     * Query for the status of the upload button in the submit dialog
     *
     * @return boolean can submit file upload
     */
    fun canSubmitDocument(): Boolean {
        log.info("Query can start upload")
        return existingElement(startUploadButton).isEnabled
    }

    fun cancelUpload(): VersionDocumentsTab {
        log.info("Click Cancel")
        clickElement(cancelUploadButton)
        waitForAMoment().withMessage("upload dialog is hidden")
                .until { driver ->
                    !driver.findElement(By.id("file-upload-component"))
                            .isDisplayed
                }
        slightPause()
        waitForPageSilence()
        return VersionDocumentsTab(driver)
    }

    fun enterFilePath(filePath: String): VersionDocumentsTab {
        log.info("Enter file path {}", filePath)
        // Make the hidden input element slightly not hidden
        executor.executeScript(
                "arguments[0].style.visibility = \'visible\'; arguments[0].style.height = \'1px\'; arguments[0].style.width = \'1px\'; arguments[0].style.opacity = 1",
                existingElement(fileUploadInput))
        // Don't clear, inject text, don't check
        enterText(readyElement(fileUploadInput), filePath, false, true, false)
        return VersionDocumentsTab(driver)
    }

    fun submitUpload(): VersionDocumentsTab {
        log.info("Click Submit upload")
        clickElement(startUploadButton)
        return VersionDocumentsTab(driver)
    }

    fun clickUploadDone(): VersionDocumentsTab {
        log.info("Click upload Done button")
        clickElement(fileUploadDone)
        return VersionDocumentsTab(driver)
    }

    fun sourceDocumentsContains(document: String): Boolean {
        log.info("Query source documents contain {}", document)
        for (documentLabel in waitForAMoment()
                .withMessage("get source document list")
                .until { sourceDocumentsList }) {
            if (documentLabel.contains(document)) {
                return true
            }
        }
        return false
    }

    fun clickRemoveOn(filename: String): VersionDocumentsTab {
        log.info("Click remove on {}", filename)
        for (element in uploadListElements) {
            if (element.findElement(By.className("list__title")).text == filename) {
                element.findElement(By.className("list__item__actions"))
                        .findElement(By.className("cancel")).click()
            }
        }
        return VersionDocumentsTab(driver)
    }

    fun expectSomeUploadItems() {
        waitForAMoment().until { !uploadListElements.isEmpty() }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionDocumentsTab::class.java)
        const val UNSUPPORTED_FILETYPE = " is not a supported file type."
    }
}
