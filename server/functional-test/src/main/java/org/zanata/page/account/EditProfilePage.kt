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
package org.zanata.page.account

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.utility.HomePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class EditProfilePage(driver: WebDriver) : BasePage(driver) {
    private val nameField = By.id("profile-form:nameField:name")
    private val usernameField = By.id("profile-form:usernameField:username")
    private val emailField = By.id("profile-form:emailField:email")
    private val saveButton = By.id("profile-form:user-create-new")

    /**
     * Enter a string into the name field
     * @param name text to enter
     * @return new EditProfilePage
     */
    fun enterName(name: String): EditProfilePage {
        log.info("Enter name {}", name)
        readyElement(nameField).clear()
        enterText(readyElement(nameField), name)
        defocus(nameField)
        return EditProfilePage(driver)
    }

    /**
     * Enter a string into the username field
     * @param userName text to enter
     * @return new EditProfilePage
     */
    fun enterUserName(userName: String): EditProfilePage {
        log.info("Enter username {}", userName)
        readyElement(usernameField).clear()
        enterText(readyElement(usernameField), userName)
        return EditProfilePage(driver)
    }

    /**
     * Enter a string into the email field
     * @param email text to enter
     * @return new EditProfilePage
     */
    fun enterEmail(email: String): EditProfilePage {
        log.info("Enter email {}", email)
        readyElement(emailField).clear()
        enterText(readyElement(emailField), email)
        defocus(emailField)
        return EditProfilePage(driver)
    }

    /**
     * Press the Save button
     * @return new HomePage
     */
    fun clickSave(): HomePage {
        log.info("Click Save")
        clickElement(saveButton)
        return HomePage(driver)
    }

    /**
     * Press the Save button, expecting errors
     * @return new EditProfilePage
     */
    fun clickSaveAndExpectErrors(): EditProfilePage {
        log.info("Click Save")
        clickElement(saveButton)
        return EditProfilePage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(EditProfilePage::class.java)
    }
}
