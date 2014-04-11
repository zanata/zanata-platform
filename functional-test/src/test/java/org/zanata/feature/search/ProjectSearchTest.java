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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.BasePage;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProjectSearchTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void successfulProjectSearchAndDisplay() {
        BasePage basePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("about")
                .waitForSearchListContains("about fedora");

        assertThat("Normal user can see the project",
                basePage.getProjectSearchAutocompleteItems(),
                hasItem("about fedora"));

        ProjectBasePage projectPage =
                basePage.clickSearchEntry("about fedora");

        assertThat("The project page is the correct one", projectPage
                .getProjectName().trim(), // UI adds a space
                equalTo("about fedora"));
    }

    @Test
    public void unsuccessfulProjectSearch() {
        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("arodef")
                .waitForSearchListContains("Search Zanata for 'arodef'")
                .submitSearch();

        assertThat("No projects are displayed", projectsPage
                .getProjectNamesOnCurrentPage().isEmpty());
    }

    @Test
    public void normalUserCannotSearchObsolete() {
        new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                .goToProject("about fedora").gotoSettingsTab()
                .gotoSettingsGeneral().archiveProject().logout();

        BasePage basePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("about")
                .waitForSearchListContains("Search Zanata for 'about'");

        assertThat("User cannot see the obsolete project",
                basePage.getProjectSearchAutocompleteItems(),
                not(hasItem("About Fedora")));

    }

}
