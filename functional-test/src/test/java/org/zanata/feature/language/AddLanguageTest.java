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

import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.feature.ZanataTestCase;
import org.zanata.page.administration.AddLanguagePage;
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class AddLanguageTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    @Ignore("RHBZ-1086036")
    public void addLanguageAsEnabled() {
        String language = "Goa'uld";
        String languageDisplayName = "goa'uld[Goa'uld]";
        ManageLanguagePage manageLanguagePage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToHomePage()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat("The language is not listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.not(Matchers.hasItem(language)));

        manageLanguagePage =
                manageLanguagePage.addNewLanguage().inputLanguage(language)
                        .saveLanguage();

        assertThat("The language is listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.hasItem(language));

        assertThat("The language is enabled by default",
                manageLanguagePage.languageIsEnabled(language));

        List<String> enabledLocaleList = manageLanguagePage.goToHomePage()
                        .goToProjects()
                        .goToProject("about fedora")
                        .gotoVersion("master")
                        .gotoSettingsTab()
                        .gotoSettingsLanguagesTab()
                        .clickInheritCheckbox()
                        .waitForLocaleListVisible()
                        .getEnabledLocaleList();

        assertThat("The language is enabled by default", enabledLocaleList,
                Matchers.hasItem(languageDisplayName));
    }

    @Test
    public void addLanguageAsDisabled() {
        String language = "Klingon";
        String languageDisplayName = "klingon[Klingon]";
        ManageLanguagePage manageLanguagePage =
                new LoginWorkFlow().signIn("admin", "admin").goToHomePage()
                        .goToAdministration().goToManageLanguagePage();

        assertThat("The language is not listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.not(Matchers.hasItem(language)));

        manageLanguagePage =
                manageLanguagePage.addNewLanguage().inputLanguage(language)
                        .disableLanguageByDefault().saveLanguage();

        assertThat("The language is listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.hasItem(language));
        assertThat("The language is disabled by default",
                !manageLanguagePage.languageIsEnabled(language));

        List<String> enabledLocaleList = manageLanguagePage.goToHomePage()
                .goToProjects()
                .goToProject("about fedora").gotoVersion("master")
                .gotoSettingsTab().gotoSettingsLanguagesTab()
                .clickInheritCheckbox()
                .waitForLocaleListVisible()
                .getEnabledLocaleList();

        assertThat("The language is disabled by default", enabledLocaleList,
                Matchers.not(Matchers.hasItem(languageDisplayName)));
    }

    @Test
    public void addKnownLanguage() {
        String language = "ru-RU";
        ManageLanguagePage manageLanguagePage =
                new LoginWorkFlow().signIn("admin", "admin").goToHomePage()
                        .goToAdministration().goToManageLanguagePage();

        assertThat("The language is not listed",
                manageLanguagePage.getLanguageLocales(),
                Matchers.not(Matchers.hasItem(language)));

        AddLanguagePage addLanguagePage =
                manageLanguagePage.addNewLanguage().inputLanguage("ru-RU");

        Map<String, String> languageInfo = addLanguagePage.getLanguageDetails();

        assertThat("The name is correct", languageInfo.get("Name"),
                Matchers.equalTo("Russian (Russia)"));
        assertThat("The native name is correct",
                languageInfo.get("Native Name"),
                Matchers.equalTo("русский (Россия)"));
        assertThat("The language is correct",
                languageInfo.get("Language Code"), Matchers.equalTo("ru"));
        assertThat("The country code is correct",
                languageInfo.get("Country Code"), Matchers.equalTo("RU"));
    }

}
