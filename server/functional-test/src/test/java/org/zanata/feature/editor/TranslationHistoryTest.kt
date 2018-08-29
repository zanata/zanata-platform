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
package org.zanata.feature.editor

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class TranslationHistoryTest : ZanataTestCase() {

    @Test
    fun showTranslationHistory() {
        LoginWorkFlow().signIn("admin", "admin")
        val editorPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("pl", "About_Fedora")
                .translateTargetAtRowIndex(0, "historytest")
                .saveAsFuzzyAtRow(0)
                .clickShowHistoryForRow(0)

        assertThat(editorPage.getHistoryEntryAuthor(0))
                .describedAs("The user is displayed")
                .startsWith("admin")
        assertThat(editorPage.getHistoryEntryContent(0))
                .describedAs("The content change is displayed")
                .contains("historytest")
    }

    @Test
    // fails intermittently
    @Disabled
    fun compareTranslationHistory() {
        LoginWorkFlow().signIn("admin", "admin")
        var editorPage = ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("pl", "About_Fedora")
                .translateTargetAtRowIndex(0, "historytest")
                .saveAsFuzzyAtRow(0)
                .translateTargetAtRowIndex(0, "historytest2")
                .approveTranslationAtRow(0)
                .clickShowHistoryForRow(0)
                .clickCompareOn(0)
                .clickCompareOn(1)

        assertThat(editorPage.translationHistoryCompareTabtext)
                .describedAs("The tab displays compared versionsList")
                .isEqualTo("Compare ver. 2 and 1")

        editorPage = editorPage.clickCompareVersionsTab()

        assertThat(editorPage.getComparisonTextInRow(0))
                .describedAs("The new text is displayed")
                .isEqualTo("historytest2")
        assertThat(editorPage.getComparisonTextInRow(1))
                .describedAs("The old text is also displayed")
                .isEqualTo("historytest2")
        assertThat(editorPage.comparisonTextDiff)
                .describedAs("The diff is displayed")
                .contains("--2")
    }
}
