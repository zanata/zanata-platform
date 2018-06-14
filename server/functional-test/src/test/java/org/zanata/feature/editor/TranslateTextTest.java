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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.FunctionalTestHelper.assumeFalse;

/**
 * See the unit tests in package org.zanata.adapter for other file types.
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class TranslateTextTest extends ZanataTestCase {

    @Rule
    public CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();

    private ZanataRestCaller zanataRestCaller;

    private TestFileGenerator testFileGenerator = new TestFileGenerator();

    @Before
    public void before() {
        zanataRestCaller = new ZanataRestCaller();
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        assumeFalse(
                "",
                new File(CleanDocumentStorageRule.getDocumentStoragePath()
                        .concat(File.separator).concat("documents")
                        .concat(File.separator)).exists());
        new LoginWorkFlow().signIn("admin", "admin");
    }

    @Trace(summary = "The user can translate a plain text file")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void translateBasicTextFile() {
        File testfile = testFileGenerator.generateTestFileWithContent(
                "basictext", ".txt",
                "Line One\nLine Two\nLine Three");
        zanataRestCaller.createProjectAndVersion("txt-translate", "txt", "file");
        EditorPage editorPage = new ProjectWorkFlow()
                .goToProjectByName("txt-translate")
                .gotoVersion("txt")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(testfile.getAbsolutePath())
                .submitUpload()
                .clickUploadDone()
                .gotoLanguageTab()
                .translate("fr", testfile.getName());

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Line One")
                .as("Item 1 shows Line One");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Line Two")
                .as("Item 2 shows Line Two");
        assertThat(editorPage.getMessageSourceAtRowIndex(2))
                .isEqualTo("Line Three")
                .as("Item 3 shows Line Three");

        editorPage = editorPage.translateTargetAtRowIndex(0, "Une Ligne")
                .approveTranslationAtRow(0)
                .translateTargetAtRowIndex(1, "Deux Ligne")
                .approveTranslationAtRow(1)
                .translateTargetAtRowIndex(2, "Ligne Trois")
                .approveTranslationAtRow(2);

        assertTranslations(editorPage);

        // Close and reopen the editor to test save, switches to CodeMirror
        editorPage.reload();

        assertTranslations(editorPage);
    }

    private void assertTranslations(EditorPage editorPage) {
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0))
                .isEqualTo("Une Ligne")
                .as("Item 1 shows a translation of Line One");
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(1))
                .isEqualTo("Deux Ligne")
                .as("Item 2 shows a translation of Line Two");
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(2))
                .isEqualTo("Ligne Trois")
                .as("Item 3 shows a translation of Line Three");
    }
}
