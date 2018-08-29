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

import java.util.HashMap
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ManageUserAccountPage(driver: WebDriver) : BasePage(driver) {
    private val passwordField = By.id("userdetailForm:password:input:password")
    private val passwordConfirmField = By.id("userdetailForm:passwordConfirm:input:confirm")
    private val fullNameField = By.id("userdetailForm:name")
    private val enabledField = By.id("userdetailForm:enabled")
    private val saveButton = By.id("userdetailForm:userdetailSave")
    private val cancelButton = By.id("userdetailForm:userdetailCancel")
    private val roleMap: MutableMap<String, String>
    private val rolePrefix = "userdetailForm:roles:input:roles:"

    /**
     * Get the current display name for the user
     * @return display name string
     */
    val currentName: String
        get() {
            log.info("Query user's name")
            return getAttribute(fullNameField, "value")
        }

    init {
        roleMap = HashMap()
        roleMap["admin"] = "0"
        roleMap["glossarist"] = "1"
        roleMap["glossary-admin"] = "2"
        roleMap["translator"] = "3"
        roleMap["user"] = "4"
    }

    /**
     * Enter a display name for the user
     * @param fullName string to enter
     * @return new ManageUserAccountPage
     */
    fun enterFullName(fullName: String): ManageUserAccountPage {
        log.info("Enter name {}", fullName)
        enterText(fullNameField, fullName)
        return ManageUserAccountPage(driver)
    }

    /**
     * Enter a password for the user
     * @param password string to enter
     * @return new ManageUserAccountPage
     */
    fun enterPassword(password: String): ManageUserAccountPage {
        log.info("Enter password {}", password)
        enterText(passwordField, password)
        return ManageUserAccountPage(driver)
    }

    /**
     * Enter a confirmation password for the user
     * @param confirmPassword string to enter
     * @return new ManageUserAccountPage
     */
    fun enterConfirmPassword(confirmPassword: String): ManageUserAccountPage {
        log.info("Enter confirm password {}", confirmPassword)
        enterText(passwordConfirmField, confirmPassword)
        return ManageUserAccountPage(driver)
    }

    /**
     * Press the enabled checkbox
     * @return new ManageUserAccountPage
     */
    fun clickEnabled(): ManageUserAccountPage {
        log.info("Click Enabled")
        clickElement(enabledField)
        return ManageUserAccountPage(driver)
    }

    /**
     * Press a named role checkbox
     * @param role checkbox name to select
     * @return new ManageUserAccountPage
     */
    fun clickRole(role: String): ManageUserAccountPage {
        log.info("Click role {}", role)
        clickElement(By.id(rolePrefix + roleMap[role]))
        return ManageUserAccountPage(driver)
    }

    /**
     * Query if a named role is checked
     * @param role name to query
     * @return boolean is role checked
     */
    @Suppress("unused")
    fun isRoleChecked(role: String): Boolean {
        log.info("Query is role {} checked", role)
        return readyElement(By.id(rolePrefix + roleMap[role]))
                .isSelected
    }

    /**
     * Press the Save button
     * @return new ManageUserPage
     */
    fun saveUser(): ManageUserPage {
        log.info("Click Save")
        clickElement(saveButton)
        return ManageUserPage(driver)
    }

    /**
     * Press the Save button, expecting a failure condition
     * @return new ManageUserAccountPage
     */
    fun saveUserExpectFailure(): ManageUserAccountPage {
        log.info("Click Save, expecting failure")
        clickElement(saveButton)
        return ManageUserAccountPage(driver)
    }

    /**
     * Press the Cancel button
     * @return new ManageUserPage
     */
    @Suppress("unused")
    fun cancelEditUser(): ManageUserPage {
        log.info("Click Cancel")
        clickElement(cancelButton)
        return ManageUserPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ManageUserAccountPage::class.java)

        var PASSWORD_ERROR = "Passwords do not match"
    }
}
