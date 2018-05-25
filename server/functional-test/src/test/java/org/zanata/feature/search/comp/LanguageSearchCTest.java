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

package org.zanata.feature.search.comp;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.explore.ExplorePage;
import org.zanata.workflow.BasicWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sachin Pathare <a
 *         href="mailto:spathare@redhat.com">spathare@redhat.com</a>
 */
@Category(TestPlan.ComprehensiveTest.class)
public class LanguageSearchCTest extends ZanataTestCase {

    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void successfulLangSearch() throws Exception {
        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("eng")
                .expectLanguageTeamListContains("English (United States)");

        assertThat(explorePage.getLanguageSearchResults())
                .as("Normal user can see the languages listed")
                .contains("English (United States)");
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION)
    public void unsuccessfulLangSearch() throws Exception {
        ExplorePage explorePage = new BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("abc");

        assertThat(explorePage.getLanguageSearchResults().isEmpty())
                .as("The Language is not displayed")
                .isTrue();
    }
}
