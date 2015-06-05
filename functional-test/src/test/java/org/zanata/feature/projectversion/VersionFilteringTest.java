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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class VersionFilteringTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private ZanataRestCaller zanataRestCaller;

    @Feature(summary = "The user can filter project versions by name",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Ignore("dodgy test (intermittent timeout)")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void versionSearchFiltering() throws Exception {
        String projectName = "versionsearchnums";
        zanataRestCaller = new ZanataRestCaller("translator",
                PropertiesHolder
                        .getProperty(Constants.zanataTranslatorKey.value()));
        zanataRestCaller.createProjectAndVersion(projectName, "alpha", "file");
        zanataRestCaller.createProjectAndVersion(projectName, "bravo", "file");

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .isEqualTo("translator")
                .as("Login as translator");

        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .goToProjectByName(projectName)
                .expectDisplayedVersions(2);

        assertVersions(projectVersionsPage, 2, new String[]{"bravo", "alpha"});

        projectVersionsPage = projectVersionsPage
                .clickSearchIcon()
                .enterVersionSearch("alpha")
                .expectDisplayedVersions(1);

        assertVersions(projectVersionsPage, 1, new String[]{"alpha"});

        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .expectDisplayedVersions(2);

        assertVersions(projectVersionsPage, 2, new String[]{"bravo", "alpha"});

        projectVersionsPage = projectVersionsPage
                .enterVersionSearch("bravo")
                .expectDisplayedVersions(1);

        assertVersions(projectVersionsPage, 1, new String[]{"bravo"});

        projectVersionsPage.waitForPageSilence();
        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .enterVersionSearch("charlie")
                .expectDisplayedVersions(0);

        assertVersions(projectVersionsPage, 0, new String[]{});

        projectVersionsPage.waitForPageSilence();
        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .expectDisplayedVersions(2);

        assertVersions(projectVersionsPage, 2, new String[]{"bravo", "alpha"});
    }

    private void assertVersions(ProjectVersionsPage page,
                                int versionsCount,
                                String[] versionNames) {
        assertThat(page.getNumberOfDisplayedVersions())
                .isEqualTo(versionsCount)
                .as("The version count is " + versionsCount);

        assertThat(page.getVersions())
                .contains(versionNames)
                .as("The versions are correct");
    }
}
