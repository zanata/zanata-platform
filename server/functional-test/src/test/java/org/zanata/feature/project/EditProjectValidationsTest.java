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

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.projectsettings.ProjectTranslationTab;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectValidationsTest extends ZanataTestCase {

    @Before
    public void before() {
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
    }

    @Trace(summary = "The administrator can change the validation levels " +
            "for a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void setValidationOptions() throws Exception {

        ProjectTranslationTab projectTranslationTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat(projectTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Warning"))
                .isTrue()
                .as("The level for tabs is currently Warning");

        projectTranslationTab = projectTranslationTab
                .setValidationLevel("HTML/XML tags", "Error");
        projectTranslationTab.reload();
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Java variables", "Error");
        projectTranslationTab.reload();
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Leading/trailing newline (\\n)", "Error");
        projectTranslationTab.reload();
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Printf variables", "Error");
        projectTranslationTab.reload();
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Tab characters (\\t)", "Error");
        projectTranslationTab.reload();
        projectTranslationTab = projectTranslationTab
                .setValidationLevel("XML entity reference", "Error");

        projectTranslationTab = projectTranslationTab.goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat(projectTranslationTab
                .isValidationLevel("HTML/XML tags", "Error"))
                .isTrue()
                .as("The validation changes were saved");
        assertThat(projectTranslationTab
                .isValidationLevel("Java variables", "Error"))
                .isTrue()
                .as("The validation changes were saved");
        assertThat(projectTranslationTab
                .isValidationLevel("Leading/trailing newline (\\n)", "Error"))
                .isTrue()
                .as("The validation changes were saved");
        assertThat(projectTranslationTab
                .isValidationLevel("Printf variables", "Error"))
                .isTrue()
                .as("The validation changes were saved");
        assertThat(projectTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Error"))
                .isTrue()
                .as("The validation changes were saved");
        assertThat(projectTranslationTab
                .isValidationLevel("XML entity reference", "Error"))
                .isTrue()
                .as("The validation changes were saved");
    }

    @Trace(summary = "The system will only allow one of the two Printf " +
            "validation options to be active at one time")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void printfAndPositionalPrintfAreExclusive() throws Exception {
        ProjectTranslationTab projectTranslationTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab()
                .setValidationLevel(
                        "Positional printf (XSI extension)", "Error");

        // TODO: Uncomment when RHBZ1199852 is fixed
        // projectTranslationTab.expectNotification("Updated validation " +
        //        "Positional printf (XSI extension) to Error.");

        assertThat(projectTranslationTab
                .isValidationLevel("Positional printf (XSI extension)", "Error"))
                .isTrue()
                .as("The Positional printf level is Error");

        assertThat(projectTranslationTab
                .isValidationLevel("Printf variables", "Off"))
                .isTrue()
                .as("The Printf level is Off");

        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Printf variables", "Error");

        // TODO: Uncomment when RHBZ1199852 is fixed
        // projectTranslationTab.expectNotification("Updated validation " +
        //        "Printf variables to Error.");

        assertThat(projectTranslationTab
                .isValidationLevel("Printf variables", "Error"))
                .isTrue()
                .as("The Printf level is Error");

        assertThat(projectTranslationTab
                .isValidationLevel("Positional printf (XSI extension)", "Off"))
                .isTrue()
                .as("The Positional printf level is Off");
    }
}
