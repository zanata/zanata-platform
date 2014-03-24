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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.NoScreenshot;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.FunctionalTestHelper.assumeFalse;
import static org.zanata.util.FunctionalTestHelper.assumeTrue;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
@NoScreenshot
public class UploadTest {

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
        documentStorageDirectory =
                CleanDocumentStorageRule.getDocumentStoragePath()
                        .concat(File.separator).concat("documents")
                        .concat(File.separator);
        assumeFalse("", new File(documentStorageDirectory).exists());
    }

    @Test
    @Category(BasicAcceptanceTest.class)
    public void uploadedDocumentIsInFilesystem() {
        File originalFile =
                testFileGenerator.generateTestFileWithContent(
                        "uploadedDocumentIsInFilesystem", ".txt",
                        "This is a test file");
        String testFileName = originalFile.getName();
        String successfullyUploaded = "Document " + testFileName + " uploaded.";

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
                        .enterFilePath(originalFile.getAbsolutePath())
                        .submitUpload();

        // We should be able to assume the new file is the only file
        assertThat("There is only one uploaded source file", new File(
                documentStorageDirectory).list().length, Matchers.equalTo(1));

        File newlyCreatedFile =
                new File(
                        documentStorageDirectory,
                        testFileGenerator
                                .getFirstFileNameInDirectory(documentStorageDirectory));

        assertThat("The contents of the file were also uploaded",
                testFileGenerator.getTestFileContent(newlyCreatedFile),
                Matchers.equalTo("This is a test file"));

        assertThat("Document uploaded notification shows",
                projectVersionPage.getNotificationMessage(),
                Matchers.equalTo(successfullyUploaded));
        assertThat("Document shows in table",
                projectVersionPage.sourceDocumentsContains(testFileName));
    }

    @Test
    public void cancelFileUpload() {
        File cancelUploadFile =
                testFileGenerator.generateTestFileWithContent(
                        "cancelFileUpload", ".txt", "Cancel File Upload Test");

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
                        .enterFilePath(cancelUploadFile.getAbsolutePath())
                        .cancelUpload();

        assertThat("Document does not show in table",
                !projectVersionPage
                        .sourceDocumentsContains("cancelFileUpload.txt"));
    }

    @Test
    public void emptyFilenameUpload() {
        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton();

        assertThat("The upload button is not available",
                !projectVersionPage.canSubmitDocument());
    }

    // RHBZ990836
    @Test(expected = RuntimeException.class)
    public void handleReallyBigFile() {
        File bigFile =
                testFileGenerator.generateTestFileWithContent("bigFile",
                        ".txt", "Big file content");
        long fileSizeInMB = (1024 * 1024) * 500;
        testFileGenerator.forceFileSize(bigFile, fileSizeInMB);

        assumeTrue("Data file " + bigFile + " is big",
                bigFile.length() == fileSizeInMB);

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
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

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
                        .enterFilePath(noFile.getAbsolutePath());

        assertThat("Data file " + noFile.getName() + " does not exists",
                noFile.delete() && !noFile.exists());

        projectVersionPage = projectVersionPage.submitUpload();
        projectVersionPage.assertNoCriticalErrors();
        assertThat("Success message is not shown",
                projectVersionPage.getNotificationMessage(),
                Matchers.not(Matchers.equalTo(successfullyUploaded)));
    }

    @Test
    public void handleVeryLongFileNames() {
        File longFile =
                testFileGenerator.generateTestFileWithContent(
                        testFileGenerator.longFileName(), ".txt",
                        "This filename is long");
        String successfullyUploaded =
                "Document " + longFile.getName() + " uploaded.";

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
                        .enterFilePath(longFile.getAbsolutePath())
                        .submitUpload();

        assertThat("Document uploaded notification shows",
                projectVersionPage.getNotificationMessage(),
                Matchers.equalTo(successfullyUploaded));
        assertThat("Document shows in table",
                projectVersionPage.sourceDocumentsContains(longFile.getName()));
    }

    @Test
    public void emptyFile() {
        File emptyFile =
                testFileGenerator.generateTestFileWithContent("emptyFile",
                        ".txt", "");
        String successfullyUploaded =
                "Document " + emptyFile.getName() + " uploaded.";

        assumeTrue("File is empty", emptyFile.length() == 0);

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
                        .enterFilePath(emptyFile.getAbsolutePath())
                        .submitUpload();

        assertThat("Data file emptyFile.txt still exists", emptyFile.exists());
        assertThat("Document uploaded notification shows",
                projectVersionPage.getNotificationMessage(),
                Matchers.equalTo(successfullyUploaded));
        assertThat("Document shows in table",
                projectVersionPage.sourceDocumentsContains(emptyFile.getName()));
    }

    @Test
    public void rejectUnsupportedValidFiletype() {
        File unsupportedFile =
                testFileGenerator.generateTestFileWithContent("testfodt",
                        ".fodt", "<xml></xml>");
        String uploadFailed =
                "Unrecognized file extension for " + unsupportedFile.getName();

        ProjectVersionPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoVersion("master")
                        .gotoDocumentTab().pressUploadFileButton()
                        .enterFilePath(unsupportedFile.getAbsolutePath())
                        .submitUpload();

        assertThat("Unrecognized file extension for ",
                projectVersionPage.getNotificationMessage(),
                Matchers.equalTo(uploadFailed));
    }

}
