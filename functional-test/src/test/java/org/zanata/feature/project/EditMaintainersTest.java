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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.ProjectMaintainersPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.junit.Assume.assumeTrue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *      href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditMaintainersTest {

    @Rule
    public ResetDatabaseRule resetDatabaseRule =
            new ResetDatabaseRule(ResetDatabaseRule.Config.WithData);

    @Test
    public void maintainerDetailsAreDisplayed() {
        ProjectMaintainersPage projectMaintainersPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickManageMaintainers();

        assertThat("The admin user is shown in the list",
                projectMaintainersPage.maintainersList(),
                Matchers.hasItem("admin"));
        assertThat("The admin user's name is correct",
                projectMaintainersPage.getUserFullName("admin"),
                Matchers.equalTo("Administrator"));
        assertThat("The admin user's email is correct",
                projectMaintainersPage.getUserEmailAddress("admin"),
                Matchers.equalTo("admin@example.com"));
    }

    @Test
    public void addMaintainer() {
        ProjectMaintainersPage projectMaintainersPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickManageMaintainers();

        assertThat("The translator user is not a maintainer",
                projectMaintainersPage.maintainersList(),
                Matchers.not(Matchers.hasItem("translator")));

        projectMaintainersPage = projectMaintainersPage
                .clickAddMaintainer()
                .enterName("translator")
                .clickAddAutoComplete("translator");

        assertThat("The user's username shows",
                projectMaintainersPage.getAddMaintainerUserName(),
                Matchers.equalTo("translator"));
        assertThat("The user's email shows",
                projectMaintainersPage.getAddMaintainerEmail(),
                Matchers.equalTo("translator@example.com"));

        projectMaintainersPage = projectMaintainersPage.clickAdd();
        // RHBZ-1022760
        assumeTrue(projectMaintainersPage.hasNoCriticalErrors());

        projectMaintainersPage = projectMaintainersPage.clickClose();
        // RHBZ-1022760
        assumeTrue(projectMaintainersPage.hasNoCriticalErrors());

        assertThat("The translator user is now a maintainer",
                projectMaintainersPage.maintainersList(),
                Matchers.hasItem("translator"));
    }

    @Test
    public void removeMaintainer() {
        // Precondition
        ProjectMaintainersPage projectMaintainersPage =
                addTranslatorAsMaintainer();

        assertThat("The translator user is a maintainer",
                projectMaintainersPage.maintainersList(),
                Matchers.hasItem("translator"));

        projectMaintainersPage =
                projectMaintainersPage.removeMaintainer("translator");

        assertThat("The translator user is no longer a maintainer",
                projectMaintainersPage.maintainersList(),
                Matchers.not(Matchers.hasItem("translator")));
    }

    @Test
    public void dontRemoveMaintainer() {
        // Precondition
        ProjectMaintainersPage projectMaintainersPage =
                addTranslatorAsMaintainer();

        assertThat("The translator user is a maintainer",
                projectMaintainersPage.maintainersList(),
                Matchers.hasItem("translator"));

        projectMaintainersPage =
                projectMaintainersPage.cancelRemoveMaintainer("translator");

        assertThat("The translator user is still a maintainer",
                projectMaintainersPage.maintainersList(),
                Matchers.hasItem("translator"));
    }

    private ProjectMaintainersPage addTranslatorAsMaintainer() {
        ProjectMaintainersPage projectMaintainersPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickManageMaintainers()
                .clickAddMaintainer()
                .enterName("translator")
                .clickAddAutoComplete("translator");
        projectMaintainersPage = projectMaintainersPage.clickAdd();
        // RHBZ-1022760
        assumeTrue(projectMaintainersPage.hasNoCriticalErrors());
        projectMaintainersPage = projectMaintainersPage.clickClose();
        // RHBZ-1022760
        assumeTrue(projectMaintainersPage.hasNoCriticalErrors());
        return projectMaintainersPage;
    }
}
