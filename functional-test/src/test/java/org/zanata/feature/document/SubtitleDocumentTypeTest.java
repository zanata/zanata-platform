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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers more detailed testing of the subtitle formats
 * @see FileTypeUploadTest
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class SubtitleDocumentTypeTest extends ZanataTestCase {

    @ClassRule
    public static SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @ClassRule
    public static CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();

    private static Map<String, File> filesToTest;
    private static String sep = System.getProperty("line.separator");

    private final static String PROJECTID = "subtitle-test";
    private final static String VERSIONID = "subtitles";

    @BeforeClass
    public static void beforeClass() {
        new ZanataRestCaller().createProjectAndVersion(
                PROJECTID, VERSIONID, "file");
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
        uploadFiles();
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void similarSrtEntriesAreIndividual() throws Exception {
        EditorPage editorPage = goToEditor("duplicationinsrtfile.srt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Exactly the same text")
                .as("The first translation source is correct");

        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Exactly the same text")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void webVttLabelsAreNotParsed() throws Exception {
        EditorPage editorPage = goToEditor("labelledVttfile.vtt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineSRTAreParsedCorrectly() throws Exception {
        EditorPage editorPage = goToEditor("multilinesrtfile.srt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");

        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineVTTAreParsedCorrectly() throws Exception {
        EditorPage editorPage = goToEditor("multilinevttfile.vtt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineSBTAreParsedCorrectly() throws Exception {
        EditorPage editorPage = goToEditor("multilinesbtfile.sbt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void multilineSubAreParsedCorrectly() throws Exception {
        EditorPage editorPage = goToEditor("multilinesubfile.sub");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("Test subtitle 1" + sep + "Test subtitle 1 line 2")
                .as("The first translation source is correct");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("Test subtitle 2")
                .as("The second translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInSrtEntries() throws Exception {
        EditorPage editorPage = goToEditor("formattedsrtfile.srt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInVttEntries() throws Exception {
        EditorPage editorPage = goToEditor("formattedvttfile.vtt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInSbtEntries() throws Exception {
        EditorPage editorPage = goToEditor("formattedsbtfile.sbt");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void formattingInSubEntries() throws Exception {
        EditorPage editorPage = goToEditor("formattedsubfile.sub");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("<x1/>Exactly the same text<x2/> {u}and more{/u}")
                .as("The translation source is correct");
    }

    private EditorPage goToEditor(String filename) {
        return new ProjectWorkFlow().goToProjectByName(PROJECTID)
                .gotoVersion(VERSIONID)
                .translate("pl", filesToTest.get(filename).getName());
    }

    private static void uploadFiles() {
        filesToTest = new HashMap<>();
        TestFileGenerator testFileGenerator = new TestFileGenerator();
        addTestFile("duplicationinsrtfile.srt",
                testFileGenerator.generateTestFileWithContent(
                "duplicationinsrtfile", ".srt",
                "1" + sep +
                "00:00:01,000 --> 00:00:02,000" + sep +
                "Exactly the same text" + sep + sep +
                "2" + sep +
                "00:00:02,000 --> 00:00:03,000" + sep +
                "Exactly the same text"));

        addTestFile("labelledVttfile.vtt",
                testFileGenerator.generateTestFileWithContent(
                "labelledVttfile", ".vtt",
                "Introduction" + sep +
                "00:00:01.000 --> 00:00:02.000" + sep +
                "Test subtitle 1"));
        addTestFile("multilinesrtfile.srt",
                testFileGenerator.generateTestFileWithContent(
                "multilinesrtfile", ".srt",
                "1" + sep +
                "00:00:01,000 --> 00:00:02,000" + sep +
                "Test subtitle 1" + sep +
                "Test subtitle 1 line 2" + sep + sep +
                "2" + sep +
                "00:00:03,000 --> 00:00:04,000" + sep +
                "Test subtitle 2"));
        addTestFile("multilinevttfile.vtt",
                testFileGenerator.generateTestFileWithContent(
                "multilinevttfile", ".vtt",
                "00:00:01.000 --> 00:00:02.000" + sep +
                "Test subtitle 1" + sep +
                "Test subtitle 1 line 2" + sep + sep +
                "00:00:03.000 --> 00:00:04.000" + sep +
                "Test subtitle 2"));
        addTestFile("multilinesbtfile.sbt",
                testFileGenerator.generateTestFileWithContent(
                "multilinesbtfile", ".sbt",
                "00:04:35.03,00:04:38.82" + sep +
                "Test subtitle 1" + sep +
                "Test subtitle 1 line 2" + sep + sep +
                "2" + sep +
                "00:04:39.03,00:04:44.82" + sep +
                "Test subtitle 2"));
        addTestFile("multilinesubfile.sub",
                testFileGenerator.generateTestFileWithContent(
                "multilinesubfile", ".sub",
                "00:04:35.03,00:04:38.82" + sep +
                "Test subtitle 1" + sep +
                "Test subtitle 1 line 2" + sep + sep +
                "00:04:39.03,00:04:44.82" + sep +
                "Test subtitle 2"));
        addTestFile("formattedsrtfile.srt",
                testFileGenerator.generateTestFileWithContent(
                "formattedsrtfile",
                ".srt",
                "1" + sep + "00:00:01,000 --> 00:00:02,000" + sep +
                "<b>Exactly the same text</b> {u}and more{/u}"));
        addTestFile("formattedvttfile.vtt",
                testFileGenerator.generateTestFileWithContent(
                "formattedvttfile",
                ".vtt",
                "00:00:01.000 --> 00:00:02.000" + sep +
                "<b>Exactly the same text</b> {u}and more{/u}"));
        addTestFile("formattedsbtfile.sbt",
                testFileGenerator.generateTestFileWithContent(
                "formattedsbtfile",
                ".sbt",
                "00:04:35.03,00:04:38.82" + sep +
                "<b>Exactly the same text</b> {u}and more{/u}"));
        addTestFile("formattedsubfile.sub",
                testFileGenerator.generateTestFileWithContent(
                "formattedsubfile", ".sub",
                "00:04:35.03,00:04:38.82" + sep +
                "<b>Exactly the same text</b> {u}and more{/u}"));
        VersionDocumentsTab versionDocumentsTab = new ProjectWorkFlow()
                .goToProjectByName(PROJECTID)
                .gotoVersion(VERSIONID)
                .gotoSettingsTab()
                .gotoSettingsDocumentsTab()
                .pressUploadFileButton();
        for (File file : filesToTest.values()) {
            versionDocumentsTab = versionDocumentsTab
                    .enterFilePath(file.getAbsolutePath());
        }
        versionDocumentsTab.submitUpload().clickUploadDone();
    }

    private static void addTestFile(String key, File testFile) {
        assertThat(filesToTest.containsKey(key))
                .isFalse()
                .as("Test file entry is unique");
        filesToTest.put(key, testFile);
        assertThat(filesToTest.get(key).exists())
                .isTrue()
                .as("File was created");
    }
}
