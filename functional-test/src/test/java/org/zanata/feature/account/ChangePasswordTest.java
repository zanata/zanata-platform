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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.page.utility.HomePage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

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

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void changePasswordSuccessful() {
        DashboardBasePage dashboard =
                new LoginWorkFlow().signIn("translator", "translator");
        dashboard.goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeOldPassword("translator")
                .typeNewPassword("newpassword")
                .clickUpdatePasswordButton();

        HomePage homePage = dashboard.logout();
        assertThat("User is logged out", !homePage.hasLoggedIn());
        DashboardBasePage dashboardPage =
                new LoginWorkFlow().signIn("translator", "newpassword");
        assertThat("User has logged in with the new password",
                dashboardPage.hasLoggedIn());
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordCurrentPasswordFailure() {
        String incorrectPassword =
                "Old password is incorrect, please check and try again.";
        List<String> fieldErrors =
                new LoginWorkFlow().signIn("translator", "translator")
                        .goToSettingsTab()
                        .gotoSettingsAccountTab()
                        .typeOldPassword("nottherightpassword")
                        .typeNewPassword("somenewpassword")
                        .clickUpdatePasswordButton()
                        .getFieldErrors();

        assertThat("Incorrect password message displayed",
                fieldErrors,
                Matchers.contains(incorrectPassword));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordRequiredFieldsAreNotEmpty() {
        String mayNotBeEmpty = "may not be empty";
        List<String> fieldErrors =
                new LoginWorkFlow().signIn("translator", "translator")
                        .goToSettingsTab()
                        .gotoSettingsAccountTab()
                        .clickUpdatePasswordButton()
                        .getFieldErrors();

        assertThat("Incorrect password message displayed",
                fieldErrors,
                Matchers.contains(mayNotBeEmpty, mayNotBeEmpty));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changePasswordAreOfRequiredLength() {
        String passwordSizeError = "size must be between 6 and 20";
        String tooShort = "test5";
        String tooLong = "t12345678901234567890";
        DashboardAccountTab dashboardAccountTab =
                new LoginWorkFlow().signIn("translator", "translator")
                        .goToSettingsTab()
                        .gotoSettingsAccountTab()
                        .typeOldPassword("translator");

        List<String> fieldErrors =
            dashboardAccountTab
                        .typeNewPassword(tooShort)
                        .clickUpdatePasswordButton()
                        .waitForFieldErrors();
        assertThat("Incorrect password message displayed",
                fieldErrors,
                Matchers.hasItem(passwordSizeError));

        fieldErrors =
                dashboardAccountTab
                        .typeNewPassword(tooLong)
                        .clickUpdatePasswordButton()
                        .waitForFieldErrors();
        assertThat("Incorrect password message displayed",
                fieldErrors,
                Matchers.hasItem(passwordSizeError));
    }
}
