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
package org.zanata.feature.project;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Keys;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.CreateVersionPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Damian Jansen <a
 * href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditValidationsTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void setValidationOptions() {
        CreateVersionPage editVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion();

        assertThat("The level is currently Warning", editVersionPage
                .isValidationLevel("Tab characters (\\t)", "Warning"));

        ProjectVersionPage projectVersionPage = editVersionPage
                .setValidationLevel("Tab characters (\\t)", "Error")
                .clickUpdate();

        assumeTrue("RHBZ1017458", projectVersionPage.hasNoCriticalErrors());

        editVersionPage = projectVersionPage.clickEditVersion();

        assertThat("The changes were saved", editVersionPage
                .isValidationLevel("Tab characters (\\t)", "Error"));
    }

    @Test
    public void verifyValidationsAreErrors() {
        ProjectVersionPage projectVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion()
                .setValidationLevel("Tab characters (\\t)", "Error")
                .clickUpdate();

        assumeTrue("RHBZ1017458", projectVersionPage.hasNoCriticalErrors());

        EditorPage editorPage = projectVersionPage
                .translate("fr")
                .clickDocumentLink("", "About_Fedora")
                .setSyntaxHighlighting(false);

        assertThat("The text in the translation target is blank",
                editorPage.getBasicTranslationTargetAtRowIndex(0),
                Matchers.equalTo(""));

        editorPage.pasteIntoRowAtIndex(0, "\t");

        assertThat("The text in the translation target is now a tab",
                editorPage.getBasicTranslationTargetAtRowIndex(0),
                Matchers.equalTo("\t"));

        editorPage.defocus();
        editorPage.waitForValidationErrorsVisible();

        assertThat("The notification area shows there's an error",
                editorPage.getValidationMessageCurrentTarget(),
                Matchers.equalTo("Warning: none, Errors: 1"));

        editorPage = editorPage.openValidationBox();

        assertThat("The correct error is shown for the validation",
                editorPage.getValidationMessageCurrentTarget(),
                Matchers.containsString("Target has more tabs (\\t) than source "+
                        "(source: 0, target: 1)"));
    }

    @Test
    public void userCannotTurnOffEnforcedValidations() {
        ProjectVersionPage projectVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion()
                .setValidationLevel("Tab characters (\\t)", "Error")
                .clickUpdate();

        assumeTrue("RHBZ1017458", projectVersionPage.hasNoCriticalErrors());

        EditorPage editorPage = projectVersionPage
                .translate("fr")
                .clickDocumentLink("", "About_Fedora")
                .openValidationOptions();

        assertThat("The option is selected", editorPage
                .validationOptionIsSelected(EditorPage.Validations.TABS));
        assertThat("The option is unavailable", !editorPage
                .validationOptionIsAvailable(EditorPage.Validations.TABS));
    }

    @Test
    public void printfAndPositionalPrintfAreExclusive() {
        CreateVersionPage editVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion()
                .setValidationLevel("Positional printf (XSI extension)", "Error");

        assertThat("The Positional printf level is Error", editVersionPage
                .isValidationLevel("Positional printf (XSI extension)", "Error"));
        assertThat("The Printf level is Off", editVersionPage
                .isValidationLevel("Printf variables", "Off"));

        editVersionPage.setValidationLevel("Printf variables", "Error");

        assertThat("The Printf level is Error", editVersionPage
                .isValidationLevel("Printf variables", "Error"));
        assertThat("The Positional printf level is Off", editVersionPage
                .isValidationLevel("Positional printf (XSI extension)", "Off"));
    }

    @Test
    public void userCanEnableADisabledValidation() {
        ProjectVersionPage projectVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion()
                .setValidationLevel("Tab characters (\\t)", "Off")
                .clickUpdate();

        assumeTrue("RHBZ1017458", projectVersionPage.hasNoCriticalErrors());

        EditorPage editorPage = projectVersionPage
                .translate("fr")
                .clickDocumentLink("", "About_Fedora")
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
