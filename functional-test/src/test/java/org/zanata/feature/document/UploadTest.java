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

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projectversion.VersionDocumentsPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.FunctionalTestHelper.assumeTrue;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
@Slf4j
public class UploadTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Rule
    public CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();

    private TestFileGenerator testFileGenerator = new TestFileGenerator();
    private String documentStorageDirectory;

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        documentStorageDirectory = CleanDocumentStorageRule
                .getDocumentStoragePath()
                .concat(File.separator)
                .concat("documents")
                .concat(File.separator);

        if (new File(documentStorageDirectory).exists()) {
            log.warn("Document storage directory exists (cleanup incomplete)");
        }
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void uploadedDocumentIsInFilesystem() {
        File originalFile =
                testFileGenerator.generateTestFileWithContent(
                        "uploadedDocumentIsInFilesystem", ".txt",
                        "This is a test file");
        String testFileName = originalFile.getName();

        VersionLanguagesPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoSettingsTab().gotoSettingsDocumentsTab()
                        .pressUploadFileButton()
                        .enterFilePath(originalFile.getAbsolutePath())
                        .submitUpload();

        // We should be able to assume the new file is the only file
        assertThat(new File(documentStorageDirectory).list().length)
                .isEqualTo(1)
                .as("There is only one uploaded source file");

        File newlyCreatedFile = new File(documentStorageDirectory,
                testFileGenerator
                        .getFirstFileNameInDirectory(documentStorageDirectory));

        assertThat(testFileGenerator.getTestFileContent(newlyCreatedFile))
                .isEqualTo("This is a test file")
                .as("The contents of the file were also uploaded");
        VersionDocumentsPage versionDocumentsPage = projectVersionPage
                .gotoDocumentTab()
                .waitForSourceDocsContains(testFileName);

        assertThat(versionDocumentsPage.sourceDocumentsContains(testFileName))
                .isTrue()
                .as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void cancelFileUpload() {
        File cancelUploadFile =
                testFileGenerator.generateTestFileWithContent(
                        "cancelFileUpload", ".txt", "Cancel File Upload Test");

        VersionDocumentsTab versionDocumentsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(cancelUploadFile.getAbsolutePath())
                .cancelUpload();

        assertThat(versionDocumentsTab.sourceDocumentsContains("cancelFileUpload.txt"))
            .isFalse()
            .as("Document does not show in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyFilenameUpload() {
        VersionDocumentsTab versionDocumentsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab().gotoSettingsDocumentsTab()
                .pressUploadFileButton();

        assertThat(versionDocumentsTab.canSubmitDocument())
                .isFalse()
                .as("The upload button is not available");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Ignore("RHBZ-990836")
    public void handleReallyBigFile() {
        File bigFile =
                testFileGenerator.generateTestFileWithContent(
                        "bigFile", ".txt", "Big file content");
        long fileSizeInMB = (1024 * 1024) * 500;
        testFileGenerator.forceFileSize(bigFile, fileSizeInMB);

        assumeTrue("Data file " + bigFile.getName() + " is big",
                bigFile.length() == fileSizeInMB);

        VersionLanguagesPage projectVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(bigFile.getAbsolutePath())
                .submitUpload();

        projectVersionPage.assertNoCriticalErrors();
        // TODO: Verify graceful handling of scenario
    }

    // RHBZ993445
    @Ignore("Fails on Chrome")
    public void failOnInvalidFileUpload() {
        File noFile =
                testFileGenerator.generateTestFileWithContent("thereIsNoSpoon",
                        ".txt", "This file will be deleted");
        String successfullyUploaded =
                "Document " + noFile.getName() + " uploaded.";

        VersionDocumentsTab versionDocumentsTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(noFile.getAbsolutePath());

        assertThat(noFile.delete() && !noFile.exists())
                .as("Data file " + noFile.getName() + " does not exist");

        VersionLanguagesPage versionLanguagesPage = versionDocumentsTab
                .submitUpload();
        versionLanguagesPage.assertNoCriticalErrors();
        assertThat(versionLanguagesPage.expectNotification(successfullyUploaded))
                .isTrue()
                .as("Success message is shown");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void handleVeryLongFileNames() {
        File longFile = testFileGenerator.generateTestFileWithContent(
                testFileGenerator.longFileName(), ".txt",
                "This filename is long");
        String successfullyUploaded =
                "Document " + longFile.getName() + " uploaded.";

        VersionLanguagesPage projectVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(longFile.getAbsolutePath())
                .submitUpload();

        assertThat(projectVersionPage.expectNotification(successfullyUploaded))
                .isTrue()
                .as("Document uploaded notification shows");

        VersionDocumentsPage versionDocumentsPage = projectVersionPage
                .gotoDocumentTab()
                .waitForSourceDocsContains(longFile.getName());

        assertThat(versionDocumentsPage.sourceDocumentsContains(longFile.getName()))
                .isTrue()
                .as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void emptyFile() {
        File emptyFile = testFileGenerator
                .generateTestFileWithContent("emptyFile", ".txt", "");
        String successfullyUploaded =
                "Document " + emptyFile.getName() + " uploaded.";

        assumeTrue("File is empty", emptyFile.length() == 0);

        VersionLanguagesPage projectVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(emptyFile.getAbsolutePath())
                .submitUpload();

        assertThat(emptyFile.exists())
                .isTrue()
                .as("Data file emptyFile.txt still exists");
        assertThat(projectVersionPage.expectNotification(successfullyUploaded))
                .isTrue()
                .as("Document uploaded notification shows");

        VersionDocumentsPage versionDocumentsPage = projectVersionPage
                .gotoDocumentTab()
                .waitForSourceDocsContains(emptyFile.getName());

        assertThat(versionDocumentsPage.sourceDocumentsContains(emptyFile.getName()))
                .isTrue()
                .as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void rejectUnsupportedValidFiletype() {
        File unsupportedFile = testFileGenerator
                .generateTestFileWithContent("testfodt", ".fodt", "<xml></xml>");
        String uploadFailed =
                "Unrecognized file extension for " + unsupportedFile.getName();

        VersionLanguagesPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoSettingsTab().gotoSettingsDocumentsTab()
                        .pressUploadFileButton()
                        .enterFilePath(unsupportedFile.getAbsolutePath())
                        .submitUpload();

        assertThat(projectVersionPage.expectNotification(uploadFailed))
                .isTrue()
                .as("Unrecognized file extension error is shown");
    }

}
