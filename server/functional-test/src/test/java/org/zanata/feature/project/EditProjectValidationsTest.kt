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
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditProjectValidationsTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The administrator can change the validation levels " + "for a project")
    @Test
    fun setValidationOptions() {

        var projectTranslationTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()

        assertThat(projectTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Warning"))
                .describedAs("The level for tabs is currently Warning")
                .isTrue()

        projectTranslationTab = projectTranslationTab
                .setValidationLevel("HTML/XML tags", "Error")
        projectTranslationTab.reload()
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Java variables", "Error")
        projectTranslationTab.reload()
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Leading/trailing newline (\\n)", "Error")
        projectTranslationTab.reload()
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Printf variables", "Error")
        projectTranslationTab.reload()
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Tab characters (\\t)", "Error")
        projectTranslationTab.reload()
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("XML entity reference", "Error")

        projectTranslationTab = projectTranslationTab.goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()

        assertThat(projectTranslationTab
                .isValidationLevel("HTML/XML tags", "Error"))
                .describedAs("The validation changes were saved")
                .isTrue()
        assertThat(projectTranslationTab
                .isValidationLevel("Java variables", "Error"))
                .describedAs("The validation changes were saved")
                .isTrue()
        assertThat(projectTranslationTab
                .isValidationLevel("Leading/trailing newline (\\n)", "Error"))
                .describedAs("The validation changes were saved")
                .isTrue()
        assertThat(projectTranslationTab
                .isValidationLevel("Printf variables", "Error"))
                .describedAs("The validation changes were saved")
                .isTrue()
        assertThat(projectTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Error"))
                .describedAs("The validation changes were saved")
                .isTrue()
        assertThat(projectTranslationTab
                .isValidationLevel("XML entity reference", "Error"))
                .describedAs("The validation changes were saved")
                .isTrue()
    }

    @Trace(summary = "The system will only allow one of the two Printf " + "validation options to be active at one time")
    @Test
    fun printfAndPositionalPrintfAreExclusive() {
        var projectTranslationTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel(
                        "Positional printf (XSI extension)", "Error")

        // TODO: Uncomment when RHBZ1199852 is fixed
        // projectTranslationTab.expectNotification("Updated validation " +
        //        "Positional printf (XSI extension) to Error.");

        assertThat(projectTranslationTab
                .isValidationLevel("Positional printf (XSI extension)",
                        "Error"))
                .describedAs("The Positional printf level is Error")
                .isTrue()

        assertThat(projectTranslationTab
                .isValidationLevel("Printf variables", "Off"))
                .describedAs("The Printf level is Off")
                .isTrue()

        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Printf variables", "Error")

        // TODO: Uncomment when RHBZ1199852 is fixed
        // projectTranslationTab.expectNotification("Updated validation " +
        //        "Printf variables to Error.");

        assertThat(projectTranslationTab
                .isValidationLevel("Printf variables", "Error"))
                .describedAs("The Printf level is Error")
                .isTrue()

        assertThat(projectTranslationTab
                .isValidationLevel("Positional printf (XSI extension)", "Off"))
                .describedAs("The Positional printf level is Off")
                .isTrue()
    }
}
