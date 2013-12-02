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

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.utility.DashboardPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class VersionGroupFullTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule =
            new ResetDatabaseRule(ResetDatabaseRule.Config.WithData);
    private DashboardPage dashboardPage;

    @Before
    public void before() {
        dashboardPage = new LoginWorkFlow().signIn("admin", "admin");
    }

    @Test
    @Category(BasicAcceptanceTest.class)
    public void createABasicGroup() {
        String groupID = "basic-group";
        String groupName = "A Basic Group";

        CreateVersionGroupPage createVersionGroupPage =
                dashboardPage.goToGroups().createNewGroup();
        createVersionGroupPage.inputGroupId(groupID);
        createVersionGroupPage.inputGroupName(groupName);
        createVersionGroupPage
                .inputGroupDescription("A basic group can be saved");
        VersionGroupsPage versionGroupsPage =
                createVersionGroupPage.saveGroup();
        assertThat("Group was created", versionGroupsPage.getGroupNames()
                .contains(groupName));
        VersionGroupPage groupView = versionGroupsPage.goToGroup(groupName);
        assertThat("The group is displayed", groupView.getTitle(),
                Matchers.equalTo("Groups:".concat(groupName)));
    }

    @Test
    public void requiredFields() {
        String errorMsg = "value is required";
        String groupID = "verifyRequiredFieldsGroupID";
        String groupName = "verifyRequiredFieldsGroupName";

        CreateVersionGroupPage groupPage =
                dashboardPage.goToGroups().createNewGroup().saveGroupFailure();
        assertThat("The two errors are value is required",
                groupPage.getFieldValidationErrors(), Matchers.contains(errorMsg, errorMsg));

        groupPage =
                groupPage.clearFields().inputGroupName(groupName)
                        .saveGroupFailure();
        assertThat("The value required error shown", groupPage.getFieldValidationErrors(),
                Matchers.contains(errorMsg));

        groupPage =
                groupPage.clearFields().inputGroupId(groupID)
                        .saveGroupFailure();
        assertThat("The value required error shown", groupPage.getFieldValidationErrors(),
                Matchers.contains(errorMsg));
    }

    @Test
    public void groupIDFieldSize() {
        String groupID = "abcdefghijklmnopqrstuvwxyzabcdefghijklmn";
        String groupIDExtra = "xyz";
        String groupName = "verifyIDFieldSizeName";

        CreateVersionGroupPage groupPage = dashboardPage
                .goToGroups()
                .createNewGroup()
                .inputGroupId(groupID + groupIDExtra)
                .inputGroupName(groupName);

        assertThat("User cannot enter more than 40 characters",
                groupPage.getGroupIdValue(),
                Matchers.equalTo(groupID));
    }

    @Test
    public void groupDescriptionFieldSize() {
        String errorMsg = "value must be shorter than or equal to 100 characters";
        String groupID = "verifyDescriptionFieldSizeID";
        String groupName = "verifyDescriptionFieldSizeName";
        String groupDescription =
                "This text is to test that the description field takes no more than exactly 100 characters - actually.";

        assertThat("Description length is greater than 100 characters",
                groupDescription.length(), Matchers.equalTo(101));
        CreateVersionGroupPage groupPage =
                dashboardPage.goToGroups().createNewGroup();
        groupPage.inputGroupId(groupID).inputGroupName(groupName)
                .inputGroupDescription(groupDescription);
        groupPage.saveGroupFailure();
        assertThat("Invalid length error is shown",
                groupPage.getFieldValidationErrors(),
                Matchers.contains(errorMsg));

        groupPage.clearFields();
        groupDescription = groupDescription.substring(0, 100);
        assertThat("Description length is now 100 characters",
                groupDescription.length(), Matchers.equalTo(100));
        groupPage.inputGroupId("verifyDescriptionFieldSizeID").inputGroupName(
                groupName);
        VersionGroupsPage verGroupsPage =
                groupPage.inputGroupDescription(groupDescription).saveGroup();
        assertThat("A group description of 100 chars is valid",
                verGroupsPage.getGroupNames(), Matchers.hasItem(groupName));

    }

    @Test
    public void addANewProjectVersionToAnEmptyGroup() {
        String groupID = "add-version-to-empty-group";
        String groupName = "AddVersionToEmptyGroup";
        VersionGroupPage versionGroupPage = dashboardPage
                .goToGroups()
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()
                .goToGroup(groupName)
                .clickProjectsTab()
                .clickAddProjectVersionsButton()
                .enterProjectVersion("about-fedora master")
                .clickAddProjectButton()
                .clickProjectsTab();

        assertThat("The version group shows in the list",
                versionGroupPage.getProjectVersionsInGroup(),
                Matchers.hasItem("about-fedora master"));

    }

}
