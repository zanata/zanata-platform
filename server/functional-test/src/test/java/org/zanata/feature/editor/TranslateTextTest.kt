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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.webtrans.EditorPage
import org.zanata.util.TestFileGenerator
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.zanata.util.CleanDocumentStorageExtension

/**
 * See the unit tests in package org.zanata.adapter for other file types.
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
@ExtendWith(CleanDocumentStorageExtension::class)
class TranslateTextTest : ZanataTestCase() {

    private val testFileGenerator = TestFileGenerator()

    @BeforeEach
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        LoginWorkFlow().signIn("admin", "admin")
    }

    @Trace(summary = "The user can translate a plain text file")
    @Test
    fun translateBasicTextFile() {
        val testfile = testFileGenerator.generateTestFileWithContent(
                "basictext", ".txt", "Line One\nLine Two\nLine Three")
        zanataRestCaller.createProjectAndVersion("txt-translate", "txt", "file")
        var editorPage = ProjectWorkFlow()
                .goToProjectByName("txt-translate")
                .gotoVersion("txt")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(testfile.absolutePath)
                .submitUpload()
                .clickUploadDone()
                .gotoLanguageTab()
                .translate("fr", testfile.name)

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .describedAs("Item 1 shows Line One")
                .isEqualTo("Line One")
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .describedAs("Item 2 shows Line Two")
                .isEqualTo("Line Two")
        assertThat(editorPage.getMessageSourceAtRowIndex(2))
                .describedAs("Item 3 shows Line Three")
                .isEqualTo("Line Three")

        editorPage = editorPage.translateTargetAtRowIndex(0, "Une Ligne")
                .approveTranslationAtRow(0)
                .translateTargetAtRowIndex(1, "Deux Ligne")
                .approveTranslationAtRow(1)
                .translateTargetAtRowIndex(2, "Ligne Trois")
                .approveTranslationAtRow(2)

        assertTranslations(editorPage)

        // Close and reopen the editor to test save, switches to CodeMirror
        editorPage.reload()

        assertTranslations(editorPage)
    }

    private fun assertTranslations(editorPage: EditorPage) {
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0))
                .describedAs("Item 1 shows a translation of Line One")
                .isEqualTo("Une Ligne")
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(1))
                .describedAs("Item 2 shows a translation of Line Two")
                .isEqualTo("Deux Ligne")
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(2))
                .describedAs("Item 3 shows a translation of Line Three")
                .isEqualTo("Ligne Trois")
    }
}
