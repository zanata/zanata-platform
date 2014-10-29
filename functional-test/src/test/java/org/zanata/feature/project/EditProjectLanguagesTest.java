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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectLanguagesTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Feature(summary = "The administrator can edit the project languages",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void editProjectLanguages() throws Exception {
        ProjectLanguagesTab projectLanguagesTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectEnabledLocaleListCount(3);

        List<String> enabledLocaleList = projectLanguagesTab
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .contains("French[fr]", "Hindi[hi]", "Polish[pl]")
                .as("The enabled list contains three languages");

        assertThat(enabledLocaleList)
                .doesNotContain("English (United States)[en-US]")
                .as("The enabled list does not contain " +
                        "'English (United States)[en-US]'");

        projectLanguagesTab = projectLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .removeLocale("pl")
                .expectEnabledLocaleListCount(2);

        enabledLocaleList = projectLanguagesTab
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .doesNotContain("English (United States)[en-US]")
                .doesNotContain("Polish[pl]")
                .as("The enabled list does not contain 'US English' or Polish");

        enabledLocaleList = projectLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .enterSearchLanguage("en-US")
                .addLanguage("English (United States)[en-US]")
                .expectEnabledLocaleListCount(3)
                .getEnabledLocaleList();

        Assertions.assertThat(enabledLocaleList)
                .contains("English (United States)[en-US]",
                        "French[fr]",
                        "Hindi[hi]")
                .as("The enabled language list contains en-US, fr and hi");
    }
}
