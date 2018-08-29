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
package org.zanata.page.account

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class EnterNewPasswordPage(driver: WebDriver) : BasePage(driver) {

    private val newPasswordField = By.id("passwordResetActivationForm:passwordFieldContainer:input:password")
    private val confirmPasswordField = By.id("passwordResetActivationForm:confirmPasswordFieldContainer:input:passwordConfirm")
    private val resetPasswordButton = By.id("passwordResetActivationForm:resetPasswordButton")

    /**
     * Enter a new password
     * @param password string to enter
     * @return new EnterNewPasswordPage
     */
    fun enterNewPassword(password: String): EnterNewPasswordPage {
        log.info("Enter new password {}", password)
        enterText(readyElement(newPasswordField), password)
        return EnterNewPasswordPage(driver)
    }

    /**
     * Enter a confirmation password
     * @param password string to enter
     * @return new EnterNewPasswordPage
     */
    fun enterConfirmPassword(password: String): EnterNewPasswordPage {
        log.info("Enter confirm password {}", password)
        enterText(readyElement(confirmPasswordField), password)
        return EnterNewPasswordPage(driver)
    }

    /**
     * Press the Change Password button
     * @return new SignInPage
     */
    fun pressChangePasswordButton(): SignInPage {
        log.info("Press Change Password button")
        clickElement(resetPasswordButton)
        return SignInPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(EnterNewPasswordPage::class.java)
    }
}
