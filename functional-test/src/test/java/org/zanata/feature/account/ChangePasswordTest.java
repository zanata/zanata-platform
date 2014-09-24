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
package org.zanata.feature.account;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ChangePasswordTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    @Before
    public void setUp() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
    }

    @Feature(summary = "The user can change their password",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86823)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void changePasswordSuccessful() throws Exception {
        DashboardBasePage dashboard = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeOldPassword("translator")
                .typeNewPassword("newpassword")
                .clickUpdatePasswordButton();
        dashboard.expectNotification(DashboardAccountTab.PASSWORD_UPDATE_SUCCESS);

        assertThat(dashboard.logout().hasLoggedIn()).isFalse()
                .as("User is logged out");

        DashboardBasePage dashboardPage = new LoginWorkFlow()
                .signIn("translator", "newpassword");

        assertThat(dashboardPage.hasLoggedIn()).isTrue()
                .as("User has logged in with the new password");
    }

    @Feature(summary = "The user must enter their current password correctly " +
            "to change it",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86823)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordCurrentPasswordFailure() throws Exception {
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeOldPassword("nottherightpassword")
                .typeNewPassword("somenewpassword")
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getFieldErrors())
                .contains(DashboardAccountTab.INCORRECT_OLD_PASSWORD_ERROR)
                .as("The old password incorrect error is shown");
    }

    @Feature(summary = "The user must enter a non-empty new password " +
            "to change it",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86823)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordRequiredFieldsAreNotEmpty() throws Exception {
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getFieldErrors())
                .contains(DashboardAccountTab.FIELD_EMPTY_ERROR)
                .as("Empty password message displayed");
    }

    @Feature(summary = "The user must enter a new password of between 6 and " +
            "20 characters in length to change it",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86823)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordAreOfRequiredLength() throws Exception {
        String tooShort = "test5";
        String tooLong = "t12345678901234567890";
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeOldPassword("translator")
                .typeNewPassword(tooShort)
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getFieldErrors())
                .contains(DashboardAccountTab.PASSWORD_LENGTH_ERROR)
                .as("Incorrect password length message displayed");

        dashboardAccountTab = dashboardAccountTab
                .typeNewPassword(tooLong)
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getFieldErrors())
                .contains(DashboardAccountTab.PASSWORD_LENGTH_ERROR)
                .as("Incorrect password length message displayed");
    }
}
