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
package org.zanata.feature.document;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.VersionDocumentsPage;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.FunctionalTestHelper.assumeTrue;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class UploadTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(UploadTest.class);

    @Rule
    public CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();
    private TestFileGenerator testFileGenerator = new TestFileGenerator();
    private String documentStorageDirectory;

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        new LoginWorkFlow().signIn("admin", "admin");
        new ZanataRestCaller().createProjectAndVersion("uploadtest",
                "txt-upload", "file");
        documentStorageDirectory = CleanDocumentStorageRule
                .getDocumentStoragePath().concat(File.separator)
                .concat("documents").concat(File.separator);
        if (new File(documentStorageDirectory).exists()) {
            log.warn("Document storage directory exists (cleanup incomplete)");
        }
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    @Ignore("Error in system path")
    public void uploadedDocumentIsInFilesystem() {
        File originalFile = testFileGenerator.generateTestFileWithContent(
                "uploadedDocumentIsInFilesystem", ".txt",
                "This is a test file");
        String testFileName = originalFile.getName();
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(originalFile.getAbsolutePath())
                        .submitUpload().clickUploadDone();
        File newlyCreatedFile =
                new File(documentStorageDirectory, testFileGenerator
                        .getFirstFileNameInDirectory(documentStorageDirectory));
        assertThat(testFileGenerator.getTestFileContent(newlyCreatedFile))
                .isEqualTo("This is a test file")
                .as("The contents of the file were also uploaded");
        VersionDocumentsPage versionDocumentsPage = versionDocumentsTab
                .gotoDocumentTab().expectSourceDocsContains(testFileName);
        assertThat(versionDocumentsPage.sourceDocumentsContains(testFileName))
                .isTrue().as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void cancelFileUpload() {
        File cancelUploadFile = testFileGenerator.generateTestFileWithContent(
                "cancelFileUpload", ".txt", "Cancel File Upload Test");
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(cancelUploadFile.getAbsolutePath())
                        .cancelUpload();
        assertThat(versionDocumentsTab
                .sourceDocumentsContains("cancelFileUpload.txt")).isFalse()
                        .as("Document does not show in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyFilenameUpload() {
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton();
        assertThat(versionDocumentsTab.canSubmitDocument()).isFalse()
                .as("The upload button is not available");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("RHBZ-990836")
    public void handleReallyBigFile() {
        File bigFile = testFileGenerator.generateTestFileWithContent("bigFile",
                ".txt", "Big file content");
        long fileSizeInMB = (1024 * 1024) * 500;
        testFileGenerator.forceFileSize(bigFile, fileSizeInMB);
        assumeTrue("Data file " + bigFile.getName() + " is big",
                bigFile.length() == fileSizeInMB);
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(bigFile.getAbsolutePath()).submitUpload()
                        .clickUploadDone();
        versionDocumentsTab.assertNoCriticalErrors();
        // TODO: Verify graceful handling of scenario
    }
    // RHBZ993445

    @Ignore("Fails on Chrome")
    public void failOnInvalidFileUpload() {
        File noFile = testFileGenerator.generateTestFileWithContent(
                "thereIsNoSpoon", ".txt", "This file will be deleted");
        String successfullyUploaded =
                "Document " + noFile.getName() + " uploaded.";
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(noFile.getAbsolutePath());
        assertThat(noFile.delete() && !noFile.exists())
                .as("Data file " + noFile.getName() + " does not exist");
        versionDocumentsTab =
                versionDocumentsTab.submitUpload().clickUploadDone();
        versionDocumentsTab.assertNoCriticalErrors();
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void handleVeryLongFileNames() {
        File longFile = testFileGenerator.generateTestFileWithContent(
                testFileGenerator.longFileName(), ".txt",
                "This filename is long");
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(longFile.getAbsolutePath())
                        .submitUpload().clickUploadDone();
        VersionDocumentsPage versionDocumentsPage = versionDocumentsTab
                .gotoDocumentTab().expectSourceDocsContains(longFile.getName());
        assertThat(versionDocumentsPage
                .sourceDocumentsContains(longFile.getName())).isTrue()
                        .as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyFile() {
        File emptyFile = testFileGenerator
                .generateTestFileWithContent("emptyFile", ".txt", "");
        assumeTrue("File is empty", emptyFile.length() == 0);
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(emptyFile.getAbsolutePath())
                        .submitUpload().clickUploadDone();
        assertThat(emptyFile.exists()).isTrue()
                .as("Data file emptyFile.txt still exists");
        VersionDocumentsPage versionDocumentsPage =
                versionDocumentsTab.gotoDocumentTab()
                        .expectSourceDocsContains(emptyFile.getName());
        assertThat(versionDocumentsPage
                .sourceDocumentsContains(emptyFile.getName())).isTrue()
                        .as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void rejectUnsupportedValidFiletype() {
        File unsupportedFile = testFileGenerator.generateTestFileWithContent(
                "testfodt", ".fodt", "<xml></xml>");
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("uploadtest")
                        .gotoVersion("txt-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(unsupportedFile.getAbsolutePath());
        assertThat(versionDocumentsTab.getUploadError())
                .contains(VersionDocumentsTab.UNSUPPORTED_FILETYPE)
                .as("Unsupported file type error is shown");
    }
}
