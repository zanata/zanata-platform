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
package org.zanata.page.projectversion

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage

import org.assertj.core.api.Assertions.assertThat

class CreateVersionPage(driver: WebDriver) : BasePage(driver) {
    var projectVersionID = By.id("create-version-form:slug:input:slug")!!
    private val projectTypeSelection = By.id("create-version-form:project-type")
    private val saveButton = By.id("create-version-form:button-create")
    private val copyFromPreviousVersionChk = By.id("create-version-form:copy-from-version")
    private val projectTypesList = By.id("create-version-form:project-type-list")
    private val previousVersionsList = By.id("create-version-form:project-version")

    private val isCopyFromVersionAvailable: Boolean
        get() = driver.findElements(copyFromPreviousVersionChk).size > 0

    private val versionIdField: WebElement
        get() {
            log.info("Query Version ID")
            return readyElement(projectVersionID)
        }

    /**
     * Enter a version ID - only available on creating a new version
     *
     * @param versionId
     * @return new CreateVersionPage
     */
    fun inputVersionId(versionId: String): CreateVersionPage {
        log.info("Enter version ID {}", versionId)
        enterText(versionIdField, versionId)
        return CreateVersionPage(driver)
    }

    private fun clickCopyFromCheckbox() {
        (driver as JavascriptExecutor).executeScript(
                "arguments[0].click();",
                readyElement(copyFromPreviousVersionChk)
                        .findElement(By.tagName("span")))
    }

    @Suppress("unused")
    fun enableCopyFromVersion(): CreateVersionPage {
        log.info("Set Copy From Previous checkbox")
        if (!isCopyFromVersionAvailable) {
            log.warn("Copy Version not available!")
            return this
        }
        if (copyFromVersionIsChecked()) {
            log.warn("Checkbox already enabled!")
        } else {
            clickCopyFromCheckbox()
        }
        readyElement(previousVersionsList)
        return CreateVersionPage(driver)
    }

    fun disableCopyFromVersion(): CreateVersionPage {
        log.info("Unset Copy From Previous checkbox")
        if (!isCopyFromVersionAvailable) {
            log.warn("Copy Version not available!")
            return this
        }
        if (!copyFromVersionIsChecked()) {
            log.warn("Checkbox already disabled!")
        } else {
            clickCopyFromCheckbox()
        }
        readyElement(projectTypesList)
        return CreateVersionPage(driver)
    }

    private fun copyFromVersionIsChecked(): Boolean {
        log.info("Query is Copy from Version checkbox checked")
        return readyElement(copyFromPreviousVersionChk)
                .findElement(By.tagName("input")).isSelected
    }

    fun selectProjectType(projectType: String): CreateVersionPage {
        log.info("Click project type {}", projectType)
        clickElement(waitForAMoment()
                .withMessage("project type found")
                .until<WebElement> {
                    for (item in readyElement(projectTypeSelection)
                            .findElements(By.tagName("li"))) {
                        if (item.findElement(By.tagName("label")).text
                                        .startsWith(projectType)) {
                            return@until item
                        }
                    }
                    null
                } as WebElement)
        return CreateVersionPage(driver)
    }

    fun saveVersion(): VersionLanguagesPage {
        log.info("Click Save")
        clickAndCheckErrors(readyElement(saveButton))
        return VersionLanguagesPage(driver)
    }

    fun saveExpectingError(): CreateVersionPage {
        log.info("Click Save")
        clickElement(saveButton)
        return CreateVersionPage(driver)
    }

    fun expectNumErrors(numberOfErrors: Int): CreateVersionPage {
        log.info("Wait for number of error to be {}", numberOfErrors)
        waitForPageSilence()
        assertThat(errors).hasSize(numberOfErrors)
        return CreateVersionPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CreateVersionPage::class.java)
        const val VALIDATION_ERROR = "must start and end with letter or number, and contain only letters, numbers, periods, underscores and hyphens."
    }
}
