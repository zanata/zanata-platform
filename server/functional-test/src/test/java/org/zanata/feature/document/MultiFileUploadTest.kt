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
import org.zanata.common.ProjectType
import org.zanata.feature.Trace
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

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@Category(DetailedTest::class)
class MultiFileUploadTest : ZanataTestCase() {

    @get:Rule
    var documentStorageRule = CleanDocumentStorageRule()

    @Before
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        LoginWorkFlow().signIn("admin", "admin")
        ZanataRestCaller().createProjectAndVersion("multi-upload",
                "multi-upload", "file")
        val documentStorageDirectory = CleanDocumentStorageRule
                .getDocumentStoragePath() +
                File.separator + "documents" + File.separator
        if (File(documentStorageDirectory).exists()) {
            log.warn("Document storage directory exists (cleanup incomplete)")
        }
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Trace(summary = "The administrator can upload raw files for translation")
    @Throws(Exception::class)
    fun uploadFileTypeDocument() {
        val testFile = createTempFile("testtxtfile", ".txt")
        testFile.writeText(testString)
        val versionDocumentsPage = ProjectWorkFlow()
                .goToProjectByName("multi-upload")
                .gotoVersion("multi-upload")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(testFile.absolutePath)
                .submitUpload()
                .clickUploadDone()
                .gotoDocumentTab()
        // check that document shows in table
        versionDocumentsPage.expectSourceDocsContains(testFile.name)
    }

    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun removeFileFromUploadList() {
        val keptUploadFile = createTempFile("removeFileFromUploadList", ".txt")
        keptUploadFile.writeText("Remove File Upload Test")

        val tempFile = createTempFile("fakefile", ".txt")

        var versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("multi-upload")
                .gotoVersion("multi-upload")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(keptUploadFile.absolutePath)
                .enterFilePath(tempFile.absolutePath)
        versionDocumentsTab.waitForPageSilence()
        // TODO try to eliminate this:
        versionDocumentsTab.expectSomeUploadItems()

        assertThat(versionDocumentsTab.uploadList)
                .describedAs("The intended files are listed")
                .contains(keptUploadFile.name).contains(tempFile.name)

        versionDocumentsTab = versionDocumentsTab.clickRemoveOn(tempFile.name)
        versionDocumentsTab.waitForPageSilence()

        assertThat(versionDocumentsTab.uploadList)
                .describedAs("The fakefile has been removed")
                .contains(keptUploadFile.name)
                .doesNotContain(tempFile.name)

        val versionDocumentsPage = versionDocumentsTab
                .submitUpload().clickUploadDone().gotoDocumentTab()
        assertThat(versionDocumentsPage.sourceDocumentNames)
                .describedAs("Only the intended file was uploaded")
                .contains(keptUploadFile.name)
                .doesNotContain(tempFile.name)
    }

    /*
     * Ensure none of the supported file types will cause an error when queued
     */
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun addAllTypesTOTheQueue() {
        var versionDocumentsTab = ProjectWorkFlow()
                .goToProjectByName("multi-upload")
                .gotoVersion("multi-upload")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
        versionDocumentsTab = createAndAddToQueue(versionDocumentsTab)
        versionDocumentsTab.assertNoCriticalErrors()
        assertThat(versionDocumentsTab.errors)
                .describedAs("There are no errors")
                .isEmpty()
    }

    private// TODO: Replace ProjectType.getSupportedSourceFileTypes
    fun createAndAddToQueue(documentsTab: VersionDocumentsTab): VersionDocumentsTab {
        var tab = documentsTab

        ProjectType.getSupportedSourceFileTypes(ProjectType.File).forEach {
            documentType -> documentType.sourceExtensions.forEach { extension ->
                log.info("[addAllTypesTOTheQueue]: Test {}", extension)
                val testFile = createTempFile("testfile", ".$extension")
                testFile.writeText(testString)
                assertThat(testFile.exists())
                        .describedAs("The file ${testFile.name} exists")
                        .isTrue()
                tab = tab.enterFilePath(testFile.path)
                assertThat(documentsTab.errors.isEmpty()).isTrue()
            }
        }
        return tab
    }

    companion object {
        private val log = org.slf4j.LoggerFactory
                .getLogger(MultiFileUploadTest::class.java)
        private const val testString = "Test text 1"
    }
}
