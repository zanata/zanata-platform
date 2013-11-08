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

package org.zanata.feature.language;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.administration.AddLanguagePage;
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.projects.CreateVersionPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *      href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class AddLanguageTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule =
            new ResetDatabaseRule(ResetDatabaseRule.Config.WithData);

    @Test
    public void addLanguageAsEnabled() {
        String language = "Goa'uld";
        String languageDisplayName = "goa'uld [Goa'uld] goa'uld";
        ManageLanguagePage manageLanguagePage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToHomePage()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat("The language is not listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.not(Matchers.hasItem(language)));

        manageLanguagePage = manageLanguagePage
                .addNewLanguage()
                .inputLanguage(language)
                .saveLanguage();

        assertThat("The language is listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.hasItem(language));
        assertThat("The language is enabled by default",
                manageLanguagePage.languageIsEnabled(language));

        CreateVersionPage createVersionPage = manageLanguagePage
                .goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion()
                .showLocalesOverride();

        assertThat("The language is enabled by default",
                createVersionPage.getEnabledLanguages(),
                Matchers.hasItem(languageDisplayName));
    }

    @Test
    public void addLanguageAsDisabled() {
        String language = "Klingon";
        String languageDisplayName = "klingon [Klingon] klingon";
        ManageLanguagePage manageLanguagePage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToHomePage()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat("The language is not listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.not(Matchers.hasItem(language)));

        manageLanguagePage = manageLanguagePage
                .addNewLanguage()
                .inputLanguage(language)
                .disableLanguageByDefault()
                .saveLanguage();

        assertThat("The language is listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.hasItem(language));
        assertThat("The language is disabled by default",
                !manageLanguagePage.languageIsEnabled(language));

        CreateVersionPage createVersionPage = manageLanguagePage
                .goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .goToVersion("master")
                .clickEditVersion()
                .showLocalesOverride();

        assertThat("The language is disabled by default",
                createVersionPage.getDisabledLanguages(),
                Matchers.hasItem(languageDisplayName));
    }

    @Test
    public void addKnownLanguage() {
        String language = "ru-RU";
        ManageLanguagePage manageLanguagePage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToHomePage()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat("The language is not listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.not(Matchers.hasItem(language)));

        AddLanguagePage addLanguagePage = manageLanguagePage
                .addNewLanguage()
                .inputLanguage("ru-RU");

        Map<String, String> languageInfo = addLanguagePage.getLanguageDetails();
        assertThat("The name is correct",
                languageInfo.get("Name"),
                Matchers.equalTo("Russian (Russia)"));
        assertThat("The native name is correct",
                languageInfo.get("Native Name"),
                Matchers.equalTo("русский (Россия)"));
        assertThat("The language is correct",
                languageInfo.get("Language Code"),
                Matchers.equalTo("ru"));
        assertThat("The country code is correct",
                languageInfo.get("Country Code"),
                Matchers.equalTo("RU"));
    }

}
