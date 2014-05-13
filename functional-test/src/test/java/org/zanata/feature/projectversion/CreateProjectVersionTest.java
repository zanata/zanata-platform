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

package org.zanata.feature.projectversion;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.CreateVersionPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class CreateProjectVersionTest extends ZanataTestCase {

    @ClassRule
    public static SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    @Category(BasicAcceptanceTest.class)
    public void createASimpleProjectVersion() {
        VersionLanguagesPage versionLanguagesPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("my-aboutfedora-version")
                .saveVersion();

        assertThat("The version is created with correct ID",
                versionLanguagesPage.getProjectVersionName(),
                equalTo("my-aboutfedora-version"));
    }

    @Test
    public void idFieldMustNotBeEmpty() {
        CreateVersionPage createVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").clickCreateVersionLink()
                        .inputVersionId("");
        createVersionPage.defocus();

        assertThat("The empty value is rejected",
                createVersionPage.getErrors(),
                hasItem("value is required"));
    }

    @Test
    public void idStartsAndEndsWithAlphanumeric() {
        CreateVersionPage createVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").clickCreateVersionLink()
                        .inputVersionId("-A");
        createVersionPage.defocus();
        String formatError =
                "must start and end with letter or number, "
                        + "and contain only letters, numbers, underscores and hyphens.";

        assertThat("The input is rejected", createVersionPage.getErrors(),
                hasItem(formatError));

        createVersionPage =
                createVersionPage.inputVersionId("B-").waitForNumErrors(1);
        createVersionPage.defocus();

        assertThat("The input is rejected", createVersionPage.getErrors(),
                hasItem(formatError));

        createVersionPage =
                createVersionPage.inputVersionId("_C_").waitForNumErrors(1);
        createVersionPage.defocus();

        assertThat("The input is rejected", createVersionPage.getErrors(),
                hasItem(formatError));

        createVersionPage =
                createVersionPage.inputVersionId("A-B_C").waitForNumErrors(0);
        createVersionPage.defocus();

        assertThat("The input is acceptable", createVersionPage.getErrors(),
                not(hasItem(formatError)));
    }

    @Test
    public void versionCounterIsUpdated() {

        String projectName = "version nums";

        assertThat("Login as translator",
                new LoginWorkFlow().signIn("translator", "translator")
                        .loggedInAs(),
                equalTo("translator"));

        assertThat("The project is created",
                new ProjectWorkFlow()
                        .createNewSimpleProject("version-nums", projectName)
                        .getProjectName(),
                equalTo(projectName));

        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .createNewProjectVersion(projectName, "alpha")
                .clickProjectLink(projectName);
        projectVersionsPage.waitForDisplayedVersions(1);

        assertThat("The version count is 1",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(1));

        projectVersionsPage = new ProjectWorkFlow()
                .createNewProjectVersion("version nums", "bravo")
                .clickProjectLink(projectName);
        projectVersionsPage.waitForDisplayedVersions(2);

        assertThat("The version count is 2",
                projectVersionsPage.getNumberOfDisplayedVersions(),
                equalTo(2));
    }
}
