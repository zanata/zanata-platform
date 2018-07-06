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
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.BasicWorkFlow
import org.assertj.core.api.Assertions.assertThat
import org.zanata.feature.testharness.ComprehensiveTest

/**
 * @author Sachin Pathare [spathare@redhat.com](mailto:spathare@redhat.com)
 */
@ComprehensiveTest
class ExploreCTest : ZanataTestCase() {

    @Test
    fun enabledCancelButtonAfterSearch() {
        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("abcde")
        assertThat(explorePage.isCancelButtonEnabled)
                .describedAs("Cancel button is enabled after search")
                .isTrue()
    }

    @Test
    fun ableToClearSearch() {
        val explorePage = BasicWorkFlow()
                .goToHome()
                .gotoExplore()
                .enterSearch("abcde")
                .clearSearch()
        assertThat(explorePage.isSearchFieldCleared)
                .describedAs("Able clear the search field")
                .isTrue()
    }
}
