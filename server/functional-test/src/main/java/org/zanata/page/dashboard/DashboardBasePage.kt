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
package org.zanata.page.dashboard

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab
import org.zanata.page.dashboard.dashboardsettings.DashboardClientTab
import org.zanata.page.dashboard.dashboardsettings.DashboardProfileTab
import org.assertj.core.api.Assertions.assertThat

open class DashboardBasePage(driver: WebDriver) : BasePage(driver) {
    private val activityTab = By.id("activity_tab")
    private val projectsTab = By.id("projects_tab")
    private val groupsTab = By.id("groups_tab")
    private val settingsTab = By.id("settings_tab")
    private val activityTabBody = By.id("activity")
    private val projectsTabBody = By.id("projects")
    private val groupsTabBody = By.id("groups")
    private val settingsTabBody = By.id("settings")
    private val settingsAccountTab = By.id("account_tab")
    private val settingsProfileTab = By.id("profile_tab")
    private val settingsClientTab = By.id("client_tab")
    private val profileOverview = By.id("profile-overview")

    val userFullName: String
        get() {
            log.info("Query user full name")
            return readyElement(profileOverview).findElement(By.tagName("h1"))
                    .text
        }

    fun gotoActivityTab(): DashboardActivityTab {
        log.info("Click Activity tab")
        existingElement(activityTabBody)
        clickWhenTabEnabled(readyElement(activityTab))
        return DashboardActivityTab(driver)
    }

    fun activityTabIsSelected(): Boolean {
        log.info("Query is Activity tab displayed")
        return driver.findElements(By.cssSelector("#activity.is-active"))
                .size > 0
    }

    fun gotoProjectsTab(): DashboardProjectsTab {
        log.info("Click Projects tab")
        existingElement(projectsTabBody)
        clickWhenTabEnabled(readyElement(projectsTab))
        return DashboardProjectsTab(driver)
    }

    fun gotoGroupsTab(): DashboardGroupsTab {
        log.info("Click Projects tab")
        existingElement(groupsTabBody)
        clickWhenTabEnabled(readyElement(groupsTab))
        return DashboardGroupsTab(driver)
    }

    fun goToSettingsTab(): DashboardBasePage {
        log.info("Click Settings tab")
        existingElement(settingsTabBody)
        clickWhenTabEnabled(readyElement(settingsTab))
        return DashboardBasePage(driver)
    }

    fun gotoSettingsAccountTab(): DashboardAccountTab {
        log.info("Click Account settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsAccountTab))
        return DashboardAccountTab(driver)
    }

    fun goToSettingsProfileTab(): DashboardProfileTab {
        log.info("Click Profile settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsProfileTab))
        return DashboardProfileTab(driver)
    }

    fun goToSettingsClientTab(): DashboardClientTab {
        log.info("Click Client settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsClientTab))
        return DashboardClientTab(driver)
    }

    fun expectUsernameChanged(current: String) {
        log.info("Wait for username change from {}", current)
        waitForPageSilence()
        assertThat(userFullName).isNotEqualTo(current)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(DashboardBasePage::class.java)
        const val EMAIL_SENT = "You will soon receive an email with a link to activate your email account change."
        const val PASSWORD_UPDATE_SUCCESS = "Your password has been successfully changed."
    }
}
