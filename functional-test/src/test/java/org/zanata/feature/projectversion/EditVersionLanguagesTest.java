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

package org.zanata.feature.projectversion;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projectversion.versionsettings.VersionLanguagesTab;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditVersionLanguagesTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void changeVersionLanguages() {

        assertThat("Admin user has logged in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                equalTo("admin"));

        VersionLanguagesTab versionLanguagesTab = new ProjectWorkFlow()
                .createNewSimpleProject("langoverride", "langoverride")
                .clickCreateVersionLink()
                .inputVersionId("overridelangtest")
                .saveVersion()
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .clickInheritCheckbox()
                .waitForLocaleListVisible();

        List<String> enabledLocaleList = versionLanguagesTab
                .getEnabledLocaleList();

        assertThat("The enabled list contains three languages",
                enabledLocaleList,
                contains("French[fr]", "Hindi[hi]", "Polish[pl]"));

        assertThat("The enabled list does not contain " +
                "'English (United States)[en-US]'",
                enabledLocaleList,
                not(hasItem("English (United States)[en-US]")));

        versionLanguagesTab = versionLanguagesTab.removeLocale("pl");
        versionLanguagesTab.waitForLanguagesNotContains(
                "English (United States)[en-US]");
        versionLanguagesTab.waitForLanguagesNotContains("Polish[pl]");
        enabledLocaleList = versionLanguagesTab.getEnabledLocaleList();

        assertThat("The enabled list does not contain 'US English'",
                enabledLocaleList,
                not(hasItem("English (United States)[en-US]")));

        assertThat("The enabled list does not contain 'Polish'",
                enabledLocaleList,
                not(hasItem("Polish[pl]")));

        versionLanguagesTab = versionLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .enterSearchLanguage("en-US")
                .addLocale("English (United States)[en-US]");

        versionLanguagesTab.waitForLanguagesContains(
                "English (United States)[en-US]");
        enabledLocaleList = versionLanguagesTab.getEnabledLocaleList();

        assertThat("Three languages are available to translate",
                enabledLocaleList,
                contains("English (United States)[en-US]",
                        "French[fr]",
                        "Hindi[hi]"));
    }
}
