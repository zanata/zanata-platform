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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.workflow.LoginWorkFlow
import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.ZanataRestCaller.Companion.buildSourceResource
import org.zanata.util.ZanataRestCaller.Companion.buildTextFlow

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
@DetailedTest
class DashboardActivityTest : ZanataTestCase() {

    private lateinit var dashboard: DashboardBasePage

    @BeforeEach
    fun setUp() {
        val resource = buildSourceResource("a", buildTextFlow("res1", "content"))
        // create 6 activities
        for (i in 0..5) {
            val projectSlug = "activity$i"
            val iterationSlug = "v$i"
            zanataRestCaller.createProjectAndVersion(projectSlug, iterationSlug,
                    "gettext")
            zanataRestCaller.postSourceDocResource(projectSlug, iterationSlug,
                    resource, false)
        }
        dashboard = LoginWorkFlow().signIn("admin", "admin")
    }

    @Trace(summary = "The user can traverse Dashboard activity lists")
    @Test
    fun dashboardBasicTests() {
        assertThat(dashboardPresentAfterLogin())
                .describedAs("Dashboard is present").isTrue()
        assertThat(activityListExpands())
                .describedAs("Activity list is present and expandable").isTrue()
        assertThat(projectListIsNotEmpty())
                .describedAs("Project List is not empty").isTrue()
    }

    private fun dashboardPresentAfterLogin(): Boolean {
        return dashboard.activityTabIsSelected()
    }

    private fun activityListExpands(): Boolean {
        val activityTab = dashboard.gotoActivityTab()
        assertThat(activityTab.isMoreActivity).isTrue()
        assertThat(activityTab.myActivityList).isNotEmpty
        return activityTab.clickMoreActivity()
    }

    private fun projectListIsNotEmpty(): Boolean {
        val projectsTab = dashboard.gotoProjectsTab()
        return projectsTab.maintainedProjectList.isNotEmpty()
    }
}
