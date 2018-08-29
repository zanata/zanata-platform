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
package org.zanata.feature.account

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.openqa.selenium.TimeoutException
import org.zanata.page.dashboard.DashboardBasePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class ChangePasswordTest : ZanataTestCase() {

    @BeforeEach
    fun setUp() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
    }

    @Trace(summary = "The user can change their password",
            testCaseIds = [5704])
    @Test
    @DisplayName("Change the users password")
    fun `Change the users password`() {
        var dashboard: DashboardAccountTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .enterOldPassword("translator")
                .enterNewPassword("newpassword")
        dashboard.slightPause()
        dashboard = dashboard.clickUpdatePasswordButton()
        try {
            dashboard.expectNotification(DashboardBasePage.PASSWORD_UPDATE_SUCCESS)
        } catch (t: TimeoutException) {
            println("ERROR: Intermittent failure on catching the update message")
        }
        dashboard.logout()

        assertThat(BasicWorkFlow().goToHome().hasLoggedIn())
                .describedAs("User is logged out")
                .isFalse()

        val dashboardPage = LoginWorkFlow()
                .signIn("translator", "newpassword")

        assertThat(dashboardPage.hasLoggedIn())
                .describedAs("User has logged in with the new password")
                .isTrue()
    }

    @Trace(summary = "The user must enter their current password correctly to change it")
    @Test
    @DisplayName("Change password fails with incorrect current password")
    fun `Change password fails with incorrect current password`() {
        val dashboardAccountTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .enterOldPassword("nottherightpassword")
                .enterNewPassword("somenewpassword")
                .clickUpdatePasswordButton()

        assertThat(dashboardAccountTab.errors)
                .describedAs("Old password is incorrect error is shown")
                .contains(DashboardAccountTab.INCORRECT_OLD_PASSWORD_ERROR)
    }

    @Trace(summary = "The user must enter a non-empty new password to change it")
    @Test
    @DisplayName("Change password fails on empty new password")
    fun `Change password fails on empty new password`() {
        val dashboardAccountTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .clickUpdatePasswordButton()

        assertThat(dashboardAccountTab.errors)
                .describedAs("Empty password message displayed")
                .contains(DashboardAccountTab.FIELD_EMPTY_ERROR)
    }

    @Trace(summary = "The user must enter a new password of between 6 and " +
            "1024 characters in length to change it")
    @Test
    @DisplayName("Change password fails when too short")
    fun `Change password fails when too short`() {
        val tooShort = "test5"
        val dashboardAccountTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .enterOldPassword("translator")
                .enterNewPassword(tooShort)
                .clickUpdatePasswordButton()

        assertThat(dashboardAccountTab.errors)
                .describedAs("Incorrect password length message displayed")
                .contains(DashboardAccountTab.PASSWORD_LENGTH_ERROR)
    }
}
