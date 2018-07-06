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

package org.zanata.feature.language

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.administration.AddLanguagePage
import org.zanata.page.languages.LanguagesPage
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class AddLanguageTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The administrator can add a language to Zanata")
    @Test
    fun addLanguageAsEnabledByDefault() {
        val language = "Goa'uld"
        var languagesPage = BasicWorkFlow()
                .goToHome()
                .goToLanguages()

        assertThat(languagesPage.languageLocales)
                .describedAs("The language is not listed")
                .doesNotContain(language)

        languagesPage = languagesPage
                .clickAddNewLanguage()
                .enterSearchLanguage(language)
                .enterLanguageName("Goa'uld")
                .enterLanguageNativeName("Kek mattet")
                .enterLanguagePlurals("nplurals=2; plural=(n != 1)")
                .expectPluralsWarning()
                .saveLanguage()

        assertThat(languagesPage.languageLocales)
                .describedAs("The language is listed")
                .contains(language)

        assertThat(languagesPage.languageIsEnabledByDefault(language))
                .describedAs("The language is enabled by default")
                .isTrue()

        languagesPage.closeNotification()

        val enabledLocaleList = languagesPage
                .goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .enabledLocaleList

        assertThat(enabledLocaleList)
                .describedAs("The language is enabled by default")
                .contains(language)
    }

    @Trace(summary = "The administrator can add a disabled language to Zanata")
    @Test
    fun addLanguageAsDisabledByDefault() {
        val language = "Klingon"
        var languagesPage = BasicWorkFlow()
                .goToHome()
                .goToHomePage()
                .goToLanguages()

        assertThat(languagesPage.languageLocales)
                .describedAs("The language is not listed")
                .doesNotContain(language)

        languagesPage = languagesPage
                .clickAddNewLanguage()
                .enterSearchLanguage(language)
                .expectPluralsWarning()
                .enterLanguageName("ta' Hol")
                .enterLanguageNativeName("tlhIngan")
                .enterLanguagePlurals("nplurals=2; plural=(n != 1)")
                .disableLanguageByDefault()
                .saveLanguage()

        assertThat(languagesPage.languageLocales)
                .describedAs("The language is listed")
                .contains(language)
        assertThat(languagesPage.languageIsEnabledByDefault(language))
                .describedAs("The language is disabled by default")
                .isFalse()

        languagesPage.closeNotification()

        val projectLanguagesTab = languagesPage.goToHomePage()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
        val enabledLocaleList = projectLanguagesTab.enabledLocaleList

        assertThat(enabledLocaleList)
                .describedAs("The language is disabled by default")
                .doesNotContain(language)
    }

    @Trace(summary = "The administrator can add a known language to Zanata")
    @Test
    fun addKnownLanguage() {
        val language = "ru-RU"
        val languagesPage = BasicWorkFlow()
                .goToHome()
                .goToLanguages()

        assertThat(languagesPage.languageLocales)
                .describedAs("The language is not listed")
                .doesNotContain(language)

        val addLanguagePage = languagesPage
                .clickAddNewLanguage()
                .enterSearchLanguage(language)
                .selectSearchLanguage(language)

        assertThat(addLanguagePage.newLanguageName)
                .describedAs("The name is correct")
                .isEqualTo("Russian (Russia)")
        assertThat(addLanguagePage.newLanguageNativeName)
                .describedAs("The native name is correct")
                .isEqualTo("русский (Россия)")
        assertThat(addLanguagePage.newLanguageCode)
                .describedAs("The language is correct")
                .isEqualTo(language)
    }

}
