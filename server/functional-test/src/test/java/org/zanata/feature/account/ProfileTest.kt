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

import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.RegisterPage
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class ProfileTest : ZanataTestCase() {

    @Trace(summary = "The user can view their account details")
    @Test
    @DisplayName("User account details are visible")
    fun `User account details are visible`() {
        val dashboardClientTab = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToSettingsTab()
                .goToSettingsClientTab()

        assertThat(dashboardClientTab.apiKey)
                .describedAs("The correct api key is present")
                .isEqualTo(adminsApiKey)

        assertThat(dashboardClientTab.configurationDetails)
                .describedAs("The configuration url is correct")
                .contains("localhost.url=$serverUrl")

        assertThat(dashboardClientTab.configurationDetails)
                .describedAs("The configuration username is correct")
                .contains("localhost.username=admin")

        assertThat(dashboardClientTab.configurationDetails)
                .describedAs("The configuration api key is correct")
                .contains("localhost.key=$adminsApiKey")
    }

    @Trace(summary = "The user can change their API key")
    @Test
    //@Ignore("Procedure call tracking appears to be flaky in this test")
    @DisplayName("User's api key can be changed")
    fun `User's api key can be changed`() {
        var dashboardClientTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsClientTab()
        val currentApiKey = dashboardClientTab.apiKey

        dashboardClientTab.waitForPageSilence()
        dashboardClientTab = dashboardClientTab.pressApiKeyGenerateButton()
        dashboardClientTab.expectApiKeyChanged(currentApiKey)

        assertThat(dashboardClientTab.apiKey)
                .describedAs("The user's api key is different")
                .isNotEqualTo(currentApiKey)

        assertThat(dashboardClientTab.apiKey)
                .describedAs("The user's api key is not empty")
                .isNotEmpty()

        assertThat(dashboardClientTab.configurationDetails)
                .describedAs("The configuration api key matches the label")
                .contains("localhost.key=" + dashboardClientTab.apiKey)
    }

    @Trace(summary = "The user can change their display name")
    @Test
    @DisplayName("User's display name can be changed")
    fun `User's display name can be changed`() {
        val dashboardProfileTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsProfileTab()
                .enterName("Nonjima")
                .clickUpdateProfileButton()

        dashboardProfileTab.expectUsernameChanged("translator")

        assertThat(dashboardProfileTab.userFullName)
                .describedAs("The user's name has been changed")
                .isEqualTo("Nonjima")
    }

    @Trace(summary = "The user's email address change is validated")
    @Test
    @DisplayName("User's email can be changed")
    fun `User's email can be changed`() {
        var dashboardAccountTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("admin@example.com")
                .clickUpdateEmailButton()

        assertThat(dashboardAccountTab.errors)
                .describedAs("The email is rejected, being already taken")
                .contains(DashboardAccountTab.EMAIL_TAKEN_ERROR)

        dashboardAccountTab = dashboardAccountTab
                .goToMyDashboard()
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("test @example.com")
                .clickUpdateEmailButton()

        assertThat(dashboardAccountTab.errors)
                .describedAs("The email is rejected, being of invalid format")
                .contains(RegisterPage.MALFORMED_EMAIL_ERROR)
    }

    companion object {

        private const val adminsApiKey = "b6d7044e9ee3b2447c28fb7c50d86d98"
        private val serverUrl = PropertiesHolder
                .getProperty(Constants.zanataInstance.value())
    }
}
