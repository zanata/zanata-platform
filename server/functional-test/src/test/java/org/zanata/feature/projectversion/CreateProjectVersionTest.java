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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.CreateVersionPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class CreateProjectVersionTest extends ZanataTestCase {

    @Before
    public void before() {
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
    }

    @Trace(summary = "The administrator can create a project version")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createASimpleProjectVersion() throws Exception {
        VersionLanguagesPage versionLanguagesPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .clickCreateVersionLink()
                .disableCopyFromVersion()
                .inputVersionId("my-aboutfedora-version")
                .saveVersion();

        assertThat(versionLanguagesPage.getProjectVersionName())
                .isEqualTo("my-aboutfedora-version")
                .as("The version is created with correct ID");
    }

    @Trace(summary = "The user must enter an id to create a project version")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void idFieldMustNotBeEmpty() throws Exception {
        CreateVersionPage createVersionPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("");
        createVersionPage.defocus(createVersionPage.projectVersionID);

        List<String> errors = createVersionPage.getErrors();
        assertThat(errors)
                .contains("value is required")
                .as("The empty value is rejected");
    }

    @Trace(summary = "The user must enter an id that starts and ends with " +
            "alphanumeric character to create a project version")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void idStartsAndEndsWithAlphanumeric() throws Exception {
        CreateVersionPage createVersionPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("-A");
        createVersionPage.defocus(createVersionPage.projectVersionID);

        assertThat(createVersionPage.getErrors())
                .contains(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is rejected");

        createVersionPage = createVersionPage.inputVersionId("B-");
        createVersionPage.defocus(createVersionPage.projectVersionID);

        assertThat(createVersionPage.getErrors())
                .contains(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is rejected");

        createVersionPage = createVersionPage.inputVersionId("_C_");
        createVersionPage.defocus(createVersionPage.projectVersionID);
        createVersionPage = createVersionPage.expectNumErrors(1);

        assertThat(createVersionPage.getErrors())
                .contains(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is rejected");

        createVersionPage = createVersionPage.inputVersionId("A-B_C");
        createVersionPage.defocus(createVersionPage.projectVersionID);
        createVersionPage = createVersionPage.expectNumErrors(0);

        assertThat(createVersionPage.getErrors())
                .doesNotContain(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is acceptable");
    }

    @Trace(summary = "The system updates the project version counter " +
            "when a project version is created")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("intermittently failing; see rhbz1168447")
    public void versionCounterIsUpdated() throws Exception {
        String projectName = "version nums";
        assertThat(new ProjectWorkFlow()
                .createNewSimpleProject("version-nums", projectName)
                .getProjectName())
                .isEqualTo(projectName)
                .as("The project is created");

        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .createNewProjectVersion(projectName, "alpha")
                .clickProjectLink(projectName);
        projectVersionsPage.expectDisplayedVersions(1);

        assertThat(projectVersionsPage.getNumberOfDisplayedVersions())
                .isEqualTo(1)
                .as("The version count is 1");

        projectVersionsPage = new ProjectWorkFlow()
                .createNewProjectVersion("version nums", "bravo")
                .clickProjectLink(projectName);
        projectVersionsPage.expectDisplayedVersions(2);

        assertThat(projectVersionsPage.getNumberOfDisplayedVersions())
                .isEqualTo(2)
                .as("The version count is 2");
    }
}
