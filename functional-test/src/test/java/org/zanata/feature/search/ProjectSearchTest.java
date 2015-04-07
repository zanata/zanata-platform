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

package org.zanata.feature.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.BasePage;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProjectSearchTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Feature(summary = "The user can search for a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void successfulProjectSearchAndDisplay() throws Exception {
        BasePage basePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("about")
                .waitForSearchListContains("about fedora");

        assertThat(basePage.getZanataSearchAutocompleteItems())
                .contains("about fedora")
                .as("Normal user can see the project");

        ProjectBasePage projectPage =
                basePage.clickProjectSearchEntry("about fedora");

        assertThat(projectPage.getProjectName().trim())
                .isEqualTo("about fedora")
                .as("The project page is the correct one");
    }

    @Feature(summary = "The system will provide no results on an " +
            "unsuccessful search",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void unsuccessfulProjectSearch() throws Exception {
        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("arodef")
                .waitForSearchListContains("Search Zanata for 'arodef'")
                .submitSearch();

        assertThat(projectsPage.getProjectNamesOnCurrentPage().isEmpty())
                .isTrue()
                .as("No projects are displayed");
    }

    @Feature(summary = "The user cannot search for Archived projects",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void normalUserCannotSearchArchived() throws Exception {
        new LoginWorkFlow().signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .archiveProject()
                .logout();

        BasePage basePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("about")
                .waitForSearchListContains("Search Zanata for 'about'");

        assertThat(basePage.getZanataSearchAutocompleteItems())
                .doesNotContain("About Fedora")
                .as("User cannot see the archived project");
    }

}
