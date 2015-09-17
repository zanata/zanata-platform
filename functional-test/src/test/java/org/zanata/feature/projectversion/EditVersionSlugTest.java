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
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.versionsettings.VersionGeneralTab;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * /**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditVersionSlugTest extends ZanataTestCase {

    private ZanataRestCaller zanataRestCaller;

    @Before
    public void before() {
        zanataRestCaller = new ZanataRestCaller();
        zanataRestCaller.createProjectAndVersion("change-version-slug",
                "oldSlug", "file");
    }

    @Feature(summary = "Project version slug can be changed and page will redirect to new URL",
            tcmsTestPlanIds = 0, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeVersionSlug() throws Exception {
        assertThat(new LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs())
                .isEqualTo("admin")
                .as("Admin user has logged in");

        VersionGeneralTab versionGeneralTab = new ProjectWorkFlow()
                .goToProjectByName("change-version-slug")
                .gotoVersion("oldSlug")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterVersionID("newSlug")
                .updateVersion();

        versionGeneralTab.reload();
        assertThat(versionGeneralTab.getUrl()).contains("/newSlug");

        versionGeneralTab = versionGeneralTab
                .gotoSettingsTab()
                .gotoSettingsGeneral();
        assertThat(versionGeneralTab.getVersionID())
                .isEqualTo("newSlug")
                .as("The version slug has been changed");
    }
}
