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

import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.RegisterPage
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class ProfileTest : ZanataTestCase() {

    @Trace(summary = "The user can view their account details")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun verifyProfileData() {
        val dashboardClientTab = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToSettingsTab()
                .goToSettingsClientTab()

        assertThat(dashboardClientTab.apiKey)
                .`as`("The correct api key is present")
                .isEqualTo(adminsApiKey)

        assertThat(dashboardClientTab.configurationDetails)
                .`as`("The configuration url is correct")
                .contains("localhost.url=$serverUrl")

        assertThat(dashboardClientTab.configurationDetails)
                .`as`("The configuration username is correct")
                .contains("localhost.username=admin")

        assertThat(dashboardClientTab.configurationDetails)
                .`as`("The configuration api key is correct")
                .contains("localhost.key=$adminsApiKey")
    }

    @Trace(summary = "The user can change their API key")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    //@Ignore("Procedure call tracking appears to be flaky in this test")
    @Throws(Exception::class)
    fun changeUsersApiKey() {
        var dashboardClientTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsClientTab()
        val currentApiKey = dashboardClientTab.apiKey

        dashboardClientTab.waitForPageSilence()
        dashboardClientTab = dashboardClientTab.pressApiKeyGenerateButton()
        dashboardClientTab.expectApiKeyChanged(currentApiKey)

        assertThat(dashboardClientTab.apiKey)
                .`as`("The user's api key is different")
                .isNotEqualTo(currentApiKey)

        assertThat(dashboardClientTab.apiKey)
                .`as`("The user's api key is not empty")
                .isNotEmpty()

        assertThat(dashboardClientTab.configurationDetails)
                .`as`("The configuration api key matches the label")
                .contains("localhost.key=" + dashboardClientTab.apiKey)
    }

    @Trace(summary = "The user can change their display name")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun changeUsersName() {
        val dashboardProfileTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsProfileTab()
                .enterName("Tranny")
                .clickUpdateProfileButton()

        dashboardProfileTab.expectUsernameChanged("translator")

        assertThat(dashboardProfileTab.userFullName)
                .`as`("The user's name has been changed")
                .isEqualTo("Tranny")
    }

    @Trace(summary = "The user's email address change is validated")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun emailValidationIsUsedOnProfileEdit() {
        var dashboardAccountTab = LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("admin@example.com")
                .clickUpdateEmailButton()

        assertThat(dashboardAccountTab.errors)
                .`as`("The email is rejected, being already taken")
                .contains(DashboardAccountTab.EMAIL_TAKEN_ERROR)

        dashboardAccountTab = dashboardAccountTab
                .goToMyDashboard()
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("test @example.com")
                .clickUpdateEmailButton()

        assertThat(dashboardAccountTab.errors)
                .`as`("The email is rejected, being of invalid format")
                .contains(RegisterPage.MALFORMED_EMAIL_ERROR)
    }

    companion object {

        private val adminsApiKey = "b6d7044e9ee3b2447c28fb7c50d86d98"
        private val serverUrl = PropertiesHolder
                .getProperty(Constants.zanataInstance.value())
    }
}
