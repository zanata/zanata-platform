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
package org.zanata.feature.versionGroup

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.groups.VersionGroupPage
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * Tests relating to custom urls handled in the page.
 *
 * The urls mapped for a version group are of the form:
 * <zanata>/version-group/view/<slug>/languages/<locale_code>
 * <zanata>/version-group/view/<slug>/projects/<project_slug>[<version_slug>]
 * <zanata>/version-group/view/<slug>/maintainers
 * <zanata>/version-group/view/<slug>/settings
 * <zanata>/version-group/view/<slug>/settings/languages etc...
 *
 * @author Carlos Munoz [camunoz@redhat.com](mailto:camunoz@redhat.com)
 */
@DetailedTest
class VersionGroupUrlTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Test
    fun testUrlChangeUpdatesActiveElements() {
        val versionGroupPage = createVersionGroup()
        testBasicGroupUrl(versionGroupPage)
    }

    private fun testBasicGroupUrl(versionGroupPage: VersionGroupPage) {
        assertThat(versionGroupPage.isLanguagesTabActive).isTrue()
    }

    @Test
    fun testTabClicksChangeUrl() {
        val versionGroupPage = createVersionGroup()

        testLanguageTabClick(versionGroupPage)
        testProjectTabClick(versionGroupPage)
        testMaintainersTabClick(versionGroupPage)
        testSettingsTabClick(versionGroupPage)
    }

    private fun testLanguageTabClick(versionGroupPage: VersionGroupPage) {
        versionGroupPage.clickLanguagesTab()
        assertThat(versionGroupPage.url)
                .contains("/version-group/view/test-group/languages")
    }

    private fun testProjectTabClick(versionGroupPage: VersionGroupPage) {
        versionGroupPage.clickProjectsTab()
        assertThat(versionGroupPage.url).contains("/version-group/view/test-group/projects")
    }

    private fun testMaintainersTabClick(versionGroupPage: VersionGroupPage) {
        versionGroupPage.clickMaintainersTab()
        assertThat(versionGroupPage.url).contains("/version-group/view/test-group/maintainers")
    }

    private fun testSettingsTabClick(versionGroupPage: VersionGroupPage) {
        versionGroupPage.clickSettingsTab()
        assertThat(versionGroupPage.url).contains("/version-group/view/test-group/settings")
    }

    private fun createVersionGroup(): VersionGroupPage {
        val groupID = "test-group"
        val groupName = "A Test group"
        return BasicWorkFlow()
                .goToDashboard()
                .gotoGroupsTab()
                .createNewGroup()
                .inputGroupId(groupID)
                .inputGroupName(groupName)
                .saveGroup()
    }
}
