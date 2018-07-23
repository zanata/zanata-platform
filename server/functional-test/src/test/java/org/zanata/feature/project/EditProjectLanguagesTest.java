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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectLanguagesTest extends ZanataTestCase {

    @Before
    public void before() {
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .as("Admin is logged in")
                .isEqualTo("admin");
    }

    @Trace(summary = "The administrator can edit the project languages")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void editProjectLanguages() throws Exception {
        ProjectLanguagesTab projectLanguagesTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectEnabledLocaleListCount(3);

        List<String> enabledLocaleList = projectLanguagesTab
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .as("The enabled list contains three languages")
                .contains("fr", "hi", "pl");

        assertThat(enabledLocaleList)
                .as("The enabled list does not contain " +
                        "'English (United States)[en-US]'")
                .doesNotContain("en-US");

        projectLanguagesTab = projectLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .removeLocale("pl")
                .expectEnabledLocaleListCount(2);

        enabledLocaleList = projectLanguagesTab
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .doesNotContain("en-US")
                .doesNotContain("pl")
                .as("The enabled list does not contain 'US English' or Polish");

        projectLanguagesTab = projectLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .filterDisabledLanguages("nonexistentLocale")
                .expectAvailableLocaleListCount(0)
                .filterDisabledLanguages("en-US")
                .expectAvailableLocaleListCount(1);
        projectLanguagesTab = projectLanguagesTab
                .addLanguage("en-US")
                .expectEnabledLocaleListCount(3);
        enabledLocaleList = projectLanguagesTab
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .as("The enabled language list contains en-US, fr and hi")
                .contains("en-US", "fr", "hi");
        projectLanguagesTab.filterEnabledLanguages("en-US")
                .expectEnabledLocaleListCount(1);
    }

    @Trace(summary = "The administrator can set an alias for a project " +
            "language")
    @Test
    public void setLanguageAliasTest() {
        ProjectLanguagesTab projectLanguagesTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectEnabledLocaleListCount(3);
        projectLanguagesTab = projectLanguagesTab
                .clickLanguageActionsDropdown("pl")
                .clickAddAlias("pl")
                .enterAliasForLocale("pl", "pl-PL")
                .saveLocaleAlias("pl");

        assertThat(projectLanguagesTab.getAlias("pl"))
                .as("The alias was set")
                .isEqualTo("pl-PL");
    }

    @Trace(summary = "The administrator can remove an alias for a project " +
            "language")
    @Test
    public void removeLanguageAliasTest() {
        ProjectLanguagesTab projectLanguagesTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .clickLanguageActionsDropdown("pl")
                .clickAddAlias("pl")
                .enterAliasForLocale("pl", "pl-PL")
                .saveLocaleAlias("pl");

        assertThat(projectLanguagesTab.getAlias("pl"))
                .as("The alias was set")
                .isEqualTo("pl-PL");

        projectLanguagesTab = projectLanguagesTab
                .clickLanguageActionsDropdown("pl")
                .deleteAlias("pl");

        assertThat(projectLanguagesTab.getAlias("pl"))
                .isEmpty();
    }

    @Trace(summary = "The administrator can edit an alias for a project " +
            "language")
    @Test
    public void editLanguageAliasTest() {
        String locale = "pl";
        ProjectLanguagesTab projectLanguagesTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .clickLanguageActionsDropdown(locale)
                .clickAddAlias(locale)
                .enterAliasForLocale(locale, "pl-PL")
                .saveLocaleAlias(locale);

        assertThat(projectLanguagesTab.getAlias(locale))
                .as("The alias was set")
                .isEqualTo("pl-PL");

        projectLanguagesTab = projectLanguagesTab
                .clickLanguageActionsDropdown(locale)
                .clickEditAlias(locale)
                .enterAliasForLocale(locale, "pl-POL")
                .saveLocaleAlias(locale);

        assertThat(projectLanguagesTab.getAlias(locale))
                .as("The alias was changed")
                .isEqualTo("pl-POL");
    }
}
