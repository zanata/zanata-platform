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
package org.zanata.feature.versionGroup;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import static org.zanata.util.FunctionalTestHelper.assumeTrue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class VersionGroupTest extends ZanataTestCase {

    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private VersionGroupsPage versionGroupsPageBase;

    @BeforeClass
    public static void beforeClass() {
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
    }

    @Before
    public void before() {
        versionGroupsPageBase = new BasicWorkFlow().goToHome().goToGroups();
    }

    @Feature(summary = "The administrator can create a basic group",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 170109)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void createABasicGroup() throws Exception {
        String groupID = "basic-group";
        String groupName = "A Basic Group";
        VersionGroupsPage versionGroupsPage = versionGroupsPageBase
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .inputGroupDescription("A basic group can be saved")
                .saveGroup();
        assertThat(versionGroupsPage.getGroupNames())
                .contains(groupName)
                .as("The version group was created");
        VersionGroupPage groupView = versionGroupsPage.goToGroup(groupName);
        assertThat(groupView.getTitle())
                .isEqualTo("Zanata: Groups - ".concat(groupName))
                .as("The group is displayed");
    }

    @Feature(summary = "The administrator must fill in the required fields " +
            "to create a group",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 170109)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void requiredFields() throws Exception {
        String errorMsg = "value is required";
        String groupID = "verifyRequiredFieldsGroupID";
        String groupName = "verifyRequiredFieldsGroupName";

        CreateVersionGroupPage groupPage = versionGroupsPageBase
                .createNewGroup()
                .saveGroupFailure();

        assertThat(groupPage.getErrors())
                .contains(errorMsg, errorMsg)
                .as("The two errors are value is required");

        groupPage = groupPage.clearFields()
                .inputGroupName(groupName)
                .saveGroupFailure();

        assertThat(groupPage.getErrors())
                .contains(errorMsg)
                .as("The value required error shown");

        groupPage = groupPage.clearFields()
                .inputGroupId(groupID)
                .saveGroupFailure();

        assertThat(groupPage.getErrors())
                .contains(errorMsg)
                .as("The value required error shown");
    }

    @Feature(summary = "The administrator must enter valid data into the " +
            "required fields to create a group",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 170109)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void groupDescriptionFieldSize() throws Exception {
        String groupID = "verifyDescriptionFieldSizeID";
        String groupName = "verifyDescriptionFieldSizeName";
        String groupDescription = "This text is to test that the description " +
                "field takes no more than exactly 100 characters - actually.";

        assumeTrue("Description length is greater than 100 characters",
                groupDescription.length() == 101);

        CreateVersionGroupPage groupPage = versionGroupsPageBase
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .inputGroupDescription(groupDescription)
                .saveGroupFailure();

        assertThat(groupPage.getErrors())
                .contains(CreateVersionGroupPage.LENGTH_ERROR)
                .as("Invalid length error is shown");

        groupDescription = groupDescription.substring(0, 100);
        VersionGroupsPage verGroupsPage = groupPage
                .clearFields()
                .inputGroupId("verifyDescriptionFieldSizeID")
                .inputGroupName(groupName)
                .inputGroupDescription(groupDescription)
                .saveGroup();

        assertThat(verGroupsPage.getGroupNames())
                .contains(groupName)
                .as("A group description of 100 chars is valid");
    }

    @Feature(summary = "The administrator can add a project version to " +
            "a newly created group",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 170109)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addANewProjectVersionToAnEmptyGroup() throws Exception {
        String groupID = "add-version-to-empty-group";
        String groupName = "AddVersionToEmptyGroup";
        VersionGroupPage versionGroupPage = versionGroupsPageBase
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()
                .goToGroup(groupName)
                .clickProjectsTab()
                .clickAddProjectVersionsButton()
                .enterProjectVersion("about-fedora")
                .selectProjectVersion("about-fedora master")
                .clickProjectsTab();

        assertThat(versionGroupPage.getProjectVersionsInGroup())
                .contains("about fedora\nmaster")
                .as("The version group shows in the list");
    }

    @Feature(summary = "The administrator can use numbers, letters, periods, " +
            "underscores and hyphens to create a group",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 396261)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void groupIdCharactersAreAcceptable() throws Exception {
        String groupID = "test-_.1";
        String groupName = "TestValidIdCharacters";
        VersionGroupPage versionGroupPage = versionGroupsPageBase
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()
                .goToGroup(groupName);

        assertThat(versionGroupPage.getGroupName())
                .isEqualTo(groupName)
                .as("The group was created");
    }

    @Feature(summary = "The administrator must use numbers, letters, periods, "
            + "underscores and hyphens to create a group",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 396261)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void inputValidationForID() throws Exception {
        String inputText = "group|name";
        CreateVersionGroupPage groupPage = versionGroupsPageBase
                .createNewGroup()
                .inputGroupId(inputText)
                .inputGroupName(inputText);
        groupPage.defocus(groupPage.groupNameField);
        groupPage.saveGroupFailure();

        assertThat(groupPage.expectError(
                    CreateVersionGroupPage.VALIDATION_ERROR))
                .contains(CreateVersionGroupPage.VALIDATION_ERROR)
                .as("Validation error is displayed for " + inputText);
    }
}
