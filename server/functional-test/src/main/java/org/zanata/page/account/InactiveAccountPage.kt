/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
class InactiveAccountPage(driver: WebDriver) : BasePage(driver) {

    private val resendEmailButton = By.id("resendEmail")
    private val emailField = By.id("inactiveAccountForm:email:input:emailInput")
    private val updateEmailButton = By.id("inactiveAccountForm:email:input:updateEmail")

    /**
     * Press the Resend Activation Email button
     * @return new HomePage
     */
    fun clickResendActivationEmail(): HomePage {
        log.info("Click resend activation email")
        clickElement(resendEmailButton)
        return HomePage(driver)
    }

    /**
     * Enter a string in the email field
     * @param email string to enter
     * @return new InactiveAccountPage
     */
    fun enterNewEmail(email: String): InactiveAccountPage {
        log.info("Enter new email {}", email)
        enterText(readyElement(emailField), email)
        return InactiveAccountPage(driver)
    }

    /**
     * Press the Update Email button
     * @return new HomePage
     */
    fun clickUpdateEmail(): HomePage {
        log.info("Click Update button")
        clickElement(updateEmailButton)
        return HomePage(driver)
    }

    /**
     * Press the Update Email button, expecting failure
     * @return new InactiveAccountPage
     */
    fun updateEmailFailure(): InactiveAccountPage {
        log.info("Click Update button, expecting failure")
        clickElement(updateEmailButton)
        return InactiveAccountPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(InactiveAccountPage::class.java)

        const val ACCOUNT_UNACTIVATED = "Zanata: Account is not activated"
    }
}
