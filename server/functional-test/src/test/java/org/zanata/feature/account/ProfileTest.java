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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardClientTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardProfileTab;
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

    private static final String adminsApiKey = "b6d7044e9ee3b2447c28fb7c50d86d98";
    private static final String serverUrl = PropertiesHolder
                .getProperty(Constants.zanataInstance.value());

    @Trace(summary = "The user can view their account details")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void verifyProfileData() throws Exception {
        DashboardClientTab dashboardClientTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToSettingsTab()
                .goToSettingsClientTab();

        assertThat(dashboardClientTab.getApiKey())
                .as("The correct api key is present")
                .isEqualTo(adminsApiKey);

        assertThat(dashboardClientTab.getConfigurationDetails())
                .as("The configuration url is correct")
                .contains("localhost.url=" + serverUrl);

        assertThat(dashboardClientTab.getConfigurationDetails())
                .as("The configuration username is correct")
                .contains("localhost.username=admin");

        assertThat(dashboardClientTab.getConfigurationDetails())
                .as("The configuration api key is correct")
                .contains("localhost.key=".concat(adminsApiKey));
    }

    @Trace(summary = "The user can change their API key")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("Procedure call tracking appears to be flaky in this test")
    public void changeUsersApiKey() throws Exception {
        DashboardClientTab dashboardClientTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsClientTab();
        String currentApiKey = dashboardClientTab.getApiKey();

        dashboardClientTab.waitForPageSilence();
        dashboardClientTab = dashboardClientTab.pressApiKeyGenerateButton();
        dashboardClientTab.expectApiKeyChanged(currentApiKey);

        assertThat(dashboardClientTab.getApiKey())
                .as("The user's api key is different")
                .isNotEqualTo(currentApiKey);

        assertThat(dashboardClientTab.getApiKey())
                .as("The user's api key is not empty")
                .isNotEmpty();

        assertThat(dashboardClientTab.getConfigurationDetails())
                .as("The configuration api key matches the label")
                .contains("localhost.key="
                        .concat(dashboardClientTab.getApiKey()));
    }

    @Trace(summary = "The user can change their display name")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeUsersName() throws Exception {
        DashboardProfileTab dashboardProfileTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .goToSettingsProfileTab()
                .enterName("Tranny")
                .clickUpdateProfileButton();

        dashboardProfileTab.expectUsernameChanged("translator");

        assertThat(dashboardProfileTab.getUserFullName())
                .as("The user's name has been changed")
                .isEqualTo("Tranny");
    }

    @Trace(summary = "The user's email address change is validated")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emailValidationIsUsedOnProfileEdit() throws Exception {
        DashboardAccountTab dashboardAccountTab = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("admin@example.com")
                .clickUpdateEmailButton();

        assertThat(dashboardAccountTab.getErrors())
                .as("The email is rejected, being already taken")
                .contains(DashboardAccountTab.EMAIL_TAKEN_ERROR);

        dashboardAccountTab = dashboardAccountTab
                .goToMyDashboard()
                .goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("test @example.com")
                .clickUpdateEmailButton();

        assertThat(dashboardAccountTab.getErrors())
                .as("The email is rejected, being of invalid format")
                .contains(RegisterPage.MALFORMED_EMAIL_ERROR);
    }
}
