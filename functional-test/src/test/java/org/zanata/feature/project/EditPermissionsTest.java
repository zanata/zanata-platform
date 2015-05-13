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

package org.zanata.feature.project;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectMaintainersPage;
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditPermissionsTest extends ZanataTestCase {

    private final String TRANSLATOR_KEY = PropertiesHolder
            .getProperty(Constants.zanataTranslatorKey.value());
    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @After
    public void after() {
        new BasicWorkFlow().goToHome();
    }

    @Feature(summary = "The user can view maintainers for a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void maintainerDetailsAreDisplayed() throws Exception {
        ProjectPermissionsTab projectPermissionsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab();

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .contains("admin")
                .as("The admin user is shown in the list");

        ProjectMaintainersPage projectMaintainersPage = projectPermissionsTab
                .gotoMaintainersTab();

        assertThat(projectMaintainersPage.getMaintainers())
                .contains("Administrator @admin")
                .as("The admin user is shown in the list");
    }

    @Feature(summary = "The administrator can add a maintainer to a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addMaintainerAsAdmin() throws Exception {
        ProjectPermissionsTab projectPermissionsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab();

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .doesNotContain("translator")
                .as("The translator user is not a maintainer");

        projectPermissionsTab = new ProjectWorkFlow()
                .addMaintainer("about fedora", "translator");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .contains("translator")
                .as("The translator user is a maintainer");

        ProjectMaintainersPage projectMaintainersPage = projectPermissionsTab
                .gotoMaintainersTab();

        assertThat(projectMaintainersPage.getMaintainers())
                .contains("translator @translator")
                .as("The translator user is shown in the list");

        projectMaintainersPage.logout();

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToProjects()
                .goToProject("about fedora")
                .settingsTabIsDisplayed())
                .isTrue()
                .as("The settings tab is now available to the user");
    }

    @Feature(summary = "The maintainer can add a maintainer to a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 199006)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addMaintainerAsMaintainer() throws Exception {
        new ZanataRestCaller("translator", TRANSLATOR_KEY)
                .createProjectAndVersion("addmaintainer", "addmaintainer",
                        "file");

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .isEqualTo("translator")
                .as("Translator has signed in");

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .goToProjectByName("addmaintainer")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab()
                .enterSearchMaintainer("glossarist")
                .selectSearchMaintainer("glossarist");

        projectPermissionsTab.expectMaintainersContains("glossarist");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .contains("glossarist")
                .as("The glossarist user was added as a maintainer");

        ProjectMaintainersPage projectMaintainersPage = projectPermissionsTab
                .gotoMaintainersTab();

        assertThat(projectMaintainersPage.getMaintainers())
                .contains("glossarist @glossarist")
                .as("The glossarist user is shown in the list");

        projectMaintainersPage.logout();

        ProjectVersionsPage projectVersionsPage = new LoginWorkFlow()
                .signIn("glossarist", "glossarist")
                .goToProjects()
                .goToProject("addmaintainer");

        assertThat(projectVersionsPage.settingsTabIsDisplayed())
                .isTrue()
                .as("The settings tab is now available to the glossarist");
    }

    @Feature(summary = "The maintainer can remove a maintainer from a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 321234)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void removeMaintainer() throws Exception {
        new ZanataRestCaller("translator", TRANSLATOR_KEY)
                .createProjectAndVersion("removemaintainer", "removemaintainer",
                        "file");
        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .isEqualTo("translator")
                .as("Translator has signed in");

        assertThat(new ProjectWorkFlow()
                .addMaintainer("removemaintainer", "glossarist")
                .getSettingsMaintainersList())
                .contains("glossarist")
                .as("Glossarist maintainer is added");

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .removeMaintainer("removemaintainer", "glossarist");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .doesNotContain("glossarist")
                .as("Glossarist maintainer is removed");

        ProjectMaintainersPage projectMaintainersPage = projectPermissionsTab
                .gotoMaintainersTab();

        assertThat(projectMaintainersPage.getMaintainers())
                .doesNotContain("Glossarist @glossarist")
                .as("The glossarist user is not in the list");
    }

    @Feature(summary = "The maintainer can remove themselves as maintainer " +
            "from a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Ignore("rhbz1151935")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void removeSelfAsMaintainer() throws Exception {
        new ZanataRestCaller("translator", TRANSLATOR_KEY)
                .createProjectAndVersion(
                        "removemaintainer", "removemaintainer", "file");

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .isEqualTo("translator")
                .as("Translator has signed in");

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .addMaintainer("removemaintainer", "admin");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .contains("admin")
                .as("admin maintainer is added");

        projectPermissionsTab.slightPause();
        ProjectBasePage projectBasePage = projectPermissionsTab
                .clickRemoveOnSelf("translator");
        projectBasePage.slightPause();
        projectBasePage.expectNotification("Maintainer \"translator\" has " +
                "been removed from project.");
        ProjectVersionsPage projectVersionsPage = projectBasePage
                .goToHomePage()
                .goToProjects()
                .goToProject("removemaintainer");

        assertThat(projectVersionsPage.settingsTabIsDisplayed())
                .isFalse()
                .as("The translator user is no longer a maintainer");
    }

}
