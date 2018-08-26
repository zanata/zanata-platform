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
import org.junit.jupiter.api.extension.ExtendWith
import org.zanata.feature.testharness.BasicAcceptanceTest
import org.zanata.page.administration.ServerConfigurationPage
import org.zanata.util.HasEmailExtension

/**
 * This aim of this test is to provide a method of testing as many
 * components as possible in a short period of time. Individual tests for
 * UI components via WebDriver are very time expensive.
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@BasicAcceptanceTest
@ExtendWith(HasEmailExtension::class)
class AdminEndToEndTest : ZanataTestCase() {

    private val adminUser = "admin"
    private val username = "aloy"
    private val email = "aloy@example.com"
    private val password = "4me2test"
    private val userRoleRegex = ".+lo.+"
    private val adminEmail = "admin@test.com"

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
                .signIn(adminUser, adminUser)
                .goToAdministration()
                .goToServerConfigPage()
                .inputAdminEmail(adminEmail)
                .save()
        // Tested by user sending contact admin email
    }

    private fun addNewUser(administrationPage: AdministrationPage): ManageUserPage {
        val manageUserPage = administrationPage.goToManageUserPage()
                .selectCreateNewUser()
                .enterUsername(username)
                .enterEmail(email)
                .clickRole("user")
                .saveUser()
        // TODO should we? manageUserPage.removeNotifications();
        manageUserPage.waitForNotificationsGone()
        manageUserPage.reload()
        assertThat(manageUserPage.userList)
                .describedAs("Manage users page contains new user")
                .contains(username)
        val messages = hasEmailExtension.messages
        assertThat(messages).describedAs("one message").hasSize(1)
        val email = messages[0]
        assertThat(email.envelopeReceiver)
                .describedAs("Outbox contains an email to the new user")
                .contains(this.email)
        return manageUserPage
    }

    private fun updateAndEnableUser(manageUserPage: ManageUserPage): ManageUserPage {
        val userPage = manageUserPage.editUserAccount(username)
                .enterPassword(password)
                .enterConfirmPassword(password)
                .clickEnabled()
                .saveUser()
        assertThat(userPage.isUserEnabled(username))
                .describedAs("The new user is enabled")
                .isTrue()
        return userPage
    }

    private fun newUserRequestsAdminAccess(): HomePage {
        val inputMessage = "Can you please make me admin?"
        val homePage = LoginWorkFlow().signIn(username, password)
                .gotoMorePage()
                .clickContactAdmin()
                .inputMessage(inputMessage)
                .send(MorePage::class.java)
                .logout()
        val email = hasEmailExtension.messages[1]
        assertThat(email.envelopeReceiver)
                .describedAs("There is an email to the administrator")
                .contains(adminEmail)
        assertThat(email.data)
                .describedAs("The email contains the user's message")
                .contains(*inputMessage.toByteArray())
        return homePage
    }

    private fun adminCreatesRoleAssignmentRule(): RoleAssignmentsPage {
        val roleAssignmentsPage = LoginWorkFlow()
                .signIn(adminUser, adminUser)
                .goToAdministration()
                .goToManageRoleAssignments()
                .clickMoreActions()
                .clickCreateNew()
                .enterIdentityPattern(userRoleRegex)
                .selectRole("admin")
                .saveRoleAssignment()
        assertThat(roleAssignmentsPage.rulesByPattern)
                .describedAs("The role rule was created")
                .contains(userRoleRegex)
        return roleAssignmentsPage
    }

    private fun userLogsInAndIsGrantedAdmin(): DashboardBasePage {
        var dashboardBasePage = LoginWorkFlow()
                .signIn(username, password)
        // May require two logins
        run {
            if (!dashboardBasePage.isAdministrator) {
                dashboardBasePage.logout()
                dashboardBasePage = LoginWorkFlow()
                        .signIn(username, password)
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
