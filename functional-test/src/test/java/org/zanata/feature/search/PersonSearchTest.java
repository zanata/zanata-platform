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
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.BasePage;
import org.zanata.page.account.ProfilePage;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import com.google.common.base.Splitter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class PersonSearchTest extends ZanataTestCase {

    @Feature(summary = "The user can search for another user",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void successfulPersonSearchAndDisplay() throws Exception {
        BasePage basePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("trans")
                .expectSearchListContains("translator");

        assertThat(basePage.getZanataSearchAutocompleteItems())
                .contains("translator")
                .as("Normal user can see the person listed");

        ProfilePage profilePage = basePage.clickUserSearchEntry("translator");

        assertThat(profilePage.getUsername().trim())
                .isEqualTo("translator");
        assertThat(profilePage.getDisplayName().trim())
                .isEqualTo("translator");
        String languages = profilePage.getLanguages().trim();
        assertThat(Splitter.on(",").trimResults().split(languages))
                .contains("Hindi", "Polish", "French");

    }

    @Feature(summary = "The system will provide no results on an " +
            "unsuccessful search",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void unsuccessfulPersonSearch() throws Exception {
        BasePage basePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("snart")
                .expectSearchListContains("Search Zanata for 'snart'");

        assertThat(basePage.getZanataSearchAutocompleteItems())
                .doesNotContain("translator")
                .as("The user is not displayed");
    }

    @Feature(summary = "The user can access another user's profile via the URL",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void viewProfileViaUrl() throws Exception {
        ProfilePage profilePage = new BasicWorkFlow()
                .goToPage("profile/view/translator", ProfilePage.class);

        assertThat(profilePage.getUsername().trim())
                .isEqualTo("translator");
    }

    @Feature(summary = "A logged user can see another user's contributions",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void contributionsAreVisibleToLoggedInUsers() throws Exception {
        new LoginWorkFlow().signIn("glossarist", "glossarist");
        ProfilePage profilePage = new BasicWorkFlow()
                .goToHome()
                .enterSearch("trans")
                .expectSearchListContains("translator")
                .clickUserSearchEntry("translator");

        assertThat(profilePage.expectContributionsMatrixVisible())
                .isTrue()
                .as("A logged in user can see the user's contributions");
    }
}
