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
package org.zanata.feature.project;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.explore.ExplorePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.projectsettings.ProjectGeneralTab;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectGeneralTest extends ZanataTestCase {

    @Before
    public void before() {
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .as("Admin is logged in")
                .isEqualTo("admin");
    }


    @Ignore("Duplicate test with setAProjectToWritable")
    @Trace(summary = "The administrator can set a project to read-only")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void setAProjectToReadOnly() throws Exception {
        ExplorePage explorePage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject()
                .gotoExplore()
                .enterSearch("about fedora")
                .expectProjectListContains("about fedora")
                .logout()
                .gotoExplore()
                .enterSearch("about fedora");

        assertThat(explorePage.getProjectSearchResults())
                .as("The project is not displayed")
                .doesNotContain("about fedora");
    }

    @Trace(summary = "The administrator can set a read-only project " +
            "to writable")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void setAProjectToWritable() throws Exception {
        new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject()
                .gotoExplore()
                .enterSearch("about fedora")
                .expectProjectListContains("about fedora");
        // TODO check whether "about fedora" in the list has a lock icon (locked project)

        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .unlockProject()
                .gotoExplore()
                .enterSearch("about fedora")
                .expectProjectListContains("about fedora");

        assertThat(explorePage.getProjectSearchResults())
                .as("The project is now displayed")
                .contains("about fedora");
    }

    @Trace(summary = "The administrator can change a project's name")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeProjectName() throws Exception {
        String replacementText = "a new name";
        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterProjectName(replacementText)
                .updateProject()
                .gotoExplore()
                .searchAndGotoProjectByName(replacementText);

        assertThat(projectVersionsPage.getProjectName())
                .as("The project name has changed")
                .isEqualTo(replacementText);
    }

    @Trace(summary = "The administrator can change a project's description")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeProjectDescription() throws Exception {
        String replacementText = "a new description";
        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora");

        assertThat(projectVersionsPage.getContentAreaParagraphs())
                .as("The description is default")
                .doesNotContain(replacementText);

        ProjectGeneralTab projectGeneralTab = projectVersionsPage
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterDescription(replacementText)
                .updateProject();

        assertThat(projectGeneralTab.getContentAreaParagraphs())
                .as("The text has changed")
                .contains(replacementText);
    }

    @Trace(summary = "The administrator can change a project's type")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeProjectType() throws Exception {
        ProjectGeneralTab projectGeneralTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .selectProjectType("Properties")
                .updateProject();

        projectGeneralTab.reload();
        projectGeneralTab = projectGeneralTab
                .gotoSettingsTab()
                .gotoSettingsGeneral();

        assertThat(projectGeneralTab.getSelectedProjectType())
                .as("The project type is correct")
                .isEqualTo("Properties");
    }

    @Trace(summary = "The administrator can change a project's source urls")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void changeSourceLinks() throws Exception {
        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterHomePage("http://www.example.com")
                .enterRepository("http://git.example.com")
                .updateProject()
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora");

        assertThat(projectVersionsPage.getHomepage())
                .as("The homepage is correct")
                .isEqualTo("http://www.example.com");

        assertThat(projectVersionsPage.getGitUrl())
                .as("The git url is correct")
                .isEqualTo("http://git.example.com");
    }

    @Trace(summary = "Project slug can be changed and page will redirect to new URL after the change")
    @Test
    public void changeProjectSlug() {
        ProjectGeneralTab projectGeneralTab = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterProjectSlug("fedora-reborn")
                .updateProject();

        projectGeneralTab.reload();
        assertThat(projectGeneralTab.getUrl()).contains("/fedora-reborn");
        projectGeneralTab = projectGeneralTab
                .gotoSettingsTab()
                .gotoSettingsGeneral();

        assertThat(projectGeneralTab.getProjectId())
                .as("The project slug is correct")
                .isEqualTo("fedora-reborn");
        // FIXME wait for async indexing to finish to avoid interfering with other tests

        // TODO test that search results work for new slug
    }
}
