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
 import org.junit.ClassRule;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.zanata.feature.DetailedTest;
 import org.zanata.page.projects.ProjectSourceDocumentsPage;
 import org.zanata.util.PropertiesHolder;
 import org.zanata.util.ResetDatabaseRule;
 import org.zanata.workflow.LoginWorkFlow;

 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;

 import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class UploadTest
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule(ResetDatabaseRule.Config.WithData);

   @Test
   public void uploadedDocumentIsInFilesystem()
   {
      String successfullyUploaded = "Document file uploadedDocumentIsInFilesystem.txt uploaded.";
      String testFileName = "uploadedDocumentIsInFilesystem.txt";
      String filePath = PropertiesHolder.getProperty("zanata.testdata.directory")
            .concat(File.separator)
            .concat(testFileName);

      assertThat("Data file "+testFileName+" exists", new File(filePath).exists());

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(filePath)
            .submitUpload();

      assertThat("Document uploaded notification shows",
            projectSourceDocumentsPage.getNotificationMessage(), Matchers.equalTo(successfullyUploaded));
      assertThat("Document shows in table", projectSourceDocumentsPage.sourceDocumentsContains(testFileName));
   }

   @Test
   public void cancelFileUpload()
   {
      String testFileName = "cancelFileUpload.txt";
      String filePath = PropertiesHolder.getProperty("zanata.testdata.directory")
            .concat(File.separator)
            .concat(testFileName);

      assertThat("Data file "+testFileName+" exists", new File(filePath).exists());

      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .enterFilePath(filePath)
            .cancelUpload();

      assertThat("Document does not show in table",
            !projectSourceDocumentsPage.sourceDocumentsContains(testFileName));
   }

   // RHBZ-990373
   @Test(expected = RuntimeException.class)
   public void emptyFilenameUpload()
   {
      ProjectSourceDocumentsPage projectSourceDocumentsPage = new LoginWorkFlow().signIn("admin", "admin")
            .goToProjects()
            .goToProject("about fedora")
            .goToVersion("master")
            .goToSourceDocuments()
            .pressUploadFileButton()
            .submitUpload();

      projectSourceDocumentsPage.assertNoCriticalErrors();
      // TODO: Verify graceful handling of scenario
   }

   // RHBZ990836
   @Test(expected = RuntimeException.class)
   public void handleReallyBigFile()
   {
      File bigFile;
      int mbyte = 1024 * 1024;
      int fileSize = 500;

      try {
         bigFile = File.createTempFile("bigFile", "txt");
         RandomAccessFile randomAccessFile = new RandomAccessFile(bigFile, "rw");
         randomAccessFile.setLength(mbyte*fileSize);
      } catch (IOException e)
      {
         throw new RuntimeException("Unable to generate the test file");
      }
      assertThat("Data file "+bigFile+" exists", bigFile.exists());
      assertThat("Data file "+bigFile+" is big", (int)bigFile.length() / mbyte,
            Matchers.equalTo(fileSize));

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

   // RHBZ-990373
   @Test(expected = RuntimeException.class)
   public void failOnInvalidFileUpload()
   {
      File noFile;
      try {
         noFile = File.createTempFile("thereIsNoSpoon", "txt");
      } catch(IOException ioException)
      {
         throw new RuntimeException("failOnInvalidFileUpload:Unable to create temporary file");
      }
      assertThat("Data file "+noFile.getName()+" exists", noFile.exists());

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
      // TODO: Verify graceful handling of scenario
   }
}