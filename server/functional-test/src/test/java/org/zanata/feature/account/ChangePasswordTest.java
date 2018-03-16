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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ChangePasswordTest extends ZanataTestCase {

    @Before
    public void setUp() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
    }

    @Trace(summary = "The user can change their password", testCaseIds = 5704)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordSuccessful() throws Exception {
        DashboardBasePage dashboard = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .enterOldPassword("translator")
                .enterNewPassword("newpassword")
                .clickUpdatePasswordButton();
        dashboard.expectNotification(DashboardAccountTab.PASSWORD_UPDATE_SUCCESS);
        dashboard.logout();

        assertThat(new BasicWorkFlow().goToHome().hasLoggedIn()).isFalse()
                .as("User is logged out");

        DashboardBasePage dashboardPage = new LoginWorkFlow()
                .signIn("translator", "newpassword");

        assertThat(dashboardPage.hasLoggedIn()).isTrue()
                .as("User has logged in with the new password");
    }

    @Trace(summary = "The user must enter their current password correctly " +
            "to change it")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordCurrentPasswordFailure() throws Exception {
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .enterOldPassword("nottherightpassword")
                .enterNewPassword("somenewpassword")
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getErrors())
                .contains(DashboardAccountTab.INCORRECT_OLD_PASSWORD_ERROR)
                .as("Old password is incorrect error is shown");
    }

    @Trace(summary = "The user must enter a non-empty new password " +
            "to change it")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordRequiredFieldsAreNotEmpty() throws Exception {
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getErrors())
                .contains(DashboardAccountTab.FIELD_EMPTY_ERROR)
                .as("Empty password message displayed");
    }

    @Trace(summary = "The user must enter a new password of between 6 and " +
            "1024 characters in length to change it")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordAreOfRequiredLength() throws Exception {
        String tooShort = "test5";
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .enterOldPassword("translator")
                .enterNewPassword(tooShort)
                .clickUpdatePasswordButton();

        assertThat(dashboardAccountTab.getErrors())
                .contains(DashboardAccountTab.PASSWORD_LENGTH_ERROR)
                .as("Incorrect password length message displayed");
    }
}
