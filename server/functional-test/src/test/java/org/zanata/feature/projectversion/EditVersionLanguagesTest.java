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

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.versionsettings.VersionLanguagesTab;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditVersionLanguagesTest extends ZanataTestCase {

    private ZanataRestCaller zanataRestCaller;

    @Before
    public void before() {
        zanataRestCaller = new ZanataRestCaller();
        zanataRestCaller.createProjectAndVersion("langoverride",
                "overridelangtest", "file");
    }

    @Trace(summary = "The maintainer can override the available languages " +
            "for a project version")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeVersionLanguages() throws Exception {
        assertThat(new LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .isEqualTo("admin")
                .as("Admin user has logged in");

        VersionLanguagesTab versionLanguagesTab = new ProjectWorkFlow()
                .goToProjectByName("langoverride")
                .gotoVersion("overridelangtest")
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .expectLocaleListVisible();

        List<String> enabledLocaleList = versionLanguagesTab
                .getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .contains("fr", "hi", "pl")
                .as("The enabled list contains no languages");

        versionLanguagesTab = versionLanguagesTab
                .gotoSettingsTab()
                .gotoSettingsLanguagesTab()
                .filterDisabledLanguages("nonexistentLocale")
                .expectAvailableLocaleListCount(0)
                .filterDisabledLanguages("en-US")
                .expectAvailableLocaleListCount(1)
                .addLocale("en-US");
        versionLanguagesTab.expectNotification("Language \"en-US\" has been " +
                "enabled.");
        versionLanguagesTab = versionLanguagesTab
                .expectLanguagesContains("en-US");

        enabledLocaleList = versionLanguagesTab.getEnabledLocaleList();

        assertThat(enabledLocaleList)
                .contains("en-US", "fr", "hi", "pl")
                .as("The languages are available to translate");
        versionLanguagesTab.filterEnabledLanguages("en-US")
                .expectEnabledLocaleListCount(1);
    }
}
