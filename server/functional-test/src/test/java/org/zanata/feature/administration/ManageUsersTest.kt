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

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.account.SignInPage
import org.zanata.page.administration.ManageUserAccountPage
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.util.HasEmailRule
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class ManageUsersTest : ZanataTestCase() {

    @get:Rule
    val emailRule = HasEmailRule()

    private lateinit var dashboardPage: DashboardBasePage

    @Before
    fun before() {
        dashboardPage = LoginWorkFlow().signIn("admin", "admin")
    }

    @Trace(summary = "The administrator can change a user's password")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun changeAUsersPassword() {
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterPassword("newpassword")
                .enterConfirmPassword("newpassword")
                .saveUser()
                .logout()

        dashboardPage = LoginWorkFlow().signIn("translator", "newpassword")

        assertThat(dashboardPage.loggedInAs())
                .`as`("User logged in with new password")
                .isEqualTo("translator")
    }

    @Trace(summary = "The administrator must enter the new user password " +
            "into password and confirm password in order to change it")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun changeAUsersPasswordRequiredFields() {
        val manageUserAccountPage = dashboardPage
                .goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterPassword("newpassword")
                .saveUserExpectFailure()

        assertThat(manageUserAccountPage.errors)
                .`as`("The password failure error is displayed")
                .contains(ManageUserAccountPage.PASSWORD_ERROR)
    }

    @Trace(summary = "The administrator can disable an account")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun disableAUsersAccount() {
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
                .`as`("The user's account cannot be logged in")
                .contains(SignInPage.LOGIN_FAILED_ERROR)
    }

    @Trace(summary = "The administrator can change a user account's roles")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun changeUserRoles() {
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .clickRole("admin")
                .saveUser()
                .logout()

        val dashboardBasePage = LoginWorkFlow()
                .signIn("translator", "translator")

        assertThat(dashboardBasePage.goToAdministration().title)
                .`as`("The user can access the administration panel")
                .isEqualTo("Zanata: Administration")
    }

    @Trace(summary = "The administrator can change a user account's name")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun changeUsersName() {
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
