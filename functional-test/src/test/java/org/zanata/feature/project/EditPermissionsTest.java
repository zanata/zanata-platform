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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.NoScreenshot;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
@NoScreenshot
public class EditPermissionsTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void maintainerDetailsAreDisplayed() {
        ProjectPermissionsTab projectPermissionsTab =
                new LoginWorkFlow()
                        .signIn("admin", "admin")
                        .goToProjects()
                        .goToProject("about fedora")
                        .gotoSettingsTab()
                        .gotoSettingsPermissionsTab();

        assertThat("The admin user is shown in the list",
                projectPermissionsTab.getSettingsMaintainersList(),
                hasItem("admin"));
    }

    @Test
    public void addMaintainerAsAdmin() {
        ProjectPermissionsTab projectPermissionsTab =
                new LoginWorkFlow()
                        .signIn("admin", "admin")
                        .goToProjects()
                        .goToProject("about fedora")
                        .gotoSettingsTab()
                        .gotoSettingsPermissionsTab();

        assertThat("The translator user is not a maintainer",
                projectPermissionsTab.getSettingsMaintainersList(),
                not(hasItem("translator")));

        projectPermissionsTab = new ProjectWorkFlow()
                .addMaintainer("about fedora", "translator");

        assertThat("The translator user is a maintainer",
                projectPermissionsTab.getSettingsMaintainersList(),
                hasItem("translator"));

        projectPermissionsTab.logout();

        assertThat("The settings tab is now available to the user",
                new LoginWorkFlow().signIn("translator", "translator")
                        .goToProjects()
                        .goToProject("about fedora")
                        .settingsTabIsDisplayed());
    }

    @Test
    public void addMaintainerAsMaintainer() {

        assertThat("Translator has signed in",
                new LoginWorkFlow().signIn("translator", "translator").loggedInAs(),
                equalTo("translator"));

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .createNewSimpleProject("addmaintainer", "addmaintainer")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab()
                .enterSearchMaintainer("glossarist")
                .selectSearchMaintainer("glossarist");

        projectPermissionsTab.waitForMaintainersContains("glossarist");

        assertThat("The glossarist user was added as a maintainer",
                projectPermissionsTab.getSettingsMaintainersList(),
                hasItem("glossarist"));

        projectPermissionsTab.logout();

        ProjectVersionsPage projectVersionsPage = new LoginWorkFlow()
                .signIn("glossarist", "glossarist")
                .goToProjects()
                .goToProject("addmaintainer");

        assertThat("The settings tab is now available to the glossarist",
                projectVersionsPage.settingsTabIsDisplayed());
    }

    @Test
    public void removeMaintainer() {

        assertThat("Translator has signed in",
                new LoginWorkFlow().signIn("translator", "translator").loggedInAs(),
                equalTo("translator"));

        assertThat("The project is created",
                new ProjectWorkFlow()
                        .createNewSimpleProject("removemaintainer",
                                "removemaintainer")
                        .getProjectName(),
                equalTo("removemaintainer"));

        assertThat("Glossarist maintainer is added",
                new ProjectWorkFlow()
                        .addMaintainer("removemaintainer", "glossarist")
                        .getSettingsMaintainersList(),
                hasItem("glossarist"));


        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .removeMaintainer("removemaintainer", "glossarist");

        assertThat("Glossarist maintainer is removed",
                projectPermissionsTab.getSettingsMaintainersList(),
                Matchers.not(hasItem("glossarist")));
    }

    // TODO: Deal with removed self permissions assertion
    @Test(expected = AssertionError.class)
    public void removeSelfAsMaintainer() {

        assertThat("Translator has signed in",
                new LoginWorkFlow().signIn("translator", "translator").loggedInAs(),
                equalTo("translator"));

        assertThat("The project is created",
                new ProjectWorkFlow()
                        .createNewSimpleProject("removemaintainer",
                                "removemaintainer")
                        .getProjectName(),
                equalTo("removemaintainer"));

        ProjectPermissionsTab projectPermissionsTab = new ProjectWorkFlow()
                .addMaintainer("removemaintainer", "admin");

        assertThat("admin maintainer is added",
                projectPermissionsTab.getSettingsMaintainersList(),
                hasItem("admin"));

        ProjectVersionsPage projectVersionsPage = projectPermissionsTab
                .clickRemoveOnSelf("translator")
                .goToHomePage().goToProjects()
                .goToProject("removemaintainer");

        assertThat("The translator user is no longer a maintainer",
                projectVersionsPage.settingsTabIsDisplayed(),
                not(true));
    }

}
