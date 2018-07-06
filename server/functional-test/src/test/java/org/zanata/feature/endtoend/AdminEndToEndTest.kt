/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.endtoend

import org.junit.jupiter.api.Test
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.administration.AdministrationPage
import org.zanata.page.administration.ManageUserPage
import org.zanata.page.administration.RoleAssignmentsPage
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.page.more.MorePage
import org.zanata.page.utility.HomePage
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.zanata.feature.testharness.BasicAcceptanceTest
import org.zanata.page.administration.ServerConfigurationPage

/**
 * This aim of this test is to provide a method of testing as many
 * components as possible in a short period of time. Individual tests for
 * UI components via WebDriver are very time expensive.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@BasicAcceptanceTest
class AdminEndToEndTest : ZanataTestCase() {

    private val ADMINUSER = "admin"
    private val USERNAME = "aloy"
    private val EMAIL = "aloy@example.com"
    private val PASSWORD = "4me2test"
    private val USERROLEREGEX = ".+lo.+"
    private val ADMINEMAIL = "admin@test.com"

    @Test
    fun adminEndToEndTest() {

        // Change admin contact address
        val serverConfigurationPage = setUpAdminContactEmail()

        // Add a new user to the system
        var manageUserPage = addNewUser(serverConfigurationPage.goToAdministration())

        // Enable the user directly, set their password
        manageUserPage = updateAndEnableUser(manageUserPage)

        // Log out
        manageUserPage.logout()

        // New user is supposed to be an admin
        newUserRequestsAdminAccess()

        // Admin create a rule assignment for users to be made admin
        val roleAssignmentsPage = adminCreatesRoleAssignmentRule()

        // Log out
        roleAssignmentsPage.logout()

        // New user logs in as admin
        // May require two logins
        val dashboardBasePage = userLogsInAndIsGrantedAdmin()

        // New admin updates the home page
        updateTheHomePage(dashboardBasePage)
    }

    private fun setUpAdminContactEmail(): ServerConfigurationPage {
        return LoginWorkFlow()
                .signIn(ADMINUSER, ADMINUSER)
                .goToAdministration()
                .goToServerConfigPage()
                .inputAdminEmail(ADMINEMAIL)
                .save()
        // Tested by user sending contact admin email
    }

    private fun addNewUser(administrationPage: AdministrationPage): ManageUserPage {
        val manageUserPage = administrationPage.goToManageUserPage()
                .selectCreateNewUser()
                .enterUsername(USERNAME)
                .enterEmail(EMAIL)
                .clickRole("user")
                .saveUser()
        // TODO should we? manageUserPage.removeNotifications();
        manageUserPage.waitForNotificationsGone()
        manageUserPage.reload()
        assertThat(manageUserPage.userList)
                .describedAs("Manage users page contains new user")
                .contains(USERNAME)
        val messages = ZanataTestCase.hasEmailExtension.messages
        assertThat(messages).describedAs("one message").hasSize(1)
        val email = messages[0]
        assertThat(email.envelopeReceiver)
                .describedAs("Outbox contains an email to the new user")
                .contains(EMAIL)
        return manageUserPage
    }

    private fun updateAndEnableUser(manageUserPage: ManageUserPage): ManageUserPage {
        val userPage = manageUserPage.editUserAccount(USERNAME)
                .enterPassword(PASSWORD)
                .enterConfirmPassword(PASSWORD)
                .clickEnabled()
                .saveUser()
        assertThat(userPage.isUserEnabled(USERNAME))
                .describedAs("The new user is enabled")
                .isTrue()
        return userPage
    }

    private fun newUserRequestsAdminAccess(): HomePage {
        val inputMessage = "Can you please make me admin?"
        val homePage = LoginWorkFlow().signIn(USERNAME, PASSWORD)
                .gotoMorePage()
                .clickContactAdmin()
                .inputMessage(inputMessage)
                .send(MorePage::class.java)
                .logout()
        val email = ZanataTestCase.hasEmailExtension.messages[1]
        assertThat(email.envelopeReceiver)
                .describedAs("There is an email to the administrator")
                .contains(ADMINEMAIL)
        assertThat(email.data)
                .describedAs("The email contains the user's message")
                .contains(*inputMessage.toByteArray())
        return homePage
    }

    private fun adminCreatesRoleAssignmentRule(): RoleAssignmentsPage {
        val roleAssignmentsPage = LoginWorkFlow()
                .signIn(ADMINUSER, ADMINUSER)
                .goToAdministration()
                .goToManageRoleAssignments()
                .clickMoreActions()
                .clickCreateNew()
                .enterIdentityPattern(USERROLEREGEX)
                .selectRole("admin")
                .saveRoleAssignment()
        assertThat(roleAssignmentsPage.rulesByPattern)
                .describedAs("The role rule was created")
                .contains(USERROLEREGEX)
        return roleAssignmentsPage
    }

    private fun userLogsInAndIsGrantedAdmin(): DashboardBasePage {
        var dashboardBasePage = LoginWorkFlow()
                .signIn(USERNAME, PASSWORD)
        // May require two logins
        run {
            if (!dashboardBasePage.isAdministrator) {
                dashboardBasePage.logout()
                dashboardBasePage = LoginWorkFlow()
                        .signIn(USERNAME, PASSWORD)
            }
        }
        assertThat(dashboardBasePage.isAdministrator)
                .describedAs("The new user is now an administrator")
                .isTrue()
        return dashboardBasePage
    }

    private fun updateTheHomePage(dashboardBasePage: DashboardBasePage): HomePage {
        val homePage = dashboardBasePage
                .goToHomePage()
                .goToEditPageContent()
                .enterText("This is some stuff right here")
                .update()
        assertThat(homePage.mainBodyContent)
                .describedAs("The home page was updated with the new text")
                .contains("This is some stuff right here")
        return homePage
    }
}
