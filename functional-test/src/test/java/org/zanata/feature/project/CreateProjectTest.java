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

package org.zanata.feature.project;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.workflow.ProjectWorkFlow.projectDefaults;
/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class CreateProjectTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

    @Test
    @Category(BasicAcceptanceTest.class)
    public void createABasicProject() {

        ProjectPage projectPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .clickOnCreateProjectLink()
                .inputProjectId("newprojecttest")
                .inputProjectName("New Project Test")
                .enterDescription("Testing a new project")
                .selectProjectType("File")
                .enterViewSourceURL("https://github.com/zanata/zanata")
                .enterDownloadSourceURL("git@github.com:zanata/zanata.git")
                .selectStatus("ACTIVE")
                .saveProject();

        assertThat("The project id is correct",
                projectPage.getProjectId(),
                Matchers.equalTo("newprojecttest"));

        assertThat("The project name is correct",
                projectPage.getProjectName().trim(),
                Matchers.equalTo("New Project Test"));

        List<String> paragraphs = projectPage.getContentAreaParagraphs();

        assertThat("The project main content area contains the description",
                paragraphs, Matchers.hasItem("Testing a new project"));

        assertThat("The project main content area contains the project link",
                paragraphs, Matchers.hasItem("View source files: " +
                        "https://github.com/zanata/zanata"));

        assertThat("The project main content area contains the source url",
                paragraphs, Matchers.hasItem("Source Download/Checkout: "+
                        "git@github.com:zanata/zanata.git"));
    }

    @Test
    public void createABasicProjectWithHomepageContent() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "homepageproject");
        projectSettings.put("Name", "Project With Homepage Test");
        projectSettings.put("Description", "Don't show this");
        projectSettings.put("Homepage Content", "Homepage content test");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));

        ProjectPage projectPage = new ProjectWorkFlow()
                .createNewProject(projectSettings);

        assertThat("The project id is correct",
                projectPage.getProjectId(),
                Matchers.equalTo(projectSettings.get("Project ID")));

        List<String> paragraphs = projectPage.getContentAreaParagraphs();

        assertThat("The project content area does not contain the description",
                paragraphs, Matchers.not(
                Matchers.hasItem(projectSettings.get("Description"))));

        assertThat("The project content area contains the homepage content",
                paragraphs,
                Matchers.hasItem(projectSettings.get("Homepage Content")));
    }

    @Test
    public void createAnInactiveProject() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "newinactiveproject");
        projectSettings.put("Name", "New Inactive Project Test");
        projectSettings.put("Status", "READONLY");

        assertThat("Admin can log in",
            new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
            Matchers.equalTo("admin"));

        ProjectPage projectPage = new ProjectWorkFlow()
                .createNewProject(projectSettings);

        assertThat("The correct project is shown",
                projectPage.getProjectName().trim(),
                Matchers.equalTo(projectSettings.get("Name")));

        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .goToProjects();

        projectsPage = projectsPage
                .setActiveFilterEnabled(true)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(false);

        projectsPage.waitForProjectVisibility(
                projectSettings.get("Name"), false);

        assertThat("The project is not displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                Matchers.not(Matchers.hasItem(projectSettings.get("Name"))));

        projectsPage = projectsPage
                .setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(true)
                .setObsoleteFilterEnabled(false);

        projectsPage.waitForProjectVisibility(
                projectSettings.get("Name"), true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                Matchers.hasItem(projectSettings.get("Name")));
    }

    @Test
    public void createAnObsoleteProject() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "newobsoleteproject");
        projectSettings.put("Name", "New Obsolete Project Test");
        projectSettings.put("Description", "Test adding an obsolete project");
        projectSettings.put("Status", "OBSOLETE");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));

        ProjectPage projectPage = new ProjectWorkFlow()
                .createNewProject(projectSettings);

        assertThat("The correct project is shown",
                projectPage.getProjectName().trim(),
                Matchers.equalTo(projectSettings.get("Name")));

        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .goToProjects();

        assertThat("The project is not displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                Matchers.not(Matchers.hasItem(projectSettings.get("Name"))));

        projectsPage = projectsPage
                .setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(true);

        projectsPage.waitForProjectVisibility(
                projectSettings.get("Name"), true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                Matchers.hasItem(projectSettings.get("Name")));
    }
}
