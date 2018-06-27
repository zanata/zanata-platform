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
package org.zanata.feature.dashboard

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.openqa.selenium.By
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.util.HasEmailRule
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.LoginWorkFlow
import com.fasterxml.jackson.databind.ObjectMapper

import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.ZanataRestCaller.buildSourceResource
import org.zanata.util.ZanataRestCaller.buildTextFlow

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
@Category(DetailedTest::class)
class DashboardTest : ZanataTestCase() {
    @get:Rule
    val emailRule = HasEmailRule()
    private var dashboard: DashboardBasePage? = null

    @Before
    fun setUp() {
        val restCaller = ZanataRestCaller()
        val resource = buildSourceResource("a", buildTextFlow("res1", "content"))
        // create 6 activities
        for (i in 0..5) {
            val projectSlug = "activity$i"
            val iterationSlug = "v$i"
            restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                    "gettext")
            restCaller.postSourceDocResource(projectSlug, iterationSlug,
                    resource, false)
        }
        dashboard = LoginWorkFlow().signIn("admin", "admin")
    }

    @Trace(summary = "The user can traverse Dashboard activity lists")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun dashboardBasicTests() {
        assertThat(dashboardPresentAfterLogin())
                .`as`("Dashboard is present").isTrue()
        assertThat(activityListExpands())
                .`as`("Activity list is present and expandable").isTrue()
        assertThat(projectListIsNotEmpty())
                .`as`("Project List is not empty").isTrue()
    }

    @Throws(Exception::class)
    private fun dashboardPresentAfterLogin(): Boolean {
        return dashboard!!.activityTabIsSelected()
    }

    @Throws(Exception::class)
    private fun activityListExpands(): Boolean {
        val activityTab = dashboard!!.gotoActivityTab()
        assertThat(activityTab.isMoreActivity).isTrue()
        assertThat(activityTab.myActivityList).isNotEmpty
        return activityTab.clickMoreActivity()
    }

    @Throws(Exception::class)
    private fun projectListIsNotEmpty(): Boolean {
        val projectsTab = dashboard!!.gotoProjectsTab()
        return projectsTab.maintainedProjectList.size > 0
    }

    @Trace(summary = "The user can export user data as JSON")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun exportUserData() {
        val url = dashboard!!.goToSettingsTab().gotoSettingsAccountTab()
                .exportUserDataURL
        // avoid switching browser tabs (target = _blank)
        val driver = dashboard!!.driver
        driver.get(url)
        val json = driver.findElement(By.tagName("pre")).text

        // just test that the JSON is valid:
        val value = ObjectMapper().readValue(json, Any::class.java)
        assertThat(value).isInstanceOf(Map::class.java)
        // NB the contents of the export are handled by the unit test ExportRestServiceTest
    }

    @Trace(summary = "The user can change their password")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun passwordChange() {
        dashboard!!.goToSettingsTab().gotoSettingsAccountTab()
                .enterOldPassword("admin").enterNewPassword("admin2")
                .clickUpdatePasswordButton()
        assertThat(dashboard!!
                .expectNotification(DashboardBasePage.PASSWORD_UPDATE_SUCCESS))
                .isTrue()
    }

    @Trace(summary = "The user can begin creating a project from the Dashboard")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(Exception::class)
    fun createProject() {
        val createProjectPage = dashboard!!.gotoProjectsTab()
                .clickOnCreateProjectLink()
        assertThat(createProjectPage.title).contains("New Project")
    }
}
