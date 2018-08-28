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
package org.zanata.page

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.account.RegisterPage
import org.zanata.page.account.SignInPage
import org.zanata.page.administration.AdministrationPage
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.page.explore.ExplorePage
import org.zanata.page.languages.LanguagesPage
import org.zanata.page.utility.HomePage
import org.zanata.workflow.BasicWorkFlow
import com.google.common.base.Preconditions

/**
 * A Base Page is an extension of the Core Page, providing the navigation bar
 * and sidebar links common to most pages outside of the editor.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
open class BasePage(driver: WebDriver) : CorePage(driver) {

    val isAdministrator: Boolean
        get() = driver.findElements(BY_ADMINISTRATION_LINK).size > 0

    /**
     * Check if the page has the home button, expecting a valid base page
     *
     * @return boolean is valid
     */
    val isPageValid: Boolean
        get() = driver.findElements(By.id("nav")).size > 0

    fun goToMyDashboard(): DashboardBasePage {
        log.info("Click Dashboard menu link")
        clickLinkAfterAnimation(BY_DASHBOARD_LINK)
        return DashboardBasePage(driver)
    }

    fun gotoExplore(): ExplorePage {
        log.info("Click Explore menu link")
        clickLinkAfterAnimation(exploreLink)
        return ExplorePage(driver)
    }

    private fun clickNavMenuItem(menuItem: WebElement) {
        slightPause()
        clickElement(menuItem)
    }

    fun goToLanguages(): LanguagesPage {
        log.info("Click Languages")
        clickNavMenuItem(existingElement(languagesLink))
        return LanguagesPage(driver)
    }

    fun goToAdministration(): AdministrationPage {
        log.info("Click Administration menu link")
        clickLinkAfterAnimation(BY_ADMINISTRATION_LINK)
        return AdministrationPage(driver)
    }

    fun goToRegistration(): RegisterPage {
        log.info("Click Sign Up")
        Preconditions.checkArgument(!hasLoggedIn(),
                "User has logged in! You should sign out or delete cookie first in your test.")
        clickElement(registrationLink)
        return RegisterPage(driver)
    }

    fun clickSignInLink(): SignInPage {
        log.info("Click Log In")
        clickElement(BY_SIGN_IN)
        return SignInPage(driver)
    }

    fun hasLoggedIn(): Boolean {
        log.info("Query user is logged in")
        waitForPageSilence()
        val logoutLink = driver.findElements(BY_SIGN_OUT)
        return logoutLink.size > 0
    }

    fun loggedInAs(): String {
        log.info("Query logged in user name")
        return existingElement(NAV).getAttribute("name")
    }

    fun logout(): HomePage {
        log.info("Click Log Out")
        clickLinkAfterAnimation(BY_SIGN_OUT)
        // Handle RHBZ1197955
        if (driver.findElements(By.className("error-div")).size > 0) {
            log.info("RHBZ1197955 encountered, go to homepage")
            BasicWorkFlow().goToHome()
        }
        return HomePage(driver)
    }

    /**
     * This is a workaround for
     * https://code.google.com/p/selenium/issues/detail?id=2766 Elemenet not
     * clickable at point due to the change coordinate of element in page.
     *
     * @param locator
     */
    fun clickLinkAfterAnimation(locator: By) {
        clickLinkAfterAnimation(existingElement(locator))
    }

    fun clickLinkAfterAnimation(element: WebElement) {
        executor.executeScript("arguments[0].click();", element)
    }

    fun clickWhenTabEnabled(tab: WebElement) {
        waitForPageSilence()
        clickElement(tab)
    }

    fun closeNotification() {
        driver.findElement(By.className("ant-notification-notice-close")).click()
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(BasePage::class.java)
        private val NAV = By.id("nav")
        private val languagesLink = By.id("nav_language")
        private val exploreLink = By.id("nav_search")
        private val BY_SIGN_IN = By.id("nav_login")
        private val BY_SIGN_OUT = By.id("nav_logout")
        private val BY_DASHBOARD_LINK = By.id("nav_dashboard")
        private val BY_ADMINISTRATION_LINK = By.id("nav_admin")
        private val registrationLink = By.id("nav_sign_up")
    }
}
