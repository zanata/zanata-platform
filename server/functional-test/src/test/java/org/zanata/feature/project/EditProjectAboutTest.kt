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

import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditProjectAboutTest : ZanataTestCase() {

    @Trace(summary = "The administrator can change a project about content")
    @Test
    fun addAboutPageDetails() {
        ZanataRestCaller().createProjectAndVersion(
                "aboutpagetest", "aboutpagetest", "file")

        val aboutTextSource = "This is my *about* text for AF"
        val aboutText = "This is my about text for AF"
        assertThat(LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")

        var projectAboutTab = ProjectWorkFlow()
                .goToProjectByName("aboutpagetest")
                .gotoSettingsTab()
                .gotoSettingsAboutTab()

        assertThat(projectAboutTab.aboutText)
                .describedAs("The text is empty")
                .isNullOrEmpty()

        projectAboutTab = projectAboutTab
                .clearAboutText()
                .enterAboutText(aboutTextSource)
                .pressSave()

        projectAboutTab.expectNotification("About page updated.")
        val projectAboutPage = projectAboutTab.gotoAboutTab()

        assertThat(projectAboutPage.aboutText)
                .describedAs("The text in the About tab is correct")
                .isEqualTo(aboutText)
    }
}
