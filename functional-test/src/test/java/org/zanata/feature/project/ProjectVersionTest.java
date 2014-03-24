/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.CreateVersionPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProjectVersionTest {

    @ClassRule
    public static SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void idFieldMustNotBeEmpty() {
        CreateVersionPage createVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").clickCreateVersionLink()
                        .inputVersionId("");

        assertThat("The empty value is rejected",
                createVersionPage.getErrors(),
                Matchers.hasItem("value is required"));
    }

    @Test
    public void idStartsAndEndsWithAlphanumeric() {
        CreateVersionPage createVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").clickCreateVersionLink()
                        .inputVersionId("-A");

        String formatError =
                "must start and end with letter or number, "
                        + "and contain only letters, numbers, underscores and hyphens.";
        assertThat("The input is rejected", createVersionPage.getErrors(),
                Matchers.hasItem(formatError));

        createVersionPage =
                createVersionPage.inputVersionId("B-").waitForNumErrors(1);

        assertThat("The input is rejected", createVersionPage.getErrors(),
                Matchers.hasItem(formatError));

        createVersionPage =
                createVersionPage.inputVersionId("_C_").waitForNumErrors(1);

        assertThat("The input is rejected", createVersionPage.getErrors(),
                Matchers.hasItem(formatError));

        createVersionPage =
                createVersionPage.inputVersionId("A-B_C").waitForNumErrors(0);

        assertThat("The input is acceptable", createVersionPage.getErrors(),
                Matchers.not(Matchers.hasItem(formatError)));
    }

    @Test
    public void overrideLanguages() {
        ProjectVersionPage versionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").clickCreateVersionLink()
                        .inputVersionId("overridetest").saveVersion()
                        .gotoSettingsTab().gotoSettingsLanguagesTab();

        List<String> enabledLocaleList =
                versionPage.gotoSettingsTab().gotoSettingsLanguagesTab()
                        .getEnabledLocaleList();

        assertThat("The enabled list contains three languages",
                enabledLocaleList,
                Matchers.contains("French[fr]", "Hindi[hi]", "Polish[pl]"));

        assertThat(
                "The enabled list does not contains 'English (United States)[en-US]'",
                enabledLocaleList, Matchers.not(Matchers
                        .contains("English (United States)[en-US]")));

        versionPage =
                versionPage.gotoSettingsTab().gotoSettingsLanguagesTab()
                        .removeLocale("pl");

        enabledLocaleList =
                versionPage.gotoSettingsTab().gotoSettingsLanguagesTab()
                        .getEnabledLocaleList();

        assertThat(
                "The enabled list does not contains 'English (United States)[en-US]' and 'Polish[pl]'",
                enabledLocaleList, Matchers.not(Matchers.contains(
                        "English (United States)[en-US]", "Polish[pl]")));

        enabledLocaleList =
                versionPage.gotoSettingsTab().gotoSettingsLanguagesTab()
                        .enterSearchLocale("en-US")
                        .addLocale("English (United States)[en-US]")
                        .getEnabledLocaleList();

        assertThat("Three languages are available to translate",
                enabledLocaleList, Matchers.containsInAnyOrder("French[fr]",
                        "Hindi[hi]", "English (United States)[en-US]"));
    }
}
