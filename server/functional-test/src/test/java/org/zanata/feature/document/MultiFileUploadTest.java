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

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class MultiFileUploadTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(MultiFileUploadTest.class);

    @Rule
    public CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();
    private TestFileGenerator testFileGenerator = new TestFileGenerator();
    private String documentStorageDirectory;

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        new LoginWorkFlow().signIn("admin", "admin");
        new ZanataRestCaller().createProjectAndVersion("multi-upload",
                "multi-upload", "file");
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
    public void uploadedDocumentsAreInFilesystem() {
        File firstFile = testFileGenerator.generateTestFileWithContent(
                "multiuploadInFilesystem", ".txt", "This is a test file");
        File secondFile = testFileGenerator.generateTestFileWithContent(
                "multiuploadInFilesystem2", ".txt",
                "This is another test file");
        String testFileName = firstFile.getName();
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("multi-upload")
                        .gotoVersion("multi-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(firstFile.getAbsolutePath())
                        .enterFilePath(secondFile.getAbsolutePath())
                        .submitUpload().clickUploadDone();
        assertThat(new File(documentStorageDirectory).list().length)
                .isEqualTo(2).as("There are two uploaded source files");
        VersionDocumentsPage versionDocumentsPage = versionDocumentsTab
                .gotoDocumentTab().expectSourceDocsContains(testFileName);
        assertThat(versionDocumentsPage.getSourceDocumentNames())
                .contains(firstFile.getName()).contains(secondFile.getName())
                .as("The documents were uploaded");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void removeFileFromUploadList() {
        File keptUploadFile = testFileGenerator.generateTestFileWithContent(
                "removeFileFromUploadList", ".txt", "Remove File Upload Test");
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("multi-upload")
                        .gotoVersion("multi-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(keptUploadFile.getAbsolutePath())
                        .enterFilePath("/tmp/fakefile.txt");
        versionDocumentsTab.waitForPageSilence();
        // TODO try to eliminate this:
        versionDocumentsTab.expectSomeUploadItems();
        assertThat(versionDocumentsTab.getUploadList())
                .contains(keptUploadFile.getName()).contains("fakefile.txt")
                .as("The intended files are listed");
        versionDocumentsTab = versionDocumentsTab.clickRemoveOn("fakefile.txt");
        versionDocumentsTab.waitForPageSilence();
        assertThat(versionDocumentsTab.getUploadList())
                .contains(keptUploadFile.getName())
                .doesNotContain("fakefile.txt")
                .as("The fakefile has been removed");
        VersionDocumentsPage versionDocumentsPage = versionDocumentsTab
                .submitUpload().clickUploadDone().gotoDocumentTab();
        assertThat(versionDocumentsPage.getSourceDocumentNames())
                .contains(keptUploadFile.getName())
                .doesNotContain("fakefile.txt")
                .as("Only the intended file was uploaded");
    }
}
