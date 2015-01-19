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
import org.zanata.page.administration.ManageLanguagePage;
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
    public void addLanguageAsEnabled() throws Exception {
        String language = "Goa'uld";
        String languageDisplayName = "goa'uld[Goa'uld]";
        ManageLanguagePage manageLanguagePage = new BasicWorkFlow()
                .goToHome()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat(manageLanguagePage.getLanguageLocales())
                .doesNotContain(language)
                .as("The language is not listed");

        manageLanguagePage = manageLanguagePage
                .clickMoreActions()
                .addNewLanguage()
                .enterSearchLanguage(language)
                .waitForPluralsWarning()
                .saveLanguage();

        assertThat(manageLanguagePage.getLanguageLocales())
                .contains(language)
                .as("The language is listed");

        assertThat(manageLanguagePage.languageIsEnabled(language))
                .isTrue()
                .as("The language is enabled by default");

        List<String> enabledLocaleList = manageLanguagePage
                .goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .waitForLocaleListVisible()
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .contains(languageDisplayName)
                .as("The language is enabled by default");
    }

    @Feature(summary = "The administrator can add a disabled language to Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181709)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addLanguageAsDisabled() throws Exception {
        String language = "Klingon";
        String languageDisplayName = "klingon[Klingon]";
        ManageLanguagePage manageLanguagePage = new BasicWorkFlow()
                .goToHome()
                .goToHomePage()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat(manageLanguagePage.getLanguageLocales())
                .doesNotContain(language)
                .as("The language is not listed");

        manageLanguagePage = manageLanguagePage
                .clickMoreActions()
                .addNewLanguage()
                .enterSearchLanguage(language)
                .waitForPluralsWarning()
                .disableLanguageByDefault()
                .saveLanguage();

        assertThat(manageLanguagePage.getLanguageLocales())
                .contains(language)
                .as("The language is listed");
        assertThat(manageLanguagePage.languageIsEnabled(language))
                .isFalse()
                .as("The language is disabled by default");

        List<String> enabledLocaleList = manageLanguagePage.goToHomePage()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .waitForLocaleListVisible()
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .doesNotContain(languageDisplayName)
                .as("The language is disabled by default");
    }

    @Feature(summary = "The administrator can add a known language to Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181709)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addKnownLanguage() throws Exception {
        String language = "ru-RU";
        ManageLanguagePage manageLanguagePage = new BasicWorkFlow()
                .goToHome()
                .goToAdministration()
                .goToManageLanguagePage();

        assertThat(manageLanguagePage.getLanguageLocales())
                .doesNotContain(language)
                .as("The language is not listed");

        AddLanguagePage addLanguagePage = manageLanguagePage
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
