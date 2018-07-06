/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.feature.search

import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class PersonSearchTest : ZanataTestCase() {

    @Trace(summary = "The user can search for another user")
    @Test
    fun successfulPersonSearchAndDisplay() {
        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("trans")
                .expectPersonListContains("translator")

        assertThat(explorePage.userSearchResults)
                .describedAs("Normal user can see the person listed")
                .contains("translator")

        val profilePage = explorePage.clickUserSearchEntry("translator")

        assertThat(profilePage.username.trim { it <= ' ' })
                .isEqualTo("translator")
        assertThat(profilePage.displayName.trim { it <= ' ' })
                .isEqualTo("translator")
    }

    @Trace(summary = "The system will provide no results on an " +
            "unsuccessful search")
    @Test
    fun unsuccessfulPersonSearch() {
        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("snart")

        assertThat(explorePage.userSearchResults.isEmpty())
                .describedAs("The user is not displayed")
                .isTrue()
    }

    @Trace(summary = "The user can access another user's profile via the URL")
    @Test
    fun viewProfileViaUrl() {
        val basicWorkFlow = BasicWorkFlow()
        val profilePage = basicWorkFlow
                .goToHome()
                .gotoExplore()
                .enterSearch("translator")
                .clickUserSearchEntry("translator")
        assertThat(profilePage.username.trim { it <= ' ' })
                .isEqualTo("translator")
    }

    @Trace(summary = "A logged user can see another user's contributions")
    @Test
    fun contributionsAreVisibleToLoggedInUsers() {
        LoginWorkFlow().signIn("glossarist", "glossarist")
        val profilePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("trans")
                .expectPersonListContains("translator")
                .clickUserSearchEntry("translator")

        assertThat(profilePage.expectContributionsMatrixVisible())
                .describedAs("A logged in user can see the user's contributions")
                .isTrue()
    }
}
