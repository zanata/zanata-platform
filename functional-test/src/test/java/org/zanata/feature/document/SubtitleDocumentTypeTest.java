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
package org.zanata.feature.document;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.VersionDocumentsPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.FunctionalTestHelper.assumeTrue;

/**
 * Covers more detailed testing of the subtitle formats
 * @see DocTypeUploadTest
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class SubtitleDocumentTypeTest extends ZanataTestCase {

    @ClassRule
    public static SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Rule
    public CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();

    private TestFileGenerator testFileGenerator = new TestFileGenerator();
    private String sep = System.getProperty("line.separator");

    @BeforeClass
    public static void beforeClass() {
        new LoginWorkFlow().signIn("admin", "admin");
        new ProjectWorkFlow().createNewProjectVersion(
                "about fedora", "subtitle-upload", "File")
                .logout();
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void similarSrtEntriesAreIndividual() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(testFileGenerator
                .generateTestFileWithContent("duplicationinsrtfile", ".srt",
                        "1" + sep +
                        "00:00:01,000 --> 00:00:02,000" + sep +
                        "Exactly the same text" + sep + sep +
                        "2" + sep +
                        "00:00:02,000 --> 00:00:03,000" + sep +
                        "Exactly the same text"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Exactly the same text")
                .as("The first translation source is correct");

        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Exactly the same text")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void webVttLabelsAreNotParsed() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(testFileGenerator
                .generateTestFileWithContent("labelledVttfile", ".vtt",
                        "Introduction" + sep +
                        "00:00:01.000 --> 00:00:02.000" + sep +
                        "Test subtitle 1"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineSRTAreParsedCorrectly() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(testFileGenerator
                .generateTestFileWithContent("multilinesrtfile", ".srt",
                        "1" + sep +
                        "00:00:01,000 --> 00:00:02,000" + sep +
                        "Test subtitle 1" + sep +
                        "Test subtitle 1 line 2" + sep + sep +
                        "2" + sep +
                        "00:00:03,000 --> 00:00:04,000" + sep +
                        "Test subtitle 2"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");

        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineVTTAreParsedCorrectly() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(testFileGenerator
                .generateTestFileWithContent("multilinevttfile", ".vtt",
                        "00:00:01.000 --> 00:00:02.000" + sep +
                        "Test subtitle 1" + sep +
                        "Test subtitle 1 line 2" + sep + sep +
                        "00:00:03.000 --> 00:00:04.000" + sep +
                        "Test subtitle 2"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineSBTAreParsedCorrectly() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(testFileGenerator
                .generateTestFileWithContent("multilinesbtfile", ".sbt",
                        "00:04:35.03,00:04:38.82" + sep +
                        "Test subtitle 1" + sep +
                        "Test subtitle 1 line 2" + sep + sep +
                        "2" + sep +
                        "00:04:39.03,00:04:44.82" + sep +
                        "Test subtitle 2"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineSubAreParsedCorrectly() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(testFileGenerator
                .generateTestFileWithContent("multilinesubfile", ".sub",
                        "00:04:35.03,00:04:38.82" + sep +
                        "Test subtitle 1" + sep +
                        "Test subtitle 1 line 2" + sep + sep +
                        "00:04:39.03,00:04:44.82" + sep +
                        "Test subtitle 2"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInSrtEntries() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(
                testFileGenerator.generateTestFileWithContent(
                    "formattedsrtfile",
                    ".srt",
                    "1" + sep + "00:00:01,000 --> 00:00:02,000" + sep +
                    "<b>Exactly the same text</b> {u}and more{/u}"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInVttEntries() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(
                testFileGenerator.generateTestFileWithContent(
                        "formattedvttfile",
                        ".vtt",
                        "00:00:01.000 --> 00:00:02.000" + sep +
                        "<b>Exactly the same text</b> {u}and more{/u}"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInSbtEntries() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(
                testFileGenerator.generateTestFileWithContent(
                        "formattedsbtfile",
                        ".sbt",
                        "00:04:35.03,00:04:38.82" + sep +
                        "<b>Exactly the same text</b> {u}and more{/u}"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInSubEntries() throws Exception {
        EditorPage editorPage = uploadAndGoToDocument(
                testFileGenerator.generateTestFileWithContent(
                        "formattedsubfile",
                        ".sub",
                        "00:04:35.03,00:04:38.82" + sep +
                        "<b>Exactly the same text</b> {u}and more{/u}"));

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    /*
     * Upload and open the test file in the editor for verification
     */
    private EditorPage uploadAndGoToDocument(File testFile) {
        VersionDocumentsPage versionDocumentsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("subtitle-upload")
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton()
                .enterFilePath(testFile.getAbsolutePath())
                .submitUpload()
                .clickUploadDone()
                .gotoDocumentTab();

        assertThat(versionDocumentsPage.sourceDocumentsContains(testFile
                .getName())).as("Document shows in table");

        return versionDocumentsPage
                .gotoLanguageTab()
                .translate("pl", testFile.getName());
    }

    private boolean storageIsClean(String documentStorageDirectory) {
        File testDir;
        try {
            testDir = new File(documentStorageDirectory);
            return  testDir.listFiles().equals(null) ||
                    testDir.listFiles().length == 0;
        } catch (NullPointerException npe) {
            return true;
        }
    }
}
