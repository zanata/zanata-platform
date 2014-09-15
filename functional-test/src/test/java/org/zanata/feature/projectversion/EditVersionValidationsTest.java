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
package org.zanata.feature.projectversion;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projectversion.versionsettings.VersionTranslationTab;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditVersionValidationsTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Feature(summary = "The administrator can set validation options for " +
            "a project version",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void setValidationOptions() throws Exception {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat(versionTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Warning"))
                .isTrue()
                .as("The level is currently Warning");

        versionTranslationTab = versionTranslationTab
                .setValidationLevel("Tab characters (\\t)", "Error");

        assumeTrue("RHBZ1017458", versionTranslationTab.hasNoCriticalErrors());

        versionTranslationTab = versionTranslationTab
                .goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat(versionTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Error"))
                .as("The changes were saved");
    }

    @Feature(summary = "The system recognises validation errors options in " +
            "translation targets and displays them to the user",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("broken test")
    public void verifyValidationsAreErrors() throws Exception {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel("Tab characters (\\t)", "Error");

        assumeTrue("RHBZ1017458", versionTranslationTab.hasNoCriticalErrors());

        EditorPage editorPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora");

        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0))
                .isEqualTo("")
                .as("The text in the translation target is blank");

        editorPage.pasteIntoRowAtIndex(0, "\t").saveAsFuzzyAtRow(0);
        editorPage.waitForValidationErrorsVisible();

        assertThat(editorPage.getValidationMessageCurrentTarget())
                .isEqualTo("Warning: none, Errors: 1")
                .as("The notification area shows there's an error");

        editorPage = editorPage.openValidationBox();

        assertThat(editorPage.getValidationMessageCurrentTarget())
                .contains("Target has more tabs (\\t) than source "
                        + "(source: 0, target: 1)")
                .as("The correct error is shown for the validation");
    }

    @Feature(summary = "The user cannot disable enforced validations",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void userCannotTurnOffEnforcedValidations() throws Exception {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel("Tab characters (\\t)", "Error");

        assumeTrue("RHBZ1017458", versionTranslationTab.hasNoCriticalErrors());

        EditorPage editorPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora")
                .openValidationOptions();

        assertThat(editorPage.isValidationOptionSelected(
                        EditorPage.Validations.TABS))
                .isTrue()
                .as("The option is selected");

        assertThat(editorPage.isValidationOptionAvailable(
                        EditorPage.Validations.TABS))
                .isFalse()
                .as("The option is unavailable");
    }

    @Feature(summary = "The user cannot select both printf formats for " +
            "validation options",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void printfAndPositionalPrintfAreExclusive() throws Exception {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel(
                        "Positional printf (XSI extension)", "Error");
        versionTranslationTab.expectNotification(
                "Updated validation Positional printf (XSI extension) to Error.");

        assertThat(versionTranslationTab
                .isValidationLevel("Positional printf (XSI extension)", "Error"))
                .isTrue()
                .as("The Positional printf level is Error");
        assertThat(versionTranslationTab
                .isValidationLevel("Printf variables", "Off"))
                .isTrue()
                .as("The Printf level is Off");

        versionTranslationTab.setValidationLevel("Printf variables", "Error");
        versionTranslationTab.expectNotification(
                "Updated validation Printf variables to Error.");

        assertThat(versionTranslationTab
                .isValidationLevel("Printf variables", "Error"))
                .isTrue()
                .as("The Printf level is Error");
        assertThat(versionTranslationTab
                .isValidationLevel("Positional printf (XSI extension)", "Off"))
                .isTrue()
                .as("The Positional printf level is Off");
    }

    @Feature(summary = "The user can turn on a disabled validation option",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void userCanEnableADisabledValidation() throws Exception {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel("Tab characters (\\t)", "Off");

        assumeTrue("RHBZ1017458", versionTranslationTab.hasNoCriticalErrors());

        EditorPage editorPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora")
                .pasteIntoRowAtIndex(0, "\t")
                .saveAsFuzzyAtRow(0);

        assertThat(editorPage.isValidationMessageCurrentTargetVisible())
                .isFalse()
                .as("The validation errors are not shown");

        editorPage = editorPage
                .openValidationOptions()
                .clickValidationCheckbox(EditorPage.Validations.TABS);
        editorPage.waitForValidationErrorsVisible();

        assertThat(editorPage.isValidationMessageCurrentTargetVisible())
                .isTrue()
                .as("The validation errors are shown");
    }

}
