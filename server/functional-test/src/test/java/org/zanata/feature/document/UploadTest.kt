/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.document

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab
import org.zanata.util.CleanDocumentStorageRule
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.FunctionalTestHelper.assumeTrue
import org.zanata.util.randomString

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class UploadTest : ZanataTestCase() {

    @get:Rule
    var documentStorageRule = CleanDocumentStorageRule()

    @Before
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        LoginWorkFlow().signIn("admin", "admin")
        ZanataRestCaller().createProjectAndVersion("uploadtest",
                "txt-upload", "file")
        val documentStorageDirectory = CleanDocumentStorageRule
                .getDocumentStoragePath() +
                File.separator + "documents" + File.separator
        if (File(documentStorageDirectory).exists()) {
            log.warn("Document storage directory exists (cleanup incomplete)")
        }
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun cancelFileUpload() {
        val cancelUploadFile = createTempFile("cancelFileUpload", ".txt")
        cancelUploadFile.writeText("Cancel File Upload Test")
        val versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("uploadtest")
                .gotoVersion("txt-upload").gotoSettingsTab()
                .gotoSettingsDocumentsTab().pressUploadFileButton()
                .enterFilePath(cancelUploadFile.absolutePath)
                .cancelUpload()
        assertThat(versionDocumentsTab
                .sourceDocumentsContains("cancelFileUpload.txt"))
                .describedAs("Document does not show in table")
                .isFalse()
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun emptyFilenameUpload() {
        val versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("uploadtest")
                .gotoVersion("txt-upload").gotoSettingsTab()
                .gotoSettingsDocumentsTab().pressUploadFileButton()
        assertThat(versionDocumentsTab.canSubmitDocument())
                .`as`("The upload button is not available")
                .isFalse()
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun handleVeryLongFileNames() {
        val longFile = createTempFile(randomString(200, true), ".txt")
        longFile.writeText("This filename is long")
        val versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("uploadtest")
                .gotoVersion("txt-upload").gotoSettingsTab()
                .gotoSettingsDocumentsTab().pressUploadFileButton()
                .enterFilePath(longFile.absolutePath)
                .submitUpload().clickUploadDone()
        val versionDocumentsPage = versionDocumentsTab
                .gotoDocumentTab().expectSourceDocsContains(longFile.name)
        assertThat(versionDocumentsPage
                .sourceDocumentsContains(longFile.name))
                .describedAs("Document shows in table")
                .isTrue()
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun emptyFile() {
        val emptyFile = createTempFile("emptyFile", ".txt")
        assumeTrue("File is empty", emptyFile.length() == 0L)
        val versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("uploadtest")
                .gotoVersion("txt-upload").gotoSettingsTab()
                .gotoSettingsDocumentsTab().pressUploadFileButton()
                .enterFilePath(emptyFile.absolutePath)
                .submitUpload().clickUploadDone()
        assertThat(emptyFile.exists())
                .describedAs("Data file ${emptyFile.name} still exists")
                .isTrue()
        val versionDocumentsPage = versionDocumentsTab.gotoDocumentTab()
                .expectSourceDocsContains(emptyFile.name)
        assertThat(versionDocumentsPage
                .sourceDocumentsContains(emptyFile.name))
                .describedAs("Document shows in table")
                .isTrue()
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun rejectUnsupportedValidFiletype() {
        val unsupportedFile = createTempFile("testfodt", ".fodt")
        unsupportedFile.writeText("<xml></xml>")
        val versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("uploadtest")
                .gotoVersion("txt-upload").gotoSettingsTab()
                .gotoSettingsDocumentsTab().pressUploadFileButton()
                .enterFilePath(unsupportedFile.absolutePath)
        assertThat(versionDocumentsTab.uploadError)
                .describedAs("Unsupported file type error is shown")
                .contains(VersionDocumentsTab.UNSUPPORTED_FILETYPE)
    }

    companion object {
        private val log =
                org.slf4j.LoggerFactory.getLogger(UploadTest::class.java)
    }
}
