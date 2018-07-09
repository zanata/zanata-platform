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
import org.zanata.page.webtrans.EditorPage
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditVersionValidationsTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The administrator can set validation options for " + "a project version")
    @Test
    fun setValidationOptions() {
        var versionTranslationTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()

        assertThat(versionTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Warning"))
                .describedAs("The level is currently Warning")
                .isTrue()

        versionTranslationTab = versionTranslationTab
                .setValidationLevel("Tab characters (\\t)", "Error")

        versionTranslationTab = versionTranslationTab
                .goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()

        assertThat(versionTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Error"))
                .describedAs("The changes were saved")
                .isTrue()
    }

    @Trace(summary = "The system recognises validation errors options in " +
            "translation targets and displays them to the user")
    @Test
    fun verifyValidationsAreErrors() {
        ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel("Tab characters (\\t)", "Error")

        var editorPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora")

        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0))
                .describedAs("The text in the translation target is blank")
                .isEqualTo("")

        editorPage.pasteIntoRowAtIndex(0, "\t").saveAsFuzzyAtRow(0)
        editorPage.expectValidationErrorsVisible()

        assertThat(editorPage.validationMessageCurrentTarget)
                .describedAs("The notification area shows there's an error")
                .isEqualTo("Warning: none, Errors: 1")

        editorPage = editorPage.openValidationBox()

        assertThat(editorPage.validationMessageCurrentTarget)
                .describedAs("The correct error is shown for the validation")
                .contains("Target has more tabs (\\t) than source " +
                        "(source: 0, target: 1)")
    }

    @Trace(summary = "The user cannot disable enforced validations")
    @Test
    fun userCannotTurnOffEnforcedValidations() {
        ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel("Tab characters (\\t)", "Error")

        val editorPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora")
                .openValidationOptions()

        assertThat(editorPage.isValidationOptionSelected(
                EditorPage.Validations.TABS))
                .describedAs("The option is selected")
                .isTrue()

        assertThat(editorPage.isValidationOptionAvailable(
                EditorPage.Validations.TABS))
                .describedAs("The option is unavailable")
                .isFalse()
    }

    @Trace(summary = "The user cannot select both printf formats for " +
            "validation options")
    @Test
    fun printfAndPositionalPrintfAreExclusive() {
        val versionTranslationTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel(
                        "Positional printf (XSI extension)", "Error")

        // TODO: Uncomment when RHBZ1199852 is fixed
        // versionTranslationTab.expectNotification(
        //        "Updated validation Positional printf (XSI extension) to Error.");

        assertThat(versionTranslationTab
                .isValidationLevel("Positional printf (XSI extension)",
                        "Error"))
                .describedAs("The Positional printf level is Error")
                .isTrue()
        assertThat(versionTranslationTab
                .isValidationLevel("Printf variables", "Off"))
                .describedAs("The Printf level is Off")
                .isTrue()

        versionTranslationTab.setValidationLevel("Printf variables", "Error")

        // TODO: Uncomment when RHBZ1199852 is fixed
        // versionTranslationTab.expectNotification(
        //        "Updated validation Printf variables to Error.");

        assertThat(versionTranslationTab
                .isValidationLevel("Printf variables", "Error"))
                .describedAs("The Printf level is Error")
                .isTrue()
        assertThat(versionTranslationTab
                .isValidationLevel("Positional printf (XSI extension)", "Off"))
                .describedAs("The Positional printf level is Off")
                .isTrue()
    }

    @Trace(summary = "The user can turn on a disabled validation option")
    @Test
    fun userCanEnableADisabledValidation() {
        ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel("Tab characters (\\t)", "Off")

        var editorPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora")
                .pasteIntoRowAtIndex(0, "\t")
                .saveAsFuzzyAtRow(0)

        assertThat(editorPage.isValidationMessageCurrentTargetVisible)
                .describedAs("The validation errors are not shown")
                .isFalse()

        editorPage = editorPage
                .openValidationOptions()
                .clickValidationCheckbox(EditorPage.Validations.TABS)
        editorPage.expectValidationErrorsVisible()

        assertThat(editorPage.isValidationMessageCurrentTargetVisible)
                .describedAs("The validation errors are shown")
                .isTrue()
    }

}
