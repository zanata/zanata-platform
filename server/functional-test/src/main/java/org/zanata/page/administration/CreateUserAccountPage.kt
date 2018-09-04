/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.HashMap

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class CreateUserAccountPage(driver: WebDriver) : BasePage(driver) {

    private val usernameField = By.id("newUserForm:username:input:username")
    private val emailField = By.id("newUserForm:email:input:email")
    private val saveButton = By.id("newUserForm:newUserSave")
    private val cancelButton = By.linkText("Cancel")
    private val roleIdPrefix = "newUserForm:roles:input:newUserRoles:"

    private val roleMap: MutableMap<String, String>

    init {
        roleMap = HashMap()
        roleMap["admin"] = "0"
        roleMap["glossarist"] = "1"
        roleMap["glossary-admin"] = "2"
        roleMap["translator"] = "3"
        roleMap["user"] = "4"
    }

    /**
     * Enter text into the username field
     * @param username string to enter
     * @return new CreateUserAccountPage
     */
    fun enterUsername(username: String): CreateUserAccountPage {
        log.info("Enter username {}", username)
        enterText(usernameField, username)
        return CreateUserAccountPage(driver)
    }

    /**
     * Enter text into the email address field
     * @param email string to enter
     * @return new CreateUserAccountPage
     */
    fun enterEmail(email: String): CreateUserAccountPage {
        enterText(emailField, email)
        return CreateUserAccountPage(driver)
    }

    /**
     * Select a role for the user
     * @param role entry to select
     * @return new CreateUserAccountPage
     */
    fun clickRole(role: String): CreateUserAccountPage {
        log.info("Click role {}", role)
        clickElement(By.id(roleIdPrefix + roleMap[role]))
        return CreateUserAccountPage(driver)
    }

    /**
     * Query if a role is checked
     * @param role entry to query
     * @return new CreateUserAccountPage
     */
    @Suppress("unused")
    fun isRoleChecked(role: String): Boolean {
        log.info("Query is role {} checked", role)
        return readyElement(By.id(roleIdPrefix + roleMap[role]))
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
     * @return new CreateUserAccountPage
     */
    @Suppress("unused")
    fun saveUserExpectFailure(): CreateUserAccountPage {
        log.info("Click Save, expecting failure")
        clickElement(saveButton)
        return CreateUserAccountPage(driver)
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
        private val log = org.slf4j.LoggerFactory.getLogger(CreateUserAccountPage::class.java)
    }
}
