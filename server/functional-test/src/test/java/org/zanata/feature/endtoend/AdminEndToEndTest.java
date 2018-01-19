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
package org.zanata.feature.endtoend;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.administration.ManageUserPage;
import org.zanata.page.administration.RoleAssignmentsPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.more.MorePage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This aim of this test is to provide a method of testing as many
 * components as possible in a short period of time. Individual tests for
 * UI components via WebDriver are very time expensive.
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(TestPlan.BasicAcceptanceTest.class)
public class AdminEndToEndTest extends ZanataTestCase {

    private final String ADMINUSER = "admin";
    private final String USERNAME = "aloy";
    private final String EMAIL = "aloy@example.com";
    private final String PASSWORD = "4me2test";
    private final String USERROLEREGEX = ".+lo.+";
    private final String ADMINEMAIL = "admin@test.com";

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Test
    public void adminEndToEndTest() {

        // Change admin contact address
        AdministrationPage administrationPage = setUpAdminContactEmail();

        // Add a new user to the system
        ManageUserPage manageUserPage = addNewUser(administrationPage);

        // Enable the user directly, set their password
        manageUserPage = updateAndEnableUser(manageUserPage);

        // Log out
        manageUserPage.logout();

        // New user is supposed to be an admin
        newUserRequestsAdminAccess();

        // Admin create a rule assignment for users to be made admin
        RoleAssignmentsPage roleAssignmentsPage = adminCreatesRoleAssignmentRule();

        // Log out
        roleAssignmentsPage.logout();

        // New user logs in as admin
        // May require two logins
        DashboardBasePage dashboardBasePage = userLogsInAndIsGrantedAdmin();

        // New admin updates the home page
        updateTheHomePage(dashboardBasePage);
    }

    private AdministrationPage setUpAdminContactEmail() {
        return new LoginWorkFlow()
                .signIn(ADMINUSER, ADMINUSER)
                .goToAdministration()
                .goToServerConfigPage()
                .inputAdminEmail(ADMINEMAIL)
                .save();
        // Tested by user sending contact admin email
    }

    private ManageUserPage addNewUser(AdministrationPage administrationPage) {
        ManageUserPage manageUserPage = administrationPage.goToManageUserPage()
                .selectCreateNewUser()
                .enterUsername(USERNAME)
                .enterEmail(EMAIL)
                .clickRole("user")
                .saveUser();
        manageUserPage.waitForNotificationsGone();
        manageUserPage.reload();
        assertThat(manageUserPage.getUserList()).contains(USERNAME);
        WiserMessage email = hasEmailRule.getMessages().get(0);
        assertThat(email.getEnvelopeReceiver()).contains(EMAIL);
        return manageUserPage;
    }

    private ManageUserPage updateAndEnableUser(ManageUserPage manageUserPage) {
        manageUserPage = manageUserPage.editUserAccount(USERNAME)
                .enterPassword(PASSWORD)
                .enterConfirmPassword(PASSWORD)
                .clickEnabled()
                .saveUser();
        assertThat(manageUserPage.isUserEnabled(USERNAME)).isTrue();
        return manageUserPage;
    }

    private HomePage newUserRequestsAdminAccess() {
        final String inputMessage = "Can you please make me admin?";
        HomePage homePage = new LoginWorkFlow().signIn(USERNAME, PASSWORD)
                .gotoMorePage()
                .clickContactAdmin()
                .inputMessage(inputMessage)
                .send(MorePage.class)
                .logout();
        WiserMessage email = hasEmailRule.getMessages().get(1);
        assertThat(email.getEnvelopeReceiver()).contains(ADMINEMAIL);
        assertThat(email.getData()).contains(inputMessage.getBytes());
        return homePage;
    }

    private RoleAssignmentsPage adminCreatesRoleAssignmentRule() {
        RoleAssignmentsPage roleAssignmentsPage = new LoginWorkFlow()
                .signIn(ADMINUSER, ADMINUSER)
                .goToAdministration()
                .goToManageRoleAssignments()
                .clickMoreActions()
                .selectCreateNewRule()
                .enterIdentityPattern(USERROLEREGEX)
                .selectRole("admin")
                .saveRoleAssignment();
        assertThat(roleAssignmentsPage.getRulesByPattern()).contains(USERROLEREGEX);
        return roleAssignmentsPage;
    }

    private DashboardBasePage userLogsInAndIsGrantedAdmin() {
        // May require two logins
        DashboardBasePage dashboardBasePage;
        {
            dashboardBasePage = new LoginWorkFlow()
                    .signIn(USERNAME, PASSWORD);
            if (!dashboardBasePage.isAdministrator()) {
                dashboardBasePage.logout();
                dashboardBasePage = new LoginWorkFlow()
                        .signIn(USERNAME, PASSWORD);
            }
        }
        assertThat(dashboardBasePage.isAdministrator()).isTrue();
        return dashboardBasePage;
    }

    private HomePage updateTheHomePage(DashboardBasePage dashboardBasePage) {
        HomePage homePage = dashboardBasePage
                .goToHomePage()
                .goToEditPageContent()
                .enterText("This is some stuff right here")
                .update();
        assertThat(homePage.getMainBodyContent()).contains("This is some stuff right here");
        return homePage;
    }
}
