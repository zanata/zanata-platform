/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.feature.search.comp

import org.junit.jupiter.api.Test
import org.zanata.feature.testharness.ComprehensiveTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Sachin Pathare [spathare@redhat.com](mailto:spathare@redhat.com)
 */
@ComprehensiveTest
class GroupSearchCTest : ZanataTestCase() {

    @Test
    fun successfulGroupSearchAndDisplay() {
        val groupID = "basic-group"
        val groupName = "A Basic Group"
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")

        BasicWorkFlow().goToHome()
                .goToMyDashboard()
                .gotoGroupsTab()
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .inputGroupDescription("A basic group can be saved")
                .saveGroup()

        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("Basic")
                .expectGroupListContains(groupName)

        assertThat(explorePage.groupSearchResults)
                .describedAs("Normal user can see the group listed")
                .contains(groupName)

        val versionGroupPage = explorePage.clickGroupSearchEntry(groupName)

        assertThat(versionGroupPage.groupName.trim { it <= ' ' })
                .isEqualTo(groupName)
    }

    @Test
    fun unsuccessfulGroupSearch() {
        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("groop")

        assertThat(explorePage.groupSearchResults.isEmpty())
                .describedAs("The group is not displayed")
                .isTrue()
    }
}
