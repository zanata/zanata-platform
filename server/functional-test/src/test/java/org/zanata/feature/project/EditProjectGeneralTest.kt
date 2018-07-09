/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditProjectGeneralTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }


    @Disabled("Duplicate test with setAProjectToWritable")
    @Trace(summary = "The administrator can set a project to read-only")
    @Test
    fun setAProjectToReadOnly() {
        val explorePage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject()
                .gotoExplore()
                .enterSearch("about fedora")
                .expectProjectListContains("about fedora")
                .logout()
                .gotoExplore()
                .enterSearch("about fedora")

        assertThat(explorePage.projectSearchResults)
                .describedAs("The project is not displayed")
                .doesNotContain("about fedora")
    }

    @Trace(summary = "The administrator can set a read-only project " +
            "to writable")
    @Test
    fun setAProjectToWritable() {
        ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject()
                .gotoExplore()
                .enterSearch("about fedora")
                .expectProjectListContains("about fedora")
        // TODO check whether "about fedora" in the list has a lock icon (locked project)

        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .unlockProject()
                .gotoExplore()
                .enterSearch("about fedora")
                .expectProjectListContains("about fedora")

        assertThat(explorePage.projectSearchResults)
                .describedAs("The project is now displayed")
                .contains("about fedora")
    }

    @Trace(summary = "The administrator can change a project's name")
    @Test
    fun changeProjectName() {
        val replacementText = "a new name"
        val projectVersionsPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterProjectName(replacementText)
                .updateProject()
                .gotoExplore()
                .searchAndGotoProjectByName(replacementText)

        assertThat(projectVersionsPage.projectName)
                .describedAs("The project name has changed")
                .isEqualTo(replacementText)
    }

    @Trace(summary = "The administrator can change a project's description")
    @Test
    fun changeProjectDescription() {
        val replacementText = "a new description"
        val projectVersionsPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")

        assertThat(projectVersionsPage.contentAreaParagraphs)
                .describedAs("The description is default")
                .doesNotContain(replacementText)

        val projectGeneralTab = projectVersionsPage
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterDescription(replacementText)
                .updateProject()

        assertThat(projectGeneralTab.contentAreaParagraphs)
                .describedAs("The text has changed")
                .contains(replacementText)
    }

    @Trace(summary = "The administrator can change a project's type")
    @Test
    fun changeProjectType() {
        var projectGeneralTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .selectProjectType("Properties")
                .updateProject()

        projectGeneralTab.reload()
        projectGeneralTab = projectGeneralTab
                .gotoSettingsTab()
                .gotoSettingsGeneral()

        assertThat(projectGeneralTab.selectedProjectType)
                .describedAs("The project type is correct")
                .isEqualTo("Properties")
    }

    @Trace(summary = "The administrator can change a project's source urls")
    @Test
    fun changeSourceLinks() {
        val projectVersionsPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterHomePage("http://www.example.com")
                .enterRepository("http://git.example.com")
                .updateProject()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")

        assertThat(projectVersionsPage.homepage)
                .describedAs("The homepage is correct")
                .isEqualTo("http://www.example.com")

        assertThat(projectVersionsPage.gitUrl)
                .describedAs("The git url is correct")
                .isEqualTo("http://git.example.com")
    }

    @Trace(summary = "Project slug can be changed and page will redirect to new URL after the change")
    @Test
    fun changeProjectSlug() {
        var projectGeneralTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterProjectSlug("fedora-reborn")
                .updateProject()

        projectGeneralTab.reload()
        assertThat(projectGeneralTab.url).contains("/fedora-reborn")
        projectGeneralTab = projectGeneralTab
                .gotoSettingsTab()
                .gotoSettingsGeneral()

        assertThat(projectGeneralTab.projectId)
                .describedAs("The project slug is correct")
                .isEqualTo("fedora-reborn")
        // FIXME wait for async indexing to finish to avoid interfering with other tests

        // TODO test that search results work for new slug
    }
}
