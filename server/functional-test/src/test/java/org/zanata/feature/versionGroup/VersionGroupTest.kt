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
package org.zanata.feature.versionGroup

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.dashboard.DashboardGroupsTab
import org.zanata.page.groups.CreateVersionGroupPage
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.FunctionalTestHelper.assumeTrue

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class VersionGroupTest : ZanataTestCase() {

    private lateinit var dashboardGroupsTab: DashboardGroupsTab

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
        dashboardGroupsTab = BasicWorkFlow().goToHome().goToMyDashboard()
                .gotoGroupsTab()
    }

    @Trace(summary = "The administrator can create a basic group")
    @Test
    fun createABasicGroup() {
        val groupID = "basic-group"
        val groupName = "A Basic Group"
        val versionGroupPage = dashboardGroupsTab
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .inputGroupDescription("A basic group can be saved")
                .saveGroup()

        assertThat(versionGroupPage.groupName.trim { it <= ' ' })
                .describedAs("The group name is correct")
                .isEqualTo(groupName)
    }

    @Trace(summary = "The administrator must fill in the required fields " + "to create a group")
    @Test
    fun requiredFields() {
        val errorMsg = "value is required"
        val groupID = "verifyRequiredFieldsGroupID"
        val groupName = "verifyRequiredFieldsGroupName"

        var groupPage = dashboardGroupsTab
                .createNewGroup()
                .saveGroupFailure()

        assertThat(groupPage.errors)
                .describedAs("The two errors are value is required")
                .contains(errorMsg, errorMsg)

        groupPage = groupPage.clearFields()
                .inputGroupName(groupName)
                .saveGroupFailure()

        assertThat(groupPage.errors)
                .describedAs("The value required error shown")
                .contains(errorMsg)

        groupPage = groupPage.clearFields()
                .inputGroupId(groupID)
                .saveGroupFailure()

        assertThat(groupPage.errors)
                .describedAs("The value required error shown")
                .contains(errorMsg)
    }

    @Trace(summary = "The administrator must enter valid data into the " + "required fields to create a group")
    @Test
    fun groupDescriptionFieldSize() {
        val groupID = "verifyDescriptionFieldSizeID"
        val groupName = "verifyDescriptionFieldSizeName"
        var groupDescription = "This text is to test that the description " + "field takes no more than exactly 100 characters - actually."

        assumeTrue("Description length is greater than 100 characters",
                groupDescription.length == 101)

        val groupPage = dashboardGroupsTab
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .inputGroupDescription(groupDescription)
                .saveGroupFailure()

        assertThat(groupPage.errors)
                .describedAs("Invalid length error is shown")
                .contains(CreateVersionGroupPage.LENGTH_ERROR)

        groupDescription = groupDescription.substring(0, 100)
        val versionGroupPage = groupPage
                .clearFields()
                .inputGroupId("verifyDescriptionFieldSizeID")
                .inputGroupName(groupName)
                .inputGroupDescription(groupDescription)
                .saveGroup()

        assertThat(versionGroupPage.groupName.trim { it <= ' ' })
                .describedAs("A group description of 100 chars is valid")
                .isEqualTo(groupName)
    }

    @Trace(summary = "The administrator can add a project version to " + "a newly created group")
    @Test
    fun addANewProjectVersionToAnEmptyGroup() {
        val groupID = "add-version-to-empty-group"
        val groupName = "AddVersionToEmptyGroup"
        val versionGroupPage = dashboardGroupsTab
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()
                .clickProjectsTab()
                .clickAddProjectVersionsButton()
                .enterProjectVersion("about-fedora")
                .selectProjectVersion("about fedora about-fedora master")
                .clickProjectsTab()

        assertThat(versionGroupPage.projectVersionsInGroup)
                .describedAs("The version group shows in the list")
                .contains("about fedora\nmaster")
    }

    @Trace(summary = "The administrator can use numbers, letters, periods, " + "underscores and hyphens to create a group")
    @Test
    fun groupIdCharactersAreAcceptable() {
        val groupID = "test-_.1"
        val groupName = "TestValidIdCharacters"
        val versionGroupPage = dashboardGroupsTab
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()

        assertThat(versionGroupPage.groupName)
                .describedAs("The group was created")
                .isEqualTo(groupName)
    }

    @Trace(summary = "The administrator must use numbers, letters, periods, " + "underscores and hyphens to create a group")
    @Test
    fun inputValidationForID() {
        val inputText = "group|name"
        val groupPage = dashboardGroupsTab
                .createNewGroup()
                .inputGroupId(inputText)
                .inputGroupName(inputText)
        groupPage.defocus(groupPage.groupNameField)
        groupPage.saveGroupFailure()

        assertThat(groupPage.errors)
                .describedAs("Validation error is displayed for $inputText")
                .contains(CreateVersionGroupPage.VALIDATION_ERROR)
    }

    @Test
    fun addLanguageToGroup() {
        val groupID = "add-language-to-a-group"
        val groupName = "AddLanguageToGroup"
        val versionGroupPage = dashboardGroupsTab
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()
                .clickLanguagesTab()
                .clickAddLanguagesButton()
                .activateLanguageList()
                .selectLanguage("French[fr]")
                .clickLanguagesTab()

        assertThat(versionGroupPage.languagesForGroup).contains(
                "French\nfr")
    }
}
