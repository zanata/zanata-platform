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
package org.zanata.page.dashboard.dashboardsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.dashboard.DashboardBasePage

/**
 * @author Carlos Munoz [camunoz@redhat.com](mailto:camunoz@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class DashboardAccountTab(driver: WebDriver) : DashboardBasePage(driver) {
    private val emailForm = By.id("email-update-form")
    private val emailField = By.id("email-update-form:emailField:input:email")
    // Use form and button tag to find the item, as its id is altered by jsf
    private val updateEmailButton = By.tagName("button")
    private val oldPasswordField = By.id("passwordChangeForm:oldPasswordField:input:oldPassword")
    private val newPasswordField = By.id("passwordChangeForm:newPasswordField:input:newPassword")
    private val changePasswordButton = By.cssSelector(
            "button[id^=\'passwordChangeForm:changePasswordButton\']")
    private val exportUserData = By.id("exportUserData")

    val exportUserDataURL: String
        get() = readyElement(exportUserData).getAttribute("href")

    fun typeNewAccountEmailAddress(emailAddress: String): DashboardAccountTab {
        log.info("Enter email {}", emailAddress)
        readyElement(emailField).clear()
        enterText(readyElement(emailField), emailAddress)
        return DashboardAccountTab(driver)
    }

    fun clickUpdateEmailButton(): DashboardAccountTab {
        log.info("Click Update Email")
        clickElement(readyElement(emailForm).findElement(updateEmailButton))
        return DashboardAccountTab(driver)
    }

    fun enterOldPassword(oldPassword: String): DashboardAccountTab {
        log.info("Enter old password {}", oldPassword)
        readyElement(oldPasswordField).clear()
        enterText(readyElement(oldPasswordField), oldPassword)
        return DashboardAccountTab(driver)
    }

    fun enterNewPassword(newPassword: String): DashboardAccountTab {
        log.info("Enter new password {}", newPassword)
        readyElement(newPasswordField).clear()
        enterText(readyElement(newPasswordField), newPassword)
        return DashboardAccountTab(driver)
    }

    fun clickUpdatePasswordButton(): DashboardAccountTab {
        log.info("Click Update Password")
        clickElement(changePasswordButton)
        // For some reason, returning a new page immediately after click
        // causes the notifications to close
        slightPause()
        return DashboardAccountTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(DashboardAccountTab::class.java)
        const val INCORRECT_OLD_PASSWORD_ERROR = "Old password is incorrect, please check and try again."
        const val FIELD_EMPTY_ERROR = "may not be empty"
        const val PASSWORD_LENGTH_ERROR = "size must be between 6 and 1024"
        const val EMAIL_TAKEN_ERROR = "This email address is already taken"
    }
}
