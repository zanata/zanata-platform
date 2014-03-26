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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.projectsettings.ProjectTranslationTab;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectValidationsTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void setValidationOptions() {

        ProjectTranslationTab projectTranslationTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat("The level for tabs is currently Warning",
                projectTranslationTab.isValidationLevel(
                        "Tab characters (\\t)", "Warning"));

        projectTranslationTab = projectTranslationTab
                .setValidationLevel("HTML/XML tags", "Error")
                .setValidationLevel("Java variables", "Error")
                .setValidationLevel("Leading/trailing newline (\\n)", "Error")
                .setValidationLevel("Printf variables", "Error")
                .setValidationLevel("Tab characters (\\t)", "Error")
                .setValidationLevel("XML entity reference", "Error");


        assumeTrue("RHBZ1017458", projectTranslationTab.hasNoCriticalErrors());

        projectTranslationTab = projectTranslationTab.goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsTranslationTab();

        assertThat("The validation changes were saved", projectTranslationTab
                .isValidationLevel("HTML/XML tags", "Error"));
        assertThat("The validation changes were saved", projectTranslationTab
                .isValidationLevel("Java variables", "Error"));
        assertThat("The validation changes were saved", projectTranslationTab
                .isValidationLevel("Leading/trailing newline (\\n)", "Error"));
        assertThat("The validation changes were saved", projectTranslationTab
                .isValidationLevel("Printf variables", "Error"));
        assertThat("The validation changes were saved", projectTranslationTab
                .isValidationLevel("Tab characters (\\t)", "Error"));
        assertThat("The validation changes were saved", projectTranslationTab
                .isValidationLevel("XML entity reference", "Error"));
    }

    @Test
    public void printfAndPositionalPrintfAreExclusive() {
        ProjectTranslationTab projectTranslationTab = new LoginWorkFlow()
                        .signIn("admin", "admin")
                        .goToProjects()
                        .goToProject("about fedora")
                        .gotoSettingsTab()
                        .gotoSettingsTranslationTab()
                        .setValidationLevel(
                                "Positional printf (XSI extension)", "Error");

        assertThat("The Positional printf level is Error",
                projectTranslationTab.isValidationLevel(
                        "Positional printf (XSI extension)", "Error"));

        assertThat("The Printf level is Off",
                projectTranslationTab
                        .isValidationLevel("Printf variables", "Off"));

        projectTranslationTab = projectTranslationTab
                .setValidationLevel("Printf variables", "Error");

        assertThat("The Printf level is Error",
                projectTranslationTab
                        .isValidationLevel("Printf variables", "Error"));

        assertThat("The Positional printf level is Off",
                projectTranslationTab.isValidationLevel(
                        "Positional printf (XSI extension)", "Off"));
    }
}
