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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.administration.AddLanguagePage;
import org.zanata.page.languages.LanguagesPage;
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab;
import org.zanata.util.AddUsersRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class AddLanguageTest extends ZanataTestCase {

    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @BeforeClass
    public static void beforeClass() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
    }

    @Feature(summary = "The administrator can add a language to Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181709)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addLanguageAsEnabledByDefault() throws Exception {
        String language = "Goa'uld";
        String languageDisplayName = "goa'uld[Goa'uld]";
        LanguagesPage languagesPage = new BasicWorkFlow()
            .goToHome()
            .goToLanguages();

        assertThat(languagesPage.getLanguageLocales())
                .doesNotContain(language)
                .as("The language is not listed");

        languagesPage = languagesPage
                .clickMoreActions()
                .addNewLanguage()
                .enterSearchLanguage(language)
                .waitForPluralsWarning()
                .saveLanguage();

        assertThat(languagesPage.getLanguageLocales())
                .contains(language)
                .as("The language is listed");

        assertThat(languagesPage.languageIsEnabledByDefault(language))
                .isTrue()
                .as("The language is enabled by default");

        List<String> enabledLocaleList = languagesPage
                .goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .waitForLocaleListVisible()
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .contains(language)
                .as("The language is enabled by default");
    }

    @Feature(summary = "The administrator can add a disabled language to Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181709)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addLanguageAsDisabledByDefault() throws Exception {
        String language = "Klingon";
        String languageDisplayName = "klingon[Klingon]";
        LanguagesPage languagesPage = new BasicWorkFlow()
                .goToHome()
                .goToHomePage()
                .goToLanguages();

        assertThat(languagesPage.getLanguageLocales())
                .doesNotContain(language)
                .as("The language is not listed");

        languagesPage = languagesPage
                .clickMoreActions()
                .addNewLanguage()
                .enterSearchLanguage(language)
                .waitForPluralsWarning()
                .enableLanguageByDefault(false)
                .saveLanguage();

        assertThat(languagesPage.getLanguageLocales())
                .contains(language)
                .as("The language is listed");
        assertThat(languagesPage.languageIsEnabledByDefault(language))
                .isFalse()
                .as("The language is disabled by default");

        ProjectLanguagesTab projectLanguagesTab = languagesPage.goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .waitForLocaleListVisible();
        List<String> enabledLocaleList = projectLanguagesTab.getEnabledLocaleList();
        List<String> disabledLocaleList = projectLanguagesTab.getDisabledLocaleList();

        assertThat(enabledLocaleList)
                .doesNotContain(language)
                .as("The language is disabled by default");
        assertThat(disabledLocaleList)
                .contains(language)
                .as("The language is disabled by default");
    }

    @Feature(summary = "The administrator can add a known language to Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181709)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addKnownLanguage() throws Exception {
        String language = "ru-RU";
        LanguagesPage languagesPage = new BasicWorkFlow()
                .goToHome()
                .goToLanguages();

        assertThat(languagesPage.getLanguageLocales())
                .doesNotContain(language)
                .as("The language is not listed");

        AddLanguagePage addLanguagePage = languagesPage
                .clickMoreActions()
                .addNewLanguage()
                .enterSearchLanguage("ru-RU")
                .selectSearchLanguage("ru-RU");

        Map<String, String> languageInfo = addLanguagePage.getLanguageDetails();

        assertThat(languageInfo.get("Name"))
                .isEqualTo("Russian (Russia)")
                .as("The name is correct");
        assertThat(languageInfo.get("Native Name"))
                .isEqualTo("русский (Россия)")
                .as("The native name is correct");
        assertThat(languageInfo.get("Language Code"))
                .isEqualTo("ru")
                .as("The language is correct");
        assertThat(languageInfo.get("Country Code"))
                .isEqualTo("RU")
                .as("The country code is correct");
    }

}
