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
package org.zanata.page.groups

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class CreateVersionGroupPage(driver: WebDriver) : BasePage(driver) {
    private val groupIdField = By.id("group-form:slug:input:slug")
    var groupNameField = By.id("group-form:name:input:name")!!
    private val groupDescriptionField = By.id("group-form:description:input:description")
    private val saveButton = By.id("group-form:group-create-new")

    private val groupIdValue: String
        get() {
            log.info("Query Group ID")
            return readyElement(groupIdField).getAttribute("value")
        }

    fun inputGroupId(groupId: String): CreateVersionGroupPage {
        log.info("Enter Group ID {}", groupId)
        enterText(readyElement(groupIdField), groupId)
        return CreateVersionGroupPage(driver)
    }

    fun inputGroupName(groupName: String): CreateVersionGroupPage {
        log.info("Enter Group name {}", groupName)
        enterText(readyElement(groupNameField), groupName)
        return CreateVersionGroupPage(driver)
    }

    fun inputGroupDescription(desc: String): CreateVersionGroupPage {
        log.info("Enter Group description {}", desc)
        enterText(readyElement(groupDescriptionField), desc)
        return CreateVersionGroupPage(driver)
    }

    fun saveGroup(): VersionGroupPage {
        log.info("Click Save")
        clickAndCheckErrors(readyElement(saveButton))
        return VersionGroupPage(driver)
    }

    fun saveGroupFailure(): CreateVersionGroupPage {
        log.info("Click Save")
        clickElement(saveButton)
        return CreateVersionGroupPage(driver)
    }

    fun clearFields(): CreateVersionGroupPage {
        readyElement(groupIdField).clear()
        readyElement(groupNameField).clear()
        readyElement(groupDescriptionField).clear()
        waitForAMoment().withMessage("Wait for fields to be clear").until {
            groupIdValue == "" &&
            readyElement(groupNameField).getAttribute("value") == ""
        }
        return CreateVersionGroupPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CreateVersionGroupPage::class.java)
        const val LENGTH_ERROR = "value must be shorter than or equal to 100 characters"
        const val VALIDATION_ERROR = "must start and end with letter or number, and contain only letters, numbers, periods, underscores and hyphens."
    }
}
