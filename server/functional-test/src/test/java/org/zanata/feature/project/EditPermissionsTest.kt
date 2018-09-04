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

package org.zanata.feature.project

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow
import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditPermissionsTest : ZanataTestCase() {

    private val translatorKey = PropertiesHolder
            .getProperty(Constants.zanataTranslatorKey.value())

    @Trace(summary = "The user can view maintainers for a project")
    @Test
    @Disabled("Test issue - also implicitly tested via other tests")
    @Throws(Exception::class)
    fun maintainerDetailsAreDisplayed() {
        val projectPermissionsTab = LoginWorkFlow()
                .signIn("admin", "admin")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab()

        assertThat(projectPermissionsTab.settingsMaintainersList)
                .describedAs("The admin user is shown in the list")
                .contains("admin")

        val projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab()

        assertThat(projectPeoplePage.people)
                .describedAs("The admin user is shown in the list")
                .contains("Administrator @admin")
    }

    @Trace(summary = "The administrator can add a maintainer to a project")
    @Test
    fun addMaintainerAsAdmin() {
        var projectPermissionsTab = LoginWorkFlow()
                .signIn("admin", "admin")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab()

        assertThat(projectPermissionsTab.settingsMaintainersList)
                .describedAs("The translator user is not a maintainer")
                .doesNotContain("translator")

        projectPermissionsTab = ProjectWorkFlow()
                .addMaintainer("about fedora", "translator")

        assertThat(projectPermissionsTab.settingsMaintainersList)
                .describedAs("The translator user is a maintainer")
                .contains("translator")

        /* Workaround for ZNTA-666 */
        projectPermissionsTab.reload()

        val projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab()

        assertThat(projectPeoplePage.people)
                .describedAs("The translator user is shown in the list")
                .contains("translator|Maintainer;")

        projectPeoplePage.logout()

        assertThat(LoginWorkFlow()
                .signIn("translator", "translator")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .settingsTabIsDisplayed())
                .describedAs("The settings tab is now available to the user")
                .isTrue()
    }

    @Trace(summary = "The maintainer can add a maintainer to a project")
    @Test
    fun addMaintainerAsMaintainer() {
        ZanataRestCaller("translator", translatorKey)
                .createProjectAndVersion("addmaintainer", "addmaintainer",
                        "file")

        assertThat(LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .describedAs("Translator has signed in")
                .isEqualTo("translator")

        val projectPermissionsTab = ProjectWorkFlow()
                .goToProjectByName("addmaintainer")
                .gotoSettingsTab()
                .gotoSettingsPermissionsTab()
                .enterSearchMaintainer("glossarist")
                .selectSearchMaintainer("glossarist")

        projectPermissionsTab.expectMaintainersContains("glossarist")

        assertThat(projectPermissionsTab.settingsMaintainersList)
                .describedAs("The glossarist user was added as a maintainer")
                .contains("glossarist")

        /* Workaround for ZNTA-666 */
        projectPermissionsTab.reload()

        val projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab()

        assertThat(projectPeoplePage.people)
                .describedAs("The glossarist user is shown in the list")
                .contains("glossarist|Maintainer;")

        projectPeoplePage.logout()

        val projectVersionsPage = LoginWorkFlow()
                .signIn("glossarist", "glossarist")
                .gotoExplore()
                .searchAndGotoProjectByName("addmaintainer")

        assertThat(projectVersionsPage.settingsTabIsDisplayed())
                .describedAs("The settings tab is now available to the glossarist")
                .isTrue()
    }

    @Trace(summary = "The maintainer can remove a maintainer from a project")
    @Test
    fun removeMaintainer() {
        ZanataRestCaller("translator", translatorKey)
                .createProjectAndVersion("removemaintainer", "removemaintainer",
                        "file")
        assertThat(LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .describedAs("Translator has signed in")
                .isEqualTo("translator")

        assertThat(ProjectWorkFlow()
                .addMaintainer("removemaintainer", "glossarist")
                .settingsMaintainersList)
                .describedAs("Glossarist maintainer is added")
                .contains("glossarist")

        val projectPermissionsTab = ProjectWorkFlow()
                .removeMaintainer("removemaintainer", "glossarist")

        assertThat(projectPermissionsTab.settingsMaintainersList)
                .describedAs("Glossarist maintainer is removed")
                .doesNotContain("glossarist")

        val projectPeoplePage = projectPermissionsTab
                .gotoPeopleTab()

        assertThat(projectPeoplePage.people)
                .describedAs("The glossarist user is not in the list")
                .doesNotContain("Glossarist|Maintainer;")
    }

    @Trace(summary = "The maintainer can remove themselves as maintainer " +
            "from a project")
    @Disabled("rhbz1151935")
    @Test
    fun removeSelfAsMaintainer() {
        ZanataRestCaller("translator", translatorKey)
                .createProjectAndVersion(
                        "removemaintainer", "removemaintainer", "file")

        assertThat(LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .describedAs("Translator has signed in")
                .isEqualTo("translator")

        val projectPermissionsTab = ProjectWorkFlow()
                .addMaintainer("removemaintainer", "admin")

        assertThat(projectPermissionsTab.settingsMaintainersList)
                .describedAs("admin maintainer is added")
                .contains("admin")

        projectPermissionsTab.slightPause()
        val projectBasePage = projectPermissionsTab
                .clickRemoveOnSelf("translator")
        projectBasePage.slightPause()
        projectBasePage.expectNotification("Maintainer \"translator\" has " +
                "been removed from project.")
        val projectVersionsPage = projectBasePage
                .goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("removemaintainer")


        assertThat(projectVersionsPage.settingsTabIsDisplayed())
                .describedAs("The translator user is no longer a maintainer")
                .isFalse()
    }

}
