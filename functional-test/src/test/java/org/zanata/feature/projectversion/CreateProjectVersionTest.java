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
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.CreateVersionPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class CreateProjectVersionTest extends ZanataTestCase {

    @ClassRule
    public static SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Feature(summary = "The administrator can create a project version",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 136517)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void createASimpleProjectVersion() throws Exception {
        VersionLanguagesPage versionLanguagesPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("my-aboutfedora-version")
                .saveVersion();

        assertThat(versionLanguagesPage.getProjectVersionName())
                .isEqualTo("my-aboutfedora-version")
                .as("The version is created with correct ID");
    }

    @Feature(summary = "The user must enter an id to create a project version",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void idFieldMustNotBeEmpty() throws Exception {
        CreateVersionPage createVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("");
        createVersionPage.defocus();

        assertThat(createVersionPage.getErrors())
                .contains("value is required")
                .as("The empty value is rejected");
    }

    @Feature(summary = "The user must enter an id that starts and ends with " +
            "alphanumeric character to create a project version",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void idStartsAndEndsWithAlphanumeric() throws Exception {
        CreateVersionPage createVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("-A");
        createVersionPage.defocus();

        assertThat(createVersionPage.getErrors())
                .contains(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is rejected");

        createVersionPage = createVersionPage.inputVersionId("B-");
        createVersionPage.defocus();
        createVersionPage = createVersionPage.waitForNumErrors(1);

        assertThat(createVersionPage.getErrors())
                .contains(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is rejected");

        createVersionPage = createVersionPage.inputVersionId("_C_");
        createVersionPage.defocus();
        createVersionPage = createVersionPage.waitForNumErrors(1);

        assertThat(createVersionPage.getErrors())
                .contains(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is rejected");

        createVersionPage = createVersionPage.inputVersionId("A-B_C");
        createVersionPage.defocus();
        createVersionPage = createVersionPage.waitForNumErrors(0);

        assertThat(createVersionPage.getErrors())
                .doesNotContain(CreateVersionPage.VALIDATION_ERROR)
                .as("The input is acceptable");
    }

    @Feature(summary = "The system updates the project version counter " +
            "when a project version is created",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void versionCounterIsUpdated() throws Exception {
        String projectName = "version nums";

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .loggedInAs())
                .isEqualTo("translator")
                .as("Login as translator");

        assertThat(new ProjectWorkFlow()
                .createNewSimpleProject("version-nums", projectName)
                .getProjectName())
                .isEqualTo(projectName)
                .as("The project is created");

        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .createNewProjectVersion(projectName, "alpha")
                .clickProjectLink(projectName);
        projectVersionsPage.waitForDisplayedVersions(1);

        assertThat(projectVersionsPage.getNumberOfDisplayedVersions())
                .isEqualTo(1)
                .as("The version count is 1");

        projectVersionsPage = new ProjectWorkFlow()
                .createNewProjectVersion("version nums", "bravo")
                .clickProjectLink(projectName);
        projectVersionsPage.waitForDisplayedVersions(2);

        assertThat(projectVersionsPage.getNumberOfDisplayedVersions())
                .isEqualTo(2)
                .as("The version count is 2");
    }
}
