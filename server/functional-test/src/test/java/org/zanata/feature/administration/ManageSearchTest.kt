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
package org.zanata.feature.administration

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class ManageSearchTest : ZanataTestCase() {

    private lateinit var dashboardPage: DashboardBasePage

    @Before
    fun before() {
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .`as`("Admin is logged in")
                .isEqualTo("admin")
        dashboardPage = BasicWorkFlow().goToDashboard()
    }

    @Trace(summary = "The administrator can clear and regenerate all of the " +
            "search indexes")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION.toLong())
    @Ignore("Unstable - sometimes the button isn't ready and sometimes " +
            "the index fails to complete.")
    fun regenerateSearchIndexes() {
        var manageSearchPage = dashboardPage
                .goToAdministration()
                .goToManageSeachPage()
                .clickSelectAll()

        assertThat(manageSearchPage.allActionsSelected())
                .`as`("All actions are selected")
                .isTrue()
        assertThat(manageSearchPage.noOperationsRunningIsDisplayed())
                .`as`("No operations are running")
                .isTrue()

        manageSearchPage = manageSearchPage
                .performSelectedActions()
                .expectActionsToFinish()

        assertThat(manageSearchPage.completedIsDisplayed())
                .`as`("Completed is displayed")
                .isTrue()

        assertThat(manageSearchPage.noOperationsRunningIsDisplayed())
                .`as`("No operations are running")
                .isTrue()
    }

    @Trace(summary = "The administrator can abort the regeneration of the " +
            "search indexes")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION.toLong())
    @Ignore("Data set not large enough to achieve stable test")
    fun abortReIndexes() {
        var manageSearchPage = dashboardPage
                .goToAdministration()
                .goToManageSeachPage()
                .clickSelectAll()

        assertThat(manageSearchPage.allActionsSelected())
                .`as`("All actions are selected")
                .isTrue()
        assertThat(manageSearchPage.noOperationsRunningIsDisplayed())
                .`as`("No operations are running")
                .isTrue()

        manageSearchPage = manageSearchPage
                .performSelectedActions()
                .abort()

        assertThat(manageSearchPage.abortedIsDisplayed())
                .`as`("Aborted is displayed")
                .isTrue()
    }
}
