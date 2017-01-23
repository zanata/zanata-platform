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
package org.zanata.feature.editor;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.RetryRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class TranslationHistoryTest extends ZanataTestCase {

    @Rule
    public RetryRule retryRule = new RetryRule(0);

    @Test
    public void showTranslationHistory() {
        new LoginWorkFlow().signIn("admin", "admin");
        EditorPage editorPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("pl", "About_Fedora")
                .translateTargetAtRowIndex(0, "historytest")
                .saveAsFuzzyAtRow(0)
                .clickShowHistoryForRow(0);

        assertThat(editorPage.getHistoryEntryAuthor(0))
                .startsWith("admin")
                .as("The user is displayed");
        assertThat(editorPage.getHistoryEntryContent(0))
                .contains("historytest")
                .as("The content change is displayed");
    }

    @Test
    // fails intermittently
    @Ignore
    public void compareTranslationHistory() {
        new LoginWorkFlow().signIn("admin", "admin");
        EditorPage editorPage = new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .translate("pl", "About_Fedora")
                .translateTargetAtRowIndex(0, "historytest")
                .saveAsFuzzyAtRow(0)
                .translateTargetAtRowIndex(0, "historytest2")
                .approveTranslationAtRow(0)
                .clickShowHistoryForRow(0)
                .clickCompareOn(0)
                .clickCompareOn(1);

        assertThat(editorPage.getTranslationHistoryCompareTabtext())
                .isEqualTo("Compare ver. 2 and 1")
                .as("The tab displays compared versions");

        editorPage = editorPage.clickCompareVersionsTab();

        assertThat(editorPage.getComparisonTextInRow(0))
                .isEqualTo("historytest2")
                .as("The new text is displayed");
        assertThat(editorPage.getComparisonTextInRow(1))
                .isEqualTo("historytest2")
                .as("The old text is also displayed");
        assertThat(editorPage.getComparisonTextDiff())
                .contains("--2")
                .as("The diff is displayed");
    }
}
