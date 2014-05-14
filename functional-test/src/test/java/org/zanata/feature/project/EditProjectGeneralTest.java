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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.projectsettings.ProjectGeneralTab;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectGeneralTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void setAProjectToReadOnly() {
        ProjectsPage projectsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject()
                .goToProjects()
                .setActiveFilterEnabled(true)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility("about fedora", false);

        assertThat("The project is not displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                not(hasItem("about fedora")));

        projectsPage = projectsPage.setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(true)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility("about fedora", true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                hasItem("about fedora"));
    }

    @Test
    public void setAProjectToWritable() {
        assertThat("The project is locked", new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .lockProject()
                .goToProjects()
                .setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(true)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility("about fedora", true)
                .getProjectNamesOnCurrentPage(),
                hasItem("about fedora"));

        ProjectsPage projectsPage = new BasicWorkFlow()
                .goToHome()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .unlockProject()
                .goToProjects()
                .setActiveFilterEnabled(true)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(false)
                .waitForProjectVisibility("about fedora", true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                hasItem("about fedora"));
    }

    @Test
    public void setAProjectObsolete() {
        ProjectsPage projectsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .archiveProject()
                .goToProjects();

        assertThat("The project is not displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                not(hasItem("about fedora")));

        projectsPage = projectsPage.setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(true);

        projectsPage.waitForProjectVisibility("about fedora", true);

        assertThat("The project is now displayed",
                projectsPage.getProjectNamesOnCurrentPage(),
                hasItem("about fedora"));

        projectsPage.logout();

        assertThat("User cannot navigate to a project", new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToProjects()
                .getProjectNamesOnCurrentPage(),
                not(hasItem("about fedora")));
    }

    @Test
    public void setAnObsoleteProjectAsActive() {
        ProjectGeneralTab projectGeneralTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .archiveProject()
                .goToProjects()
                .setObsoleteFilterEnabled(true)
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .unarchiveProject();

        assertThat("The archive button is now available",
                projectGeneralTab.isArchiveButtonAvailable());

        projectGeneralTab.logout();

        assertThat("Translator can view the project", new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToProjects()
                .goToProject("about fedora")
                .getProjectName(),
                equalTo("about fedora"));

    }

    @Test
    public void changeProjectName() {
        String replacementText = "a new name";
        ProjectVersionsPage projectVersionsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
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
        ProjectVersionsPage projectVersionsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora");

        assertThat("The description is default",
                projectVersionsPage.getContentAreaParagraphs(),
                not(hasItem(replacementText)));

        ProjectGeneralTab projectGeneralTab = projectVersionsPage
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterDescription(replacementText)
                .updateProject();

        assertThat("The text has changed",
                projectGeneralTab.getContentAreaParagraphs(),
                Matchers.hasItem(replacementText));
    }

    @Test
    public void changeProjectType() {
        ProjectGeneralTab projectGeneralTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .selectProjectType("Properties")
                .updateProject()
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral();

        assertThat("The type is correct",
                projectGeneralTab.getSelectedProjectType(),
                Matchers.equalTo("Properties"));
    }

    @Test
    public void changeSourceLinks() {
        ProjectVersionsPage projectVersionsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .enterHomePage("http://www.example.com")
                .enterRepository("http://www.test.com")
                .updateProject()
                .goToProjects()
                .goToProject("about fedora");

        assertThat("The homepage is correct",
                projectVersionsPage.getHomepage(),
                Matchers.equalTo("http://www.example.com"));

        assertThat("The git url is correct",
                projectVersionsPage.getGitUrl(),
                Matchers.equalTo("http://www.test.com"));
    }
}
