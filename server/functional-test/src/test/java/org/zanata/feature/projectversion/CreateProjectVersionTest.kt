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

package org.zanata.feature.projectversion

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.projectversion.CreateVersionPage
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class CreateProjectVersionTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The administrator can create a project version")
    @Test
    fun createASimpleProjectVersion() {
        val versionLanguagesPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .clickCreateVersionLink()
                .disableCopyFromVersion()
                .inputVersionId("my-aboutfedora-version")
                .saveVersion()

        assertThat(versionLanguagesPage.projectVersionName)
                .describedAs("The version is created with correct ID")
                .isEqualTo("my-aboutfedora-version")
    }

    @Trace(summary = "The user must enter an id to create a project version")
    @Test
    fun idFieldMustNotBeEmpty() {
        val createVersionPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("")
        createVersionPage.defocus(createVersionPage.projectVersionID)

        val errors = createVersionPage.errors
        assertThat(errors)
                .describedAs("The empty value is rejected")
                .contains("value is required")
    }

    @Trace(summary = "The user must enter an id that starts and ends with " +
            "alphanumeric character to create a project version")
    @Test
    fun idStartsAndEndsWithAlphanumeric() {
        var createVersionPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("-A")
        createVersionPage.defocus(createVersionPage.projectVersionID)

        assertThat(createVersionPage.errors)
                .describedAs("The input is rejected")
                .contains(CreateVersionPage.VALIDATION_ERROR)

        createVersionPage = createVersionPage.inputVersionId("B-")
        createVersionPage.defocus(createVersionPage.projectVersionID)

        assertThat(createVersionPage.errors)
                .describedAs("The input is rejected")
                .contains(CreateVersionPage.VALIDATION_ERROR)

        createVersionPage = createVersionPage.inputVersionId("_C_")
        createVersionPage.defocus(createVersionPage.projectVersionID)
        createVersionPage = createVersionPage.expectNumErrors(1)

        assertThat(createVersionPage.errors)
                .describedAs("The input is rejected")
                .contains(CreateVersionPage.VALIDATION_ERROR)

        createVersionPage = createVersionPage.inputVersionId("A-B_C")
        createVersionPage.defocus(createVersionPage.projectVersionID)
        createVersionPage = createVersionPage.expectNumErrors(0)

        assertThat(createVersionPage.errors)
                .describedAs("The input is acceptable")
                .doesNotContain(CreateVersionPage.VALIDATION_ERROR)
    }

    @Trace(summary = "The system updates the project version counter " +
            "when a project version is created")
    @Test
    @Disabled("intermittently failing; see rhbz1168447")
    fun versionCounterIsUpdated() {
        val projectName = "version nums"
        assertThat(ProjectWorkFlow()
                .createNewSimpleProject("version-nums", projectName)
                .projectName)
                .describedAs("The project is created")
                .isEqualTo(projectName)

        var projectVersionsPage = ProjectWorkFlow()
                .createNewProjectVersion(projectName, "alpha")
                .clickProjectLink(projectName)
        projectVersionsPage.expectDisplayedVersions(1)

        assertThat(projectVersionsPage.numberOfDisplayedVersions)
                .describedAs("The version count is 1")
                .isEqualTo(1)

        projectVersionsPage = ProjectWorkFlow()
                .createNewProjectVersion("version nums", "bravo")
                .clickProjectLink(projectName)
        projectVersionsPage.expectDisplayedVersions(2)

        assertThat(projectVersionsPage.numberOfDisplayedVersions)
                .describedAs("The version count is 2")
                .isEqualTo(2)
    }
}
