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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class EditVersionSlugTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        zanataRestCaller.createProjectAndVersion("change-version-slug",
                "oldSlug", "file")
    }

    @Trace(summary = "Project version slug can be changed and page will redirect to new URL")
    @Test
    fun changeVersionSlug() {
        assertThat(LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .describedAs("Admin user has logged in")
                .isEqualTo("admin")

        val versionLanguagesPage = ProjectWorkFlow()
                .goToProjectByName("change-version-slug")
                .gotoVersion("oldSlug")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterVersionID("newSlug")
                .updateVersion()

        versionLanguagesPage.reload()
        assertThat(versionLanguagesPage.url).contains("/newSlug")

        val versionGeneralTab = versionLanguagesPage
                .gotoSettingsTab()
                .gotoSettingsGeneral()
        assertThat(versionGeneralTab.getVersionID())
                .describedAs("The version slug has been changed")
                .isEqualTo("newSlug")
    }
}
