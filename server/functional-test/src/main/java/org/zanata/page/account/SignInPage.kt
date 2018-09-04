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
package org.zanata.page.account

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.CorePage
import org.zanata.page.dashboard.DashboardBasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class SignInPage(driver: WebDriver) : CorePage(driver) {
    private val usernameField = By.id("loginForm:username")
    private val passwordField = By.id("loginForm:password")
    private val signInButton = By.id("loginForm:loginButton")
    private val forgotPasswordLink = By.linkText("Forgot your password?")
    private val signUpLink = By.linkText("Sign Up")
    private val titleLabel = By.className("heading--sub")

    /**
     * Retrieve the login page title
     * @return page title string
     */
    val pageTitle: String
        get() {
            log.info("Query page title")
            return readyElement(titleLabel).text
        }

    /**
     * Enter a string into the username field
     * @param username string to enter
     * @return new SignInPage
     */
    fun enterUsername(username: String): SignInPage {
        log.info("Enter username {}", username)
        enterText(readyElement(usernameField), username)
        return SignInPage(driver)
    }

    /**
     * Enter a string into the password field
     * @param password string to enter
     * @return new SignInPage
     */
    fun enterPassword(password: String): SignInPage {
        log.info("Enter password {}", password)
        enterText(readyElement(passwordField), password)
        return SignInPage(driver)
    }

    /**
     * Press the sign in button
     * @return new DashboardBasePage
     */
    fun clickSignIn(): DashboardBasePage {
        log.info("Click Sign In")
        clickElement(signInButton)
        return DashboardBasePage(driver)
    }

    /**
     * Press the sign in button, expecting failure
     * @return new SignInPage
     */
    fun clickSignInExpectError(): SignInPage {
        log.info("Click Sign In")
        clickElement(signInButton)
        return SignInPage(driver)
    }

    /**
     * Press the sign in button, expecting an 'account inactive' response
     * @return new InactiveAccountPage
     */
    fun clickSignInExpectInactive(): InactiveAccountPage {
        log.info("Click Sign In")
        clickElement(signInButton)
        return InactiveAccountPage(driver)
    }

    /**
     * Press the Forgot Password link
     * @return new ResetPasswordPage
     */
    fun goToResetPassword(): ResetPasswordPage {
        log.info("Click Forgot Password")
        clickElement(forgotPasswordLink)
        return ResetPasswordPage(driver)
    }

    /**
     * Press the Sign Up button
     * @return new RegisterPage
     */
    fun goToRegister(): RegisterPage {
        log.info("Click Sign Up")
        clickElement(signUpLink)
        return RegisterPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(SignInPage::class.java)

        const val LOGIN_FAILED_ERROR = "Login failed"
        const val ACTIVATION_SUCCESS = "Your account was successfully activated. You can now sign in."
    }
}
