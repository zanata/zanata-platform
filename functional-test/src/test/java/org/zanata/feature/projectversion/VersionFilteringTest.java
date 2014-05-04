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

import org.junit.ClassRule;
import org.junit.Test;
import org.zanata.feature.ZanataTestCase;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class VersionFilteringTest extends ZanataTestCase {

    @ClassRule
    public static SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void versionSearchFiltering() {

        String projectName = "versionsearchnums";

        assertThat("Login as translator",
                new LoginWorkFlow().signIn("translator", "translator")
                        .loggedInAs(),
                equalTo("translator"));

        assertThat("The project is created",
                new ProjectWorkFlow()
                        .createNewSimpleProject("version-search", projectName)
                        .getProjectName(),
                equalTo(projectName));

        assertThat("The version alpha is created",
                new ProjectWorkFlow()
                        .createNewProjectVersion(projectName, "alpha")
                        .getProjectVersionName(),
                equalTo("alpha"));

        assertThat("The version bravo is created",
                new ProjectWorkFlow()
                        .createNewProjectVersion(projectName, "bravo")
                        .getProjectVersionName(),
                equalTo("bravo"));

        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .goToProjectByName(projectName)
                .waitForDisplayedVersions(2);

        assertThat("The version count is 2",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(2));

        assertThat("The versions are correct",
                projectVersionsPage.getVersions(),
                contains("bravo", "alpha"));

        projectVersionsPage = projectVersionsPage
                .clickSearchIcon()
                .enterVersionSearch("alpha")
                .waitForDisplayedVersions(1);

        assertThat("The version count is 1",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(1));

        assertThat("The versions are correct",
                projectVersionsPage.getVersions(),
                contains("alpha"));

        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .waitForDisplayedVersions(2);

        assertThat("The version count is 2 again",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(2));

        assertThat("The versions are correct",
                projectVersionsPage.getVersions(),
                contains("bravo", "alpha"));

        projectVersionsPage = projectVersionsPage
                .enterVersionSearch("bravo")
                .waitForDisplayedVersions(1);

        assertThat("The version count is 1",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(1));

        assertThat("The versions are correct",
                projectVersionsPage.getVersions(),
                contains("bravo"));

        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .enterVersionSearch("charlie")
                .waitForDisplayedVersions(0);

        assertThat("The version count is 0",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(0));

        assertThat("The versions are correct",
                projectVersionsPage.getVersions().size(),
                equalTo(0));

        projectVersionsPage = projectVersionsPage
                .clearVersionSearch()
                .waitForDisplayedVersions(2);

        assertThat("The version count is 2 again",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(2));

        assertThat("The versions are correct",
                projectVersionsPage.getVersions(),
                contains("bravo", "alpha"));
    }
}
