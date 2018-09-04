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
package org.zanata.workflow

import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.support.PageFactory
import org.zanata.page.AbstractPage
import org.zanata.page.BasePage
import org.zanata.page.account.InactiveAccountPage
import org.zanata.page.account.SignInPage
import org.zanata.page.dashboard.DashboardBasePage

class LoginWorkFlow : AbstractWebWorkFlow() {

    private fun <P : AbstractPage> signInAndGoToPage(username: String,
                                                     password: String,
                                                     pageClass: Class<P>): P {
        try {
            doSignIn(username, password)
        } catch (iae: IllegalAccessError) {
            log.warn("Login failed, potential intermittent issue. Retrying...")
            doSignIn(username, password)
        } catch (e: TimeoutException) {
            log.error("Timeout on login. If you are running tests manually " +
                    "with cargo.wait, you possibly forgot to create the test " +
                    "user or administrator (admin/admin).")
            throw e
        }

        return PageFactory.initElements(driver, pageClass)
    }

    fun signIn(username: String, password: String): DashboardBasePage {
        log.info("Accessing zanata at: {}", hostUrl)
        return signInAndGoToPage(username, password, DashboardBasePage::class.java)
    }

    fun signInFailure(username: String, password: String): SignInPage {
        log.info("Log in as username: {}", username)
        return BasePage(driver).clickSignInLink().enterUsername(username)
                .enterPassword(password).clickSignInExpectError()
    }

    fun signInInactive(username: String,
                       password: String): InactiveAccountPage {
        log.info("Log in as username: {}", username)
        return BasePage(driver).clickSignInLink().enterUsername(username)
                .enterPassword(password).clickSignInExpectInactive()
    }

    private fun doSignIn(username: String, password: String) {
        log.info("Log in as username: {}", username)
        val basePage = BasePage(driver)
        basePage.deleteCookiesAndRefresh()
        basePage.clickSignInLink()
                .enterUsername(username)
                .enterPassword(password)
                .clickSignIn()
                .waitForAMoment()
                .withMessage("waiting for login (logout button visible)")
                .until { driver1 ->
                    // only enable this if you temporarily disable implicit
                    // waits:
                    // fail-fast logic
                    // TODO can we enable this now?
                    // List<WebElement> messages =
                    // driver.findElements(By.className("message--danger"));
                    // if (messages.size() > 0 && messages.get(0)
                    // .getText().contains(" Login failed ")) {
                    // throw new IllegalAccessError("Login failed");
                    // }
                    driver1.findElement(By.id("nav_logout"))
                    true
                }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(LoginWorkFlow::class.java)
    }
}
