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

package org.zanata.feature.project

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditProjectLanguagesTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The administrator can edit the project languages")
    @Test
    fun editProjectLanguages() {
        var projectLanguagesTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectEnabledLocaleListCount(3)

        var enabledLocaleList = projectLanguagesTab
                .enabledLocaleList

        assertThat(enabledLocaleList)
                .describedAs("The enabled list contains three languages")
                .contains("fr", "hi", "pl")

        assertThat(enabledLocaleList)
                .describedAs("The enabled list does not contain " + "'English (United States)[en-US]'")
                .doesNotContain("en-US")

        projectLanguagesTab = projectLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .removeLocale("pl")
                .expectEnabledLocaleListCount(2)

        enabledLocaleList = projectLanguagesTab
                .enabledLocaleList

        assertThat(enabledLocaleList)
                .doesNotContain("en-US")
                .doesNotContain("pl")
                .describedAs("The enabled list does not contain 'US English' or Polish")

        projectLanguagesTab = projectLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .filterDisabledLanguages("nonexistentLocale")
                .expectAvailableLocaleListCount(0)
                .filterDisabledLanguages("en-US")
                .expectAvailableLocaleListCount(1)
        projectLanguagesTab = projectLanguagesTab
                .addLanguage("en-US")
                .expectEnabledLocaleListCount(3)
        enabledLocaleList = projectLanguagesTab
                .enabledLocaleList

        assertThat(enabledLocaleList)
                .describedAs("The enabled language list contains en-US, fr and hi")
                .contains("en-US", "fr", "hi")
        projectLanguagesTab.filterEnabledLanguages("en-US")
                .expectEnabledLocaleListCount(1)
    }

    @Trace(summary = "The administrator can set an alias for a project " + "language")
    @Test
    fun setLanguageAliasTest() {
        var projectLanguagesTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectEnabledLocaleListCount(3)
        projectLanguagesTab = projectLanguagesTab
                .clickLanguageActionsDropdown("pl")
                .clickAddAlias("pl")
                .enterAliasForLocale("pl", "pl-PL")
                .saveLocaleAlias("pl")

        assertThat(projectLanguagesTab.getAlias("pl"))
                .describedAs("The alias was set")
                .isEqualTo("pl-PL")
    }

    @Trace(summary = "The administrator can remove an alias for a project " + "language")
    @Test
    fun removeLanguageAliasTest() {
        var projectLanguagesTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .clickLanguageActionsDropdown("pl")
                .clickAddAlias("pl")
                .enterAliasForLocale("pl", "pl-PL")
                .saveLocaleAlias("pl")

        assertThat(projectLanguagesTab.getAlias("pl"))
                .describedAs("The alias was set")
                .isEqualTo("pl-PL")

        projectLanguagesTab = projectLanguagesTab
                .clickLanguageActionsDropdown("pl")
                .deleteAlias("pl")

        assertThat(projectLanguagesTab.getAlias("pl"))
                .isEmpty()
    }

    @Trace(summary = "The administrator can edit an alias for a project " + "language")
    @Test
    fun editLanguageAliasTest() {
        val locale = "pl"
        var projectLanguagesTab = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .clickLanguageActionsDropdown(locale)
                .clickAddAlias(locale)
                .enterAliasForLocale(locale, "pl-PL")
                .saveLocaleAlias(locale)

        assertThat(projectLanguagesTab.getAlias(locale))
                .describedAs("The alias was set")
                .isEqualTo("pl-PL")

        projectLanguagesTab = projectLanguagesTab
                .clickLanguageActionsDropdown(locale)
                .clickEditAlias(locale)
                .enterAliasForLocale(locale, "pl-POL")
                .saveLocaleAlias(locale)

        assertThat(projectLanguagesTab.getAlias(locale))
                .describedAs("The alias was changed")
                .isEqualTo("pl-POL")
    }
}
