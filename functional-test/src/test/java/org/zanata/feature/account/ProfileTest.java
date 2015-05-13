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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardClientTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardProfileTab;
import org.zanata.util.AddUsersRule;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProfileTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    private static final String adminsApiKey = "b6d7044e9ee3b2447c28fb7c50d86d98";
    private static final String serverUrl = PropertiesHolder
                .getProperty(Constants.zanataInstance.value());

    @Feature(summary = "The user can view their account details",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86819)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void verifyProfileData() throws Exception {
        DashboardClientTab dashboardClientTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToSettingsTab()
                .goToSettingsClientTab();

        assertThat(dashboardClientTab.getApiKey()).isEqualTo(adminsApiKey)
                .as("The correct api key is present");

        assertThat(dashboardClientTab.getConfigurationDetails())
                .contains("localhost.url=" + serverUrl)
                .as("The configuration url is correct");

        assertThat(dashboardClientTab.getConfigurationDetails())
                .contains("localhost.username=admin")
                .as("The configuration username is correct");

        assertThat(dashboardClientTab.getConfigurationDetails())
                .contains("localhost.key=".concat(adminsApiKey))
                .as("The configuration api key is correct");
    }

    @Feature(summary = "The user can change their API key",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeUsersApiKey() throws Exception {
        DashboardClientTab dashboardClientTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsClientTab();
        String currentApiKey = dashboardClientTab.getApiKey();

        dashboardClientTab.waitForPageSilence();
        dashboardClientTab = dashboardClientTab.pressApiKeyGenerateButton();
        dashboardClientTab.expectApiKeyChanged(currentApiKey);

        assertThat(dashboardClientTab.getApiKey()).isNotEqualTo(currentApiKey)
                .as("The user's api key is different");

        assertThat(dashboardClientTab.getApiKey()).isNotEmpty()
                .as("The user's api key is not empty");

        assertThat(dashboardClientTab.getConfigurationDetails())
                .contains("localhost.key="
                        .concat(dashboardClientTab.getApiKey()))
                .as("The configuration api key matches the label");
    }

    @Feature(summary = "The user can change their display name",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86822)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeUsersName() throws Exception {
        DashboardProfileTab dashboardProfileTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsProfileTab()
                .enterName("Tranny")
                .clickUpdateProfileButton();

        dashboardProfileTab.expectUsernameChanged("translator");

        assertThat(dashboardProfileTab.getUserFullName()).isEqualTo("Tranny")
                .as("The user's name has been changed");
    }

    @Feature(summary = "The user's email address change is validated",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 86822)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emailValidationIsUsedOnProfileEdit() throws Exception {
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("admin@example.com")
                .clickUpdateEmailButton();

        assertThat(dashboardAccountTab.getErrors())
                .contains(DashboardAccountTab.EMAIL_TAKEN_ERROR)
                .as("The email is rejected, being already taken");

        dashboardAccountTab = dashboardAccountTab
                .goToMyDashboard()
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("test @example.com")
                .clickUpdateEmailButton();

        assertThat(dashboardAccountTab.getErrors())
                .contains(RegisterPage.MALFORMED_EMAIL_ERROR)
                .as("The email is rejected, being of invalid format");
    }
}
