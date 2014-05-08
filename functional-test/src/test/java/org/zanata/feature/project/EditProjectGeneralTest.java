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

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.projectsettings.ProjectGeneralTab;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.zanata.workflow.ProjectWorkFlow.projectDefaults;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectGeneralTest extends ZanataTestCase {

    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    @Test
    public void setAProjectToReadOnly() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "deactivateproject");
        projectSettings.put("Name", "Deactivate Project Test");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                equalTo("admin"));

        new ProjectWorkFlow()
                .createNewProject(projectSettings)
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject();

        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .goToProjects()
                .setActiveFilterEnabled(true)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility(projectSettings.get("Name"), false);

        assertThat("The project is not displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                not(hasItem(projectSettings.get("Name"))));

        projectsPage = projectsPage.setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(true)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility(projectSettings.get("Name"), true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                hasItem(projectSettings.get("Name")));
    }

    @Test
    public void setAProjectToWritable() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "rewritableproject");
        projectSettings.put("Name", "Rewrite Project Test");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                equalTo("admin"));

        assertThat("The project is locked",
                new ProjectWorkFlow()
                        .createNewProject(projectSettings)
                        .gotoSettingsTab()
                        .gotoSettingsGeneral()
                        .lockProject()
                        .goToProjects()
                        .setActiveFilterEnabled(false)
                        .setReadOnlyFilterEnabled(true)
                        .setObsoleteFilterEnabled(false)
                        .waitForProjectVisibility(
                                projectSettings.get("Name"), true)
                        .getProjectNamesOnCurrentPage(),
                hasItem(projectSettings.get("Name")));

        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .goToProjects()
                .goToProject(projectSettings.get("Name"))
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .unlockProject()
                .goToProjects()
                .setActiveFilterEnabled(true)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility(projectSettings.get("Name"), true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                hasItem(projectSettings.get("Name")));
    }

    @Test
    public void setAProjectObsolete() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "setobsoleteproject");
        projectSettings.put("Name", "Obsolete Project Test");
        projectSettings.put("Project Type", "File");

        assertThat("Translator can log in",
                new LoginWorkFlow()
                        .signIn("translator", "translator")
                        .loggedInAs(),
                equalTo("translator"));

        assertThat("Archiving is not available to non admin",
                new ProjectWorkFlow()
                        .createNewProject(projectSettings)
                        .gotoSettingsTab()
                        .gotoSettingsGeneral()
                        .isArchiveButtonAvailable(),
                not(true));

        new BasicWorkFlow().goToHome().logout();

        ProjectsPage projectsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject(projectSettings.get("Name"))
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .archiveProject()
                .goToProjects();

        assertThat("The project is not displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                not(hasItem(projectSettings.get("Name"))));

        projectsPage = projectsPage.setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(true);

        projectsPage
                .waitForProjectVisibility(projectSettings.get("Name"), true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                hasItem(projectSettings.get("Name")));

        projectsPage.logout();

        assertThat("User cannot navigate to a project",
                new LoginWorkFlow()
                        .signIn("translator", "translator")
                        .goToProjects()
                        .getProjectNamesOnCurrentPage(),
                not(hasItem(projectSettings.get("Name"))));
    }

    @Test
    public void setAnObsoleteProjectAsActive() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "setobsoleteprojectactive");
        projectSettings.put("Name", "Unobsolete Project Test");
        projectSettings.put("Project Type", "File");

        assertThat("Translator can log in",
                new LoginWorkFlow()
                        .signIn("translator", "translator")
                        .loggedInAs(),
                equalTo("translator"));

        new ProjectWorkFlow()
                .createNewProject(projectSettings)
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .logout();

        ProjectGeneralTab projectGeneralTab =
                new LoginWorkFlow()
                    .signIn("admin", "admin")
                    .goToProjects()
                    .goToProject(projectSettings.get("Name"))
                    .gotoSettingsTab()
                    .gotoSettingsGeneral()
                    .archiveProject()
                    .goToProjects()
                    .setObsoleteFilterEnabled(true)
                    .goToProject(projectSettings.get("Name"))
                    .gotoSettingsTab()
                    .gotoSettingsGeneral()
                    .unarchiveProject();

        assertThat("The archive button is now available",
                projectGeneralTab.isArchiveButtonAvailable());

        assertThat("Translator can view the project",
                new LoginWorkFlow()
                    .signIn("translator", "translator")
                    .goToProjects()
                    .goToProject(projectSettings.get("Name"))
                    .getProjectName(),
                equalTo(projectSettings.get("Name")));

    }

    @Test
    public void changeProjectName() {

        String replacementText = "a new name";
        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "changeName");
        projectSettings.put("Name", "Project Name Change Test");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));

        ProjectVersionsPage projectVersionsPage =
                new ProjectWorkFlow()
                .createNewProject(projectSettings)
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterProjectName(replacementText)
                .updateProject()
                .goToProjects()
                .goToProject(replacementText);

        assertThat("The name has changed",
                projectVersionsPage.getProjectName(),
                Matchers.equalTo(replacementText));
    }

    @Test
    public void changeProjectDescription() {

        String replacementText = "a new description";
        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "changeDescription");
        projectSettings.put("Name", "Project Description Test");
        projectSettings.put("Description", "An old description");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));

        ProjectVersionsPage projectVersionsPage =
                new ProjectWorkFlow()
                .createNewProject(projectSettings)
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterDescription(replacementText)
                .updateProject()
                .goToProjects()
                .goToProject(projectSettings.get("Name"));

        assertThat("The text has changed",
                projectVersionsPage.getContentAreaParagraphs(),
                Matchers.hasItem(replacementText));
    }

    @Test
    public void changeProjectType() {
        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "changeType");
        projectSettings.put("Name", "Project Type Test");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));

        ProjectGeneralTab projectGeneralTab =
                new ProjectWorkFlow()
                .createNewProject(projectSettings)
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .selectProjectType("Properties")
                .updateProject()
                .goToProjects()
                .goToProject(projectSettings.get("Name"))
                .gotoSettingsTab()
                .gotoSettingsGeneral();

        assertThat("The type is correct",
                projectGeneralTab.getSelectedProjectType(),
                Matchers.equalTo("Properties"));
    }

    @Test
    public void changeSourceLinks() {
        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "changeLinks");
        projectSettings.put("Name", "Project Links Test");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                Matchers.equalTo("admin"));

        ProjectVersionsPage projectVersionsPage =
                new ProjectWorkFlow()
                .createNewProject(projectSettings)
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterHomePage("http://www.example.com")
                .enterRepository("http://www.test.com")
                .updateProject()
                .goToProjects()
                .goToProject(projectSettings.get("Name"));

        assertThat("The homepage is correct",
                projectVersionsPage.getHomepage(),
                Matchers.equalTo("http://www.example.com"));

        assertThat("The git url is correct",
                projectVersionsPage.getGitUrl(),
                Matchers.equalTo("http://www.test.com"));
    }
}
