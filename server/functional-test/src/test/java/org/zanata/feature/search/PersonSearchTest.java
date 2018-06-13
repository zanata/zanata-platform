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

package org.zanata.feature.search;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.account.ProfilePage;
import org.zanata.page.explore.ExplorePage;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class PersonSearchTest extends ZanataTestCase {

    @Trace(summary = "The user can search for another user")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void successfulPersonSearchAndDisplay() throws Exception {
        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("trans")
                .expectPersonListContains("translator");

        assertThat(explorePage.getUserSearchResults())
                .as("Normal user can see the person listed")
                .contains("translator");

        ProfilePage profilePage =
                explorePage.clickUserSearchEntry("translator");

        assertThat(profilePage.getUsername().trim())
                .isEqualTo("translator");
        assertThat(profilePage.getDisplayName().trim())
                .isEqualTo("translator");
    }

    @Trace(summary = "The system will provide no results on an " +
            "unsuccessful search")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void unsuccessfulPersonSearch() throws Exception {
        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("snart");

        assertThat(explorePage.getUserSearchResults().isEmpty())
                .as("The user is not displayed")
                .isTrue();
    }

    @Trace(summary = "The user can access another user's profile via the URL")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void viewProfileViaUrl() throws Exception {
        BasicWorkFlow basicWorkFlow = new BasicWorkFlow();
        ProfilePage profilePage = basicWorkFlow
            .goToHome()
            .gotoExplore()
            .enterSearch("translator")
            .clickUserSearchEntry("translator");
        assertThat(profilePage.getUsername().trim())
            .isEqualTo("translator");
    }

    @Trace(summary = "A logged user can see another user's contributions")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void contributionsAreVisibleToLoggedInUsers() throws Exception {
        new LoginWorkFlow().signIn("glossarist", "glossarist");
        ProfilePage profilePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("trans")
                .expectPersonListContains("translator")
                .clickUserSearchEntry("translator");

        assertThat(profilePage.expectContributionsMatrixVisible())
                .as("A logged in user can see the user's contributions")
                .isTrue();
    }
}
