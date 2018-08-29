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
import org.zanata.page.CorePage

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class RegisterPage(driver: WebDriver) : CorePage(driver) {

    private val nameField = By.id("loginForm:name:input:name")
    private val emailField = By.id("loginForm:email:input:email")
    var usernameField = By.id("loginForm:username:input:username")!!
    private val passwordField = By.id("loginForm:passwordField:input:password")
    private val signUpButton = By.xpath("//input[@value=\'Sign up\']")
    private val showHideToggleButton = By.className("js-form-password-toggle")
    private val loginLink = By.linkText("Log In")
    private val titleLabel = By.className("heading--sub")
    private val termsOfUseUrl = By.id("termsOfUseUrl")

    /**
     * Retrieve the registration page title
     * @return page title string
     */
    val pageTitle: String
        get() {
            log.info("Query page title")
            return getText(titleLabel)
        }

    /**
     * Retrieve the current text of the password field
     * @return password string
     */
    val password: String
        get() {
            log.info("Query password")
            return getAttribute(passwordField, "value")
        }

    /**
     * Retrieve the current view type of the password field
     * @return view type string e.g. password, text
     */
    val passwordFieldType: String
        get() {
            log.info("Query password field type")
            return getAttribute(passwordField, "type")
        }

    /**
     * Retrieve the URL of the Terms of Use link
     * @return url as string
     */
    val termsUrl: String
        get() {
            log.info("Query terms of use URL")
            return readyElement(termsOfUseUrl).findElement(By.tagName("a"))
                    .getAttribute("href")
        }

    /**
     * Enter a display name in the name field
     * @param name string to enter
     * @return new RegisterPage
     */
    fun enterName(name: String): RegisterPage {
        log.info("Enter name {}", name)
        enterText(readyElement(nameField), name)
        return RegisterPage(driver)
    }

    /**
     * Enter a string in the username field
     * @param userName string to enter
     * @return new RegisterPage
     */
    fun enterUserName(userName: String): RegisterPage {
        log.info("Enter username {}", userName)
        enterText(readyElement(usernameField), userName)
        return RegisterPage(driver)
    }

    /**
     * Enter a string in the email field
     * @param email string to enter
     * @return new RegisterPage
     */
    fun enterEmail(email: String): RegisterPage {
        log.info("Enter email {}", email)
        enterText(readyElement(emailField), email)
        return RegisterPage(driver)
    }

    /**
     * Enter a string in the password field
     * @param password string to enter
     * @return new RegisterPage
     */
    fun enterPassword(password: String): RegisterPage {
        log.info("Enter password {}", password)
        enterText(readyElement(passwordField), password)
        return RegisterPage(driver)
    }

    /**
     * Press the Sign Up button
     * @return new SignInPage
     */
    fun register(): SignInPage {
        log.info("Click Sign Up")
        clickElement(signUpButton)
        return SignInPage(driver)
    }

    /**
     * Press the Sign Up button, expecting failure
     * @return new SignInPage
     */
    fun registerFailure(): RegisterPage {
        log.info("Click Sign Up")
        clickElement(signUpButton)
        return RegisterPage(driver)
    }

    /**
     * Clear all registration page fields
     * @return new RegisterPage
     */
    fun clearFields(): RegisterPage {
        log.info("Clear fields")
        readyElement(nameField).clear()
        readyElement(emailField).clear()
        readyElement(usernameField).clear()
        readyElement(passwordField).clear()
        return RegisterPage(driver)
    }

    /*
     * Pass in a map of strings, to be entered into the registration fields.
     * Fields: name, email, username, password, confirmpassword
     */
    fun setFields(fields: Map<String, String>): RegisterPage {
        return clearFields().enterName(fields["name"].orEmpty())
                .enterEmail(fields["email"].orEmpty())
                .enterUserName(fields["username"].orEmpty())
                .enterPassword(fields["password"].orEmpty())
    }

    /**
     * Press the Log In button
     * @return new SignInPage
     */
    fun goToSignIn(): SignInPage {
        log.info("Click Log In")
        clickElement(loginLink)
        return SignInPage(driver)
    }

    /**
     * Toggle the show / hide password text
     * @return new RegisterPage
     */
    fun clickPasswordShowToggle(): RegisterPage {
        log.info("Click Show/Hide")
        clickElement(showHideToggleButton)
        return RegisterPage(driver)
    }

    /**
     * Determine if the the terms of use url is displayed
     * @return boolean terms of use is visible
     */
    fun termsOfUseUrlVisible(): Boolean {
        log.info("Query terms of use URL is visible")
        val elements = driver.findElements(termsOfUseUrl)
        return elements.size > 0 && elements[0].isDisplayed
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(RegisterPage::class.java)

        const val USERNAME_VALIDATION_ERROR = "Username must be between 3 and 20 characters, start with a letter or number and contain only lowercase letters, numbers and underscores"
        const val USERNAME_LENGTH_ERROR = "size must be between 3 and 20"
        const val USERNAME_UNAVAILABLE_ERROR = "This username is not available"
        const val USER_DISPLAY_NAME_LENGTH_ERROR = "size must be between 2 and 80"
        const val MALFORMED_EMAIL_ERROR = "not a well-formed email address"
        const val REQUIRED_FIELD_ERROR = "may not be empty"
        const val EMAIL_TAKEN = "This email address is already taken."
        const val PASSWORD_LENGTH_ERROR = "size must be between 6 and 1024"
    }
}
