/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.workflow

import org.openqa.selenium.By
import org.zanata.page.projects.ProjectVersionsPage
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab
import org.zanata.page.projectversion.CreateVersionPage
import org.zanata.page.projectversion.VersionLanguagesPage
import java.util.HashMap

/**
 * This class represents the work-flows involved when interacting with projects,
 * such as creating projects and versions and altering settings.
 */
class ProjectWorkFlow : AbstractWebWorkFlow() {

    /**
     * Creates a new project using minimal input, all other items are default.
     * This function is lenient, i.e. will not fail if the project exists.
     *
     * @param projectId Project identifier
     * @param projectName Project short name
     * @return new Project page for created project
     */
    fun createNewSimpleProject(projectId: String,
                               projectName: String): ProjectVersionsPage {
        val explorePage = goToHome().gotoExplore().enterSearch(projectName)
        if (!explorePage.projectSearchResults.isEmpty() && explorePage
                        .projectSearchResults.contains(projectName)) {
            log.warn("{} already exists. This test environment is not clean.",
                    projectId)
            // since we can't create same project multiple times,
            // if we run this test more than once manually, we don't want it to
            // fail
            return explorePage.clickProjectEntry(projectName)
        }
        return goToHome().goToMyDashboard().gotoProjectsTab()
                .clickOnCreateProjectLink().enterProjectId(projectId)
                .enterProjectName(projectName).pressCreateProject()
    }

    /**
     * Create a project in full, using the details given in settings.<br></br>
     * All items must be defined.
     *
     * @param settings A HashMap of project identifiers and settings
     * @return a new Project page for the created project
     * @see {@link .projectDefaults
     */
    fun createNewProject(settings: HashMap<String, String>): ProjectVersionsPage {
        val dashboard = goToHome().goToMyDashboard()
        val createProjectPage = dashboard.gotoProjectsTab().clickOnCreateProjectLink()
                .enterProjectName(settings["Name"].orEmpty())
                .enterProjectId(settings["Project ID"].orEmpty())
                .enterDescription(settings["Description"].orEmpty())
                .selectProjectType(settings["Project Type"].orEmpty())
        // TODO is waitForPageSilence (in pressCreateProject) enough?
        // Unusual timing issue:
        createProjectPage.slightPause()
        return createProjectPage.pressCreateProject()
    }

    /**
     * By default this will create a podir project version
     *
     * @param projectName
     * project name
     * @param projectVersion
     * project version id
     * @return project version page
     */
    fun createNewProjectVersion(projectName: String,
                                projectVersion: String): VersionLanguagesPage {
        return createNewProjectVersion(projectName, projectVersion, "Podir")
    }

    /**
     * Create a new project version.
     *
     * @param projectName name of the project
     * @param versionID ID of the version
     * @param versionType type of the version
     * @return new Version page, on the default Languages tab
     */
    private fun createNewProjectVersion(projectName: String,
                                        versionID: String,
                                        versionType: String): VersionLanguagesPage {
        val projectVersionsPage = goToProjectByName(projectName)
        if (projectVersionsPage.getVersions().contains(versionID)) {
            log.warn(
                    "{} has already been created. Presumably you are running this test manually and more than once.",
                    versionID)
            return projectVersionsPage.gotoVersion(versionID)
        }
        var createVersionPage: CreateVersionPage = projectVersionsPage.clickCreateVersionLink()
        // First version has no copy options
        if (driver.findElements(By.id("create-version-form:copy-from-version"))
                        .size > 0) {
            createVersionPage = createVersionPage.disableCopyFromVersion()
                    .selectProjectType(versionType)
        }
        return createVersionPage.inputVersionId(versionID).saveVersion()
    }

    fun goToProjectByName(projectName: String): ProjectVersionsPage {
        val explorePage = goToHome().gotoExplore()
        return explorePage.searchAndGotoProjectByName(projectName)
    }

    fun addMaintainer(projectName: String,
                      username: String): ProjectPermissionsTab {
        val projectPermissionsTab = goToProjectByName(projectName).gotoSettingsTab()
                .gotoSettingsPermissionsTab()
                .enterSearchMaintainer(username)
                .selectSearchMaintainer(username)
        projectPermissionsTab.expectMaintainersContains(username)
        return ProjectPermissionsTab(driver)
    }

    fun removeMaintainer(projectName: String,
                         username: String): ProjectPermissionsTab {
        val projectPermissionsTab = goToProjectByName(projectName).gotoSettingsTab()
                .gotoSettingsPermissionsTab()
        projectPermissionsTab.clickRemoveOn(username)
                .expectMaintainersNotContains(username)
        return ProjectPermissionsTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectWorkFlow::class.java)

        fun projectDefaults(): HashMap<String, String> {
            val defaults = HashMap<String, String>()
            defaults["Project ID"] = ""
            defaults["Name"] = ""
            defaults["Description"] = ""
            defaults["Project Type"] = "File"
            return defaults
        }
    }
}
