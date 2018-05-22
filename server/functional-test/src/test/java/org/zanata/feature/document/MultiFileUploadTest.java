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
import org.zanata.common.DocumentType;
import org.zanata.common.ProjectType;
import org.zanata.feature.Trace;
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
import java.util.List;

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
    private static String testString = "Test text 1";

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        new LoginWorkFlow().signIn("admin", "admin");
        new ZanataRestCaller().createProjectAndVersion("multi-upload",
                "multi-upload", "file");
        String documentStorageDirectory = CleanDocumentStorageRule
                .getDocumentStoragePath().concat(File.separator)
                .concat("documents").concat(File.separator);
        if (new File(documentStorageDirectory).exists()) {
            log.warn("Document storage directory exists (cleanup incomplete)");
        }
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Trace(summary = "The administrator can upload raw files for translation")
    public void uploadFileTypeDocument() throws Exception {
        File testFile = new TestFileGenerator()
                .generateTestFileWithContent("testtxtfile", ".txt", testString);
        String testFileName = testFile.getName();
        VersionDocumentsPage versionDocumentsPage =
                new ProjectWorkFlow().goToProjectByName("multi-upload")
                        .gotoVersion("multi-upload")
                        .gotoSettingsTab()
                        .gotoSettingsDocumentsTab()
                        .pressUploadFileButton()
                        .enterFilePath(testFile.getAbsolutePath())
                        .submitUpload()
                        .clickUploadDone()
                        .gotoDocumentTab();
        assertThat(versionDocumentsPage.expectSourceDocsContains(testFileName))
                // FIXME should there be another assertion here? (expectSourceDocsContains has one)
                .as("Document shows in table");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void removeFileFromUploadList() {
        File keptUploadFile = testFileGenerator.generateTestFileWithContent(
                "removeFileFromUploadList", ".txt", "Remove File Upload Test");

        File tempfile = testFileGenerator
                .generateTestFileWithContent("fakefile", ".txt", "");

        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("multi-upload")
                        .gotoVersion("multi-upload")
                        .gotoSettingsTab()
                        .gotoSettingsDocumentsTab()
                        .pressUploadFileButton()
                        .enterFilePath(keptUploadFile.getAbsolutePath())
                        .enterFilePath(tempfile.getAbsolutePath());
        versionDocumentsTab.waitForPageSilence();
        // TODO try to eliminate this:
        versionDocumentsTab.expectSomeUploadItems();
        assertThat(versionDocumentsTab.getUploadList())
                .contains(keptUploadFile.getName()).contains(tempfile.getName())
                .as("The intended files are listed");
        versionDocumentsTab = versionDocumentsTab.clickRemoveOn(tempfile.getName());
        versionDocumentsTab.waitForPageSilence();
        assertThat(versionDocumentsTab.getUploadList())
                .contains(keptUploadFile.getName())
                .doesNotContain(tempfile.getName())
                .as("The fakefile has been removed");
        VersionDocumentsPage versionDocumentsPage = versionDocumentsTab
                .submitUpload().clickUploadDone().gotoDocumentTab();
        assertThat(versionDocumentsPage.getSourceDocumentNames())
                .contains(keptUploadFile.getName())
                .doesNotContain(tempfile.getName())
                .as("Only the intended file was uploaded");
    }

    /*
     * Ensure none of the supported file types will cause an error when queued
     */
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addAllTypesTOTheQueue() {
        VersionDocumentsTab versionDocumentsTab =
                new ProjectWorkFlow().goToProjectByName("multi-upload")
                        .gotoVersion("multi-upload")
                        .gotoSettingsTab()
                        .gotoSettingsDocumentsTab()
                        .pressUploadFileButton();
        versionDocumentsTab = createAndAddToQueue(versionDocumentsTab);
        versionDocumentsTab.assertNoCriticalErrors();
        assertThat(versionDocumentsTab.getErrors().isEmpty());
    }

    @SuppressWarnings("deprecation")
    // TODO: Replace ProjectType.getSupportedSourceFileTypes
    private VersionDocumentsTab createAndAddToQueue(VersionDocumentsTab versionDocumentsTab) {
        List<DocumentType> projectTypes = ProjectType
                .getSupportedSourceFileTypes(ProjectType.File);
        for (DocumentType documentType : projectTypes) {
            for (String extension : documentType.getSourceExtensions()) {
                log.info("[addAllTypesTOTheQueue]: Test {}", extension);
                File testFile = testFileGenerator.generateTestFileWithContent(
                        "testfile",
                        "." + extension,
                        testString);
                assertThat(testFile.exists());
                versionDocumentsTab = versionDocumentsTab.enterFilePath(testFile.getPath());
                assertThat(versionDocumentsTab.getErrors().isEmpty());
            }
        }
        return versionDocumentsTab;
    }
}
