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
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class EditVersionLanguagesTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        zanataRestCaller.createProjectAndVersion("langoverride",
                "overridelangtest", "file")
    }

    @Trace(summary = "The maintainer can override the available languages " + "for a project version")
    @Test
    fun changeVersionLanguages() {
        assertThat(LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .describedAs("Admin user has logged in")
                .isEqualTo("admin")

        var versionLanguagesTab = ProjectWorkFlow()
                .goToProjectByName("langoverride")
                .gotoVersion("overridelangtest")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectLocaleListVisible()

        var enabledLocaleList = versionLanguagesTab
                .enabledLocaleList

        assertThat(enabledLocaleList)
                .describedAs("The enabled list contains no languages")
                .contains("fr", "hi", "pl")

        versionLanguagesTab = versionLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .filterDisabledLanguages("nonexistentLocale")
                .expectAvailableLocaleListCount(0)
                .filterDisabledLanguages("en-US")
                .expectAvailableLocaleListCount(1)
                .addLocale("en-US")
        versionLanguagesTab.expectNotification("Language \"en-US\" has been " + "enabled.")
        versionLanguagesTab = versionLanguagesTab
                .expectLanguagesContains("en-US")

        enabledLocaleList = versionLanguagesTab.enabledLocaleList

        assertThat(enabledLocaleList)
                .describedAs("The languages are available to translate")
                .contains("en-US", "fr", "hi", "pl")
        versionLanguagesTab.filterEnabledLanguages("en-US")
                .expectEnabledLocaleListCount(1)
    }
}
