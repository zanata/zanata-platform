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

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projectversion.versionsettings.VersionTranslationTab;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeTrue;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditVersionValidationsTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void setValidationOptions() {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat("The level is currently Warning", versionTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Warning"));

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

        assertThat("The changes were saved", versionTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Error"));
    }


    @Test
    public void verifyValidationsAreErrors() {
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
                .setSyntaxHighlighting(false);

        assertThat("The text in the translation target is blank",
                editorPage.getBasicTranslationTargetAtRowIndex(0),
                equalTo(""));

        editorPage.pasteIntoRowAtIndex(0, "\t");

        assertThat("The text in the translation target is now a tab",
                editorPage.getBasicTranslationTargetAtRowIndex(0),
                equalTo("\t"));

        editorPage.defocus();
        editorPage.waitForValidationErrorsVisible();

        assertThat("The notification area shows there's an error",
                editorPage.getValidationMessageCurrentTarget(),
                equalTo("Warning: none, Errors: 1"));

        editorPage = editorPage.openValidationBox();

        assertThat("The correct error is shown for the validation",
                editorPage.getValidationMessageCurrentTarget(),
                Matchers.containsString("Target has more tabs (\\t) than source "
                        + "(source: 0, target: 1)"));
    }

    @Test
    public void userCannotTurnOffEnforcedValidations() {
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

        assertThat("The option is selected",
                editorPage.isValidationOptionSelected(
                        EditorPage.Validations.TABS));

        assertThat("The option is unavailable",
                !editorPage.isValidationOptionAvailable(
                        EditorPage.Validations.TABS));
    }

    @Test
    public void printfAndPositionalPrintfAreExclusive() {
        VersionTranslationTab versionTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel(
                        "Positional printf (XSI extension)", "Error");

        assertThat("The Positional printf level is Error",
                versionTranslationTab.isValidationLevel(
                        "Positional printf (XSI extension)", "Error"));
        assertThat("The Printf level is Off",
                versionTranslationTab.isValidationLevel("Printf variables", "Off"));

        versionTranslationTab.setValidationLevel("Printf variables", "Error");

        assertThat("The Printf level is Error",
                versionTranslationTab.isValidationLevel("Printf variables", "Error"));
        assertThat("The Positional printf level is Off",
                versionTranslationTab.isValidationLevel(
                        "Positional printf (XSI extension)", "Off"));
    }

    @Test
    public void userCanEnableADisabledValidation() {
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
                .setSyntaxHighlighting(false)
                .pasteIntoRowAtIndex(0, "\t");

        assertThat("The validation errors are not shown",
                !editorPage.isValidationMessageCurrentTargetVisible());

        editorPage = editorPage
                .openValidationOptions()
                .clickValidationCheckbox(EditorPage.Validations.TABS);

        editorPage.waitForValidationErrorsVisible();

        assertThat("The validation errors are shown",
                editorPage.isValidationMessageCurrentTargetVisible());
    }

}
