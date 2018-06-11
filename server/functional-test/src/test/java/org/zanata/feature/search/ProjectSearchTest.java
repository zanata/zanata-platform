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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.explore.ExplorePage;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProjectSearchTest extends ZanataTestCase {

    @Trace(summary = "The user can search for a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void successfulProjectSearchAndDisplay() throws Exception {
        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("about")
                .expectProjectListContains("about fedora");

        assertThat(explorePage.getProjectSearchResults())
                .contains("about fedora")
                .as("Normal user can see the project");

        ProjectBasePage projectPage =
            explorePage.clickProjectEntry("about fedora");

        assertThat(projectPage.getProjectName().trim())
                .isEqualTo("about fedora")
                .as("The project page is the correct one");
    }

    @Trace(summary = "The system will provide no results on an " +
            "unsuccessful search")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void unsuccessfulProjectSearch() throws Exception {
        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("arodef");

        assertThat(explorePage.getProjectSearchResults().isEmpty())
                .isTrue()
                .as("No projects are displayed");
    }

    @Trace(summary = "The user cannot search for Deleted projects")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void userCannotSearchDeleteProject() throws Exception {
        new LoginWorkFlow().signIn("admin", "admin")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .deleteProject()
                .enterProjectNameToConfirmDelete("about fedora")
                .confirmDeleteProject()
                .logout();

        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("about");

        assertThat(explorePage.getProjectSearchResults().isEmpty())
            .isTrue()
            .as("No projects are displayed");
    }

}
