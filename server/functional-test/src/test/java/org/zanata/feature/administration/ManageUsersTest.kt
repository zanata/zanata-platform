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
package org.zanata.feature.administration

import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.SignInPage
import org.zanata.page.administration.ManageUserAccountPage
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class ManageUsersTest : ZanataTestCase() {

    private lateinit var dashboardPage: DashboardBasePage

    fun loginAsAdmin() {
        dashboardPage = LoginWorkFlow().signIn("admin", "admin")
    }

    @Trace(summary = "The administrator can change a user's password")
    @Test
    @DisplayName("Change a user password")
    fun `Change a user password`() {
        loginAsAdmin()
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterPassword("newpassword")
                .enterConfirmPassword("newpassword")
                .saveUser()
                .logout()

        dashboardPage = LoginWorkFlow().signIn("translator", "newpassword")

        assertThat(dashboardPage.loggedInAs())
                .describedAs("User logged in with new password")
                .isEqualTo("translator")
    }

    @Trace(summary = "The administrator must enter the new user password " +
            "into password and confirm password in order to change it")
    @Test
    @DisplayName("Change a user password fails when not specified")
    fun `Change a user password fails when not specified`() {
        loginAsAdmin()
        val manageUserAccountPage = dashboardPage
                .goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterPassword("newpassword")
                .saveUserExpectFailure()

        assertThat(manageUserAccountPage.errors)
                .describedAs("The password failure error is displayed")
                .contains(ManageUserAccountPage.PASSWORD_ERROR)
    }

    @Trace(summary = "The administrator can disable an account")
    @Test
    @DisplayName("Disable a user account")
    fun disableAUsersAccount() {
        loginAsAdmin()
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .clickEnabled()
                .saveUser()
                .logout()

        val signInPage = BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .enterUsername("translator")
                .enterPassword("translator")
                .clickSignInExpectError()
        assertThat(signInPage.errors)
                .describedAs("The user's account cannot be logged in")
                .contains(SignInPage.LOGIN_FAILED_ERROR)
    }

    @Trace(summary = "The administrator can change a user account's roles")
    @Test
    @DisplayName("Change a user's roles")
    fun `Change a user's roles`() {
        loginAsAdmin()
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .clickRole("admin")
                .saveUser()
                .logout()

        val dashboardBasePage = LoginWorkFlow()
                .signIn("translator", "translator")

        assertThat(dashboardBasePage.goToAdministration().title)
                .describedAs("The user can access the administration panel")
                .isEqualTo("Zanata: Administration")
    }

    @Trace(summary = "The administrator can change a user account's name")
    @Test
    @DisplayName("Change a user's name")
    fun `Change a user's name`() {
        loginAsAdmin()
        val manageUserAccountPage = dashboardPage
                .goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterFullName("Aloy")
                .saveUser()
                .editUserAccount("translator")

        assertThat(manageUserAccountPage.currentName).isEqualTo("Aloy")
    }
}
