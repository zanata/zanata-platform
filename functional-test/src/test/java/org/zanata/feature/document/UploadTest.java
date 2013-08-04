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

 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.zanata.feature.DetailedTest;
 import org.zanata.page.projects.ProjectSourceDocumentsPage;
 import org.zanata.util.PropertiesHolder;
 import org.zanata.util.ResetDatabaseRule;
 import org.zanata.util.TestFileGenerator;
 import org.zanata.workflow.BasicWorkFlow;
 import org.zanata.workflow.LoginWorkFlow;

 import java.io.File;

 import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class UploadTest
{
   @Rule
   public ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule(ResetDatabaseRule.Config.WithData);
   private TestFileGenerator testFileGenerator = new TestFileGenerator();

   @Before
   public void before()
   {
      new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
   }

   @Test
   public void uploadedDocumentIsInFilesystem()
   {
      File originalFile = testFileGenerator.generateTestFileWithContent(
            "uploadedDocumentIsInFilesystem", ".txt", "This is a test file");
      String newFilePath = PropertiesHolder.getProperty("document.storage.directory")
            .concat(File.separator).concat("documents").concat(File.separator);
      String testFileName = originalFile.getName();
      String successfullyUploaded = "Document file " +testFileName+ " uploaded.";

      assertThat("Data file " + testFileName + " exists", originalFile.exists());
      // We should be able to assume the target dir is non-existent
      assertThat("Target directory does not exist", !(new File(newFilePath).exists()));

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(originalFile.getAbsolutePath())
            .submitUpload();

      // We should be able to assume the new file is the only file
      assertThat("There is only one uploaded source file", new File(newFilePath).list().length,
            Matchers.equalTo(1));

      File newlyCreatedFile = new File(newFilePath,
            testFileGenerator.getFirstFileNameInDirectory(newFilePath));

      assertThat("The contents of the file were also uploaded",
            testFileGenerator.getTestFileContent(newlyCreatedFile),
            Matchers.equalTo("This is a test file"));

      assertThat("Document uploaded notification shows",
            projectSourceDocumentsPage.getNotificationMessage(), Matchers.equalTo(successfullyUploaded));
      assertThat("Document shows in table",
            projectSourceDocumentsPage.sourceDocumentsContains(testFileName));
   }

   @Test
   public void cancelFileUpload()
   {
      File cancelUploadFile = testFileGenerator.generateTestFileWithContent(
            "cancelFileUpload", ".txt", "Cancel File Upload Test");
      assertThat("Data file cancelFileUpload.txt exists", cancelUploadFile.exists());

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(cancelUploadFile.getAbsolutePath())
            .cancelUpload();

      assertThat("Document does not show in table",
            !projectSourceDocumentsPage.sourceDocumentsContains("cancelFileUpload.txt"));
   }

   @Test
   public void emptyFilenameUpload()
   {
      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton();

      assertThat("The upload button is not available", !projectSourceDocumentsPage.canSubmitDocument());
   }

   // RHBZ990836
   @Test(expected = RuntimeException.class)
   public void handleReallyBigFile()
   {
      File bigFile = testFileGenerator.generateTestFileWithContent("bigFile", ".txt", "Big file content");
      long fileSizeInMB = (1024 * 1024) * 500;
      testFileGenerator.forceFileSize(bigFile, fileSizeInMB);

      assertThat("Data file " + bigFile + " exists", bigFile.exists());
      assertThat("Data file "+bigFile+" is big", bigFile.length(), Matchers.equalTo(fileSizeInMB));

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(bigFile.getAbsolutePath())
            .submitUpload();

      projectSourceDocumentsPage.assertNoCriticalErrors();
      // TODO: Verify graceful handling of scenario
   }

   // RHBZ993445
   @Ignore("Fails on Chrome")
   public void failOnInvalidFileUpload()
   {
      File noFile = testFileGenerator.generateTestFileWithContent(
            "thereIsNoSpoon", ".txt", "This file will be deleted");
      assertThat("Data file "+noFile.getName()+" exists", noFile.exists());
      String successfullyUploaded = "Document file " + noFile.getName()+ " uploaded.";

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(noFile.getAbsolutePath());

      noFile.delete();
      assertThat("Data file " + noFile.getName() + " does not exists", !noFile.exists());

      projectSourceDocumentsPage = projectSourceDocumentsPage.submitUpload();
      projectSourceDocumentsPage.assertNoCriticalErrors();
      assertThat("Success message is not shown", projectSourceDocumentsPage.getNotificationMessage(),
            Matchers.not(Matchers.equalTo(successfullyUploaded)));
   }

   @Test
   public void handleVeryLongFileNames()
   {
      File longFile = testFileGenerator.generateTestFileWithContent(
            testFileGenerator.longFileName(), ".txt", "This filename is long");
      String successfullyUploaded = "Document file "+longFile.getName()+" uploaded.";

      assertThat("Data file "+longFile.getName()+" exists", longFile.exists());

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(longFile.getAbsolutePath())
            .submitUpload();

      assertThat("Document uploaded notification shows",
            projectSourceDocumentsPage.getNotificationMessage(), Matchers.equalTo(successfullyUploaded));
      assertThat("Document shows in table", projectSourceDocumentsPage.sourceDocumentsContains(longFile.getName()));
   }

   @Test
   public void emptyFile()
   {
      File emptyFile = testFileGenerator.generateTestFileWithContent("emptyFile", ".txt", "");
      String successfullyUploaded = "Document file "+emptyFile.getName()+" uploaded.";

      assertThat("Data file emptyFile.txt exists", emptyFile.exists());
      assertThat("File is empty", (int)emptyFile.length(), Matchers.equalTo(0));

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(emptyFile.getAbsolutePath())
            .submitUpload();
      assertThat("Data file emptyFile.txt still exists", emptyFile.exists());
      assertThat("Document uploaded notification shows",
            projectSourceDocumentsPage.getNotificationMessage(), Matchers.equalTo(successfullyUploaded));
      assertThat("Document shows in table", projectSourceDocumentsPage.sourceDocumentsContains(emptyFile.getName()));
   }

}
