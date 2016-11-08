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
package org.zanata.feature.administration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.SignInPage;
import org.zanata.page.administration.ManageUserAccountPage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ManageUsersTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule emailRule = new HasEmailRule();

    private DashboardBasePage dashboardPage;

    @Before
    public void before() {
        dashboardPage = new LoginWorkFlow().signIn("admin", "admin");
    }

    @Feature(summary = "The administrator can change a user's password",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeAUsersPassword() throws Exception {
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterPassword("newpassword")
                .enterConfirmPassword("newpassword")
                .saveUser()
                .logout();

        dashboardPage = new LoginWorkFlow().signIn("translator", "newpassword");

        assertThat(dashboardPage.loggedInAs())
                .isEqualTo("translator")
                .as("User logged in with new password");
    }

    @Feature(summary = "The administrator must enter the new user password " +
            "into password and confirm password in order to change it",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeAUsersPasswordRequiredFields() throws Exception {
        ManageUserAccountPage manageUserAccountPage = dashboardPage
                .goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .enterPassword("newpassword")
                .saveUserExpectFailure();

        assertThat(manageUserAccountPage.getErrors())
                .contains(ManageUserAccountPage.PASSWORD_ERROR)
                .as("The password failure error is displayed");
    }

    @Feature(summary = "The administrator can disable an account",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void disableAUsersAccount() throws Exception {
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .clickEnabled()
                .saveUser()
                .logout();

        SignInPage signInPage = new BasicWorkFlow()
                .goToHome()
                .clickSignInLink()
                .enterUsername("translator")
                .enterPassword("translator")
                .clickSignInExpectError();
        assertThat(signInPage.getErrors())
                .contains(SignInPage.LOGIN_FAILED_ERROR)
                .as("The user's account cannot be logged in");
    }

    @Feature(summary = "The administrator can change a user account's roles",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeUserRoles() throws Exception {
        dashboardPage.goToAdministration()
                .goToManageUserPage()
                .editUserAccount("translator")
                .clickRole("admin")
                .saveUser()
                .logout();

        DashboardBasePage dashboardBasePage = new LoginWorkFlow()
                .signIn("translator", "translator");

        assertThat(dashboardBasePage.goToAdministration().getTitle())
                .isEqualTo("Zanata: Administration")
                .as("The user can access the administration panel");
    }
}
