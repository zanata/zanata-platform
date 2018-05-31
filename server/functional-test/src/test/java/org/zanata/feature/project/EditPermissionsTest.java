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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectPeoplePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditPermissionsTest extends ZanataTestCase {

    private final String TRANSLATOR_KEY = PropertiesHolder
            .getProperty(Constants.zanataTranslatorKey.value());


    @Trace(summary = "The user can view maintainers for a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("Test issue - also implicitly tested via other tests")
    public void maintainerDetailsAreDisplayed() throws Exception {
        ProjectPermissionsTab projectPermissionsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab();

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .as("The admin user is shown in the list")
                .contains("admin");

        ProjectPeoplePage projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab();

        assertThat(projectPeoplePage.getPeople())
                .as("The admin user is shown in the list")
                .contains("Administrator @admin");
    }

    @Trace(summary = "The administrator can add a maintainer to a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addMaintainerAsAdmin() throws Exception {
        ProjectPermissionsTab projectPermissionsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab();

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .as("The translator user is not a maintainer")
                .doesNotContain("translator");

        projectPermissionsTab = new ProjectWorkFlow()
                .addMaintainer("about fedora", "translator");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .as("The translator user is a maintainer")
                .contains("translator");

        /* Workaround for ZNTA-666 */
        projectPermissionsTab.reload();

        ProjectPeoplePage projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab();

        assertThat(projectPeoplePage.getPeople())
                .as("The translator user is shown in the list")
                .contains("translator|Maintainer;");

        projectPeoplePage.logout();

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .settingsTabIsDisplayed())
                .as("The settings tab is now available to the user")
                .isTrue();
    }

    @Trace(summary = "The maintainer can add a maintainer to a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addMaintainerAsMaintainer() throws Exception {
        new ZanataRestCaller("translator", TRANSLATOR_KEY)
                .createProjectAndVersion("addmaintainer", "addmaintainer",
                        "file");

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .as("Translator has signed in")
                .isEqualTo("translator");

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .goToProjectByName("addmaintainer")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab()
                .enterSearchMaintainer("glossarist")
                .selectSearchMaintainer("glossarist");

        projectPermissionsTab.expectMaintainersContains("glossarist");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .as("The glossarist user was added as a maintainer")
                .contains("glossarist");

        /* Workaround for ZNTA-666 */
        projectPermissionsTab.reload();

        ProjectPeoplePage projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab();

        assertThat(projectPeoplePage.getPeople())
                .as("The glossarist user is shown in the list")
                .contains("glossarist|Maintainer;");

        projectPeoplePage.logout();

        ProjectVersionsPage projectVersionsPage = new LoginWorkFlow()
                .signIn("glossarist", "glossarist")
                .gotoExplore()
                .searchAndGotoProjectByName("addmaintainer");

        assertThat(projectVersionsPage.settingsTabIsDisplayed())
                .as("The settings tab is now available to the glossarist")
                .isTrue();
    }

    @Trace(summary = "The maintainer can remove a maintainer from a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void removeMaintainer() throws Exception {
        new ZanataRestCaller("translator", TRANSLATOR_KEY)
                .createProjectAndVersion("removemaintainer", "removemaintainer",
                        "file");
        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .as("Translator has signed in")
                .isEqualTo("translator");

        assertThat(new ProjectWorkFlow()
                .addMaintainer("removemaintainer", "glossarist")
                .getSettingsMaintainersList())
                .as("Glossarist maintainer is added")
                .contains("glossarist");

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .removeMaintainer("removemaintainer", "glossarist");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .as("Glossarist maintainer is removed")
                .doesNotContain("glossarist");

        ProjectPeoplePage projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab();

        assertThat(projectPeoplePage.getPeople())
                .as("The glossarist user is not in the list")
                .doesNotContain("Glossarist|Maintainer;");
    }

    @Trace(summary = "The maintainer can remove themselves as maintainer " +
            "from a project")
    @Ignore("rhbz1151935")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void removeSelfAsMaintainer() throws Exception {
        new ZanataRestCaller("translator", TRANSLATOR_KEY)
                .createProjectAndVersion(
                        "removemaintainer", "removemaintainer", "file");

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .as("Translator has signed in")
                .isEqualTo("translator");

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .addMaintainer("removemaintainer", "admin");

        assertThat(projectPermissionsTab.getSettingsMaintainersList())
                .as("admin maintainer is added")
                .contains("admin");

        projectPermissionsTab.slightPause();
        ProjectBasePage projectBasePage = projectPermissionsTab
                .clickRemoveOnSelf("translator");
        projectBasePage.slightPause();
        projectBasePage.expectNotification("Maintainer \"translator\" has " +
                "been removed from project.");
        ProjectVersionsPage projectVersionsPage = projectBasePage
                .goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("removemaintainer");


        assertThat(projectVersionsPage.settingsTabIsDisplayed())
                .as("The translator user is no longer a maintainer")
                .isFalse();
    }

}
