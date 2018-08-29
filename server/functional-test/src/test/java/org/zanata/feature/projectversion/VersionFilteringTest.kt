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

package org.zanata.feature.projectversion

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.projects.ProjectVersionsPage
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
class VersionFilteringTest : ZanataTestCase() {

    @Trace(summary = "The user can filter project versionsList by name")
    @Disabled("dodgy test (intermittent timeout)")
    @Test
    fun versionSearchFiltering() {
        val projectName = "versionsearchnums"
        zanataRestCaller = ZanataRestCaller("translator",
                PropertiesHolder
                        .getProperty(Constants.zanataTranslatorKey.value()))
        zanataRestCaller.createProjectAndVersion(projectName, "alpha", "file")
        zanataRestCaller.createProjectAndVersion(projectName, "bravo", "file")

        assertThat(LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .describedAs("Login as translator")
                .isEqualTo("translator")

        var projectVersionsPage = ProjectWorkFlow()
                .goToProjectByName(projectName)
                .expectDisplayedVersions(2)

        assertVersions(projectVersionsPage, 2,
                arrayOf("bravo", "alpha"))

        projectVersionsPage = projectVersionsPage
                .clickSearchIcon()
                .enterVersionSearch("alpha")
                .expectDisplayedVersions(1)

        assertVersions(projectVersionsPage, 1, arrayOf("alpha"))

        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .expectDisplayedVersions(2)

        assertVersions(projectVersionsPage, 2,
                arrayOf("bravo", "alpha"))

        projectVersionsPage = projectVersionsPage
                .enterVersionSearch("bravo")
                .expectDisplayedVersions(1)

        assertVersions(projectVersionsPage, 1, arrayOf("bravo"))

        projectVersionsPage.waitForPageSilence()
        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .enterVersionSearch("charlie")
                .expectDisplayedVersions(0)

        assertVersions(projectVersionsPage, 0, arrayOf())

        projectVersionsPage.waitForPageSilence()
        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .expectDisplayedVersions(2)

        assertVersions(projectVersionsPage, 2,
                arrayOf("bravo", "alpha"))
    }

    private fun assertVersions(page: ProjectVersionsPage,
                               versionsCount: Int,
                               versionNames: Array<String>) {
        assertThat(page.numberOfDisplayedVersions)
                .describedAs("The version count is $versionsCount")
                .isEqualTo(versionsCount)

        assertThat(page.getVersions())
                .describedAs("The versionsList are correct")
                .contains(*versionNames)
    }
}
