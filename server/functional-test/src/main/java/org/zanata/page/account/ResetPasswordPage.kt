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
class ResetPasswordPage(driver: WebDriver) : BasePage(driver) {

    private val usernameEmailField = By.id("passwordResetRequestForm:usernameEmail:input:usernameEmail")
    private val submitButton = By.id("passwordResetRequestForm:submitRequest")

    /**
     * Enter a string into the username / email field
     * @param usernameEmail string to enter
     * @return new ResetPasswordPage
     */
    fun enterUserNameEmail(usernameEmail: String): ResetPasswordPage {
        log.info("Enter username or email {}", usernameEmail)
        enterText(readyElement(usernameEmailField), usernameEmail)
        defocus(usernameEmailField)
        return ResetPasswordPage(driver)
    }

    /**
     * Clear the username field of text
     * @return new ResetPasswordPage
     */
    fun clearUsernameField(): ResetPasswordPage {
        log.info("Clear username field")
        readyElement(usernameEmailField).clear()
        defocus(usernameEmailField)
        return ResetPasswordPage(driver)
    }

    /**
     * Press the submit button
     * @return new HomePage
     */
    fun resetPassword(): HomePage {
        log.info("Click Submit")
        clickElement(submitButton)
        return HomePage(driver)
    }

    /**
     * Press the submit button, expecting failure
     * @return new HomePage
     */
    fun resetFailure(): ResetPasswordPage {
        log.info("Click Submit")
        clickElement(submitButton)
        return ResetPasswordPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ResetPasswordPage::class.java)
    }
}
