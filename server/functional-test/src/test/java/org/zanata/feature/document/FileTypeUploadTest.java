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
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.VersionDocumentsPage;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Theories.class)
@Category(DetailedTest.class)
public class FileTypeUploadTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FileTypeUploadTest.class);

    @ClassRule
    public static CleanDocumentStorageRule documentStorageRule;

    @Before
    public void before() {
        documentStorageRule = new CleanDocumentStorageRule();
        new ZanataRestCaller().createProjectAndVersion("doctype-test",
                "doctype-upload", "File");
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin").as("Admin is logged in");
    }

    private static String testString = "Test text 1";
    private static String htmlString =
            "<html><title>" + testString + "</title><body/> </html>";
    private static String qtTsString =
            "<!DOCTYPE TS []><TS><context><name>Test</name><message><source>"
                    + testString
                    + "</source><translation>Teststring1</translation></message></context></TS>";
    @DataPoint
    public static File TXT_FILE = new TestFileGenerator()
            .generateTestFileWithContent("testtxtfile", ".txt", testString);
    @DataPoint
    public static File DTD_FILE =
            new TestFileGenerator().generateTestFileWithContent("testdtdfile",
                    ".dtd", "<!ENTITY firstField \"" + testString + "\">");
    @DataPoint
    public static File SRT_FILE = new TestFileGenerator()
            .generateTestFileWithContent("testsrtfile", ".srt", "1" + sep()
                    + "00:00:01,000 --> 00:00:02,000" + sep() + testString);
    @DataPoint
    public static File WEBVTT_FILE =
            new TestFileGenerator().generateTestFileWithContent("testvttfile",
                    ".vtt", "00:01.000 --> 00:01.000" + sep() + testString);
    @DataPoint
    public static File SBT_FILE =
            new TestFileGenerator().generateTestFileWithContent("testsbtfile",
                    ".sbt", "00:04:35.03,00:04:38.82" + sep() + testString);
    @DataPoint
    public static File SUB_FILE =
            new TestFileGenerator().generateTestFileWithContent("testsubfile",
                    ".sub", "00:04:35.03,00:04:38.82" + sep() + testString);
    @DataPoint
    public static File HTM_FILE = new TestFileGenerator()
            .generateTestFileWithContent("testhtmfile", ".htm", htmlString);
    @DataPoint
    public static File HTML_FILE = new TestFileGenerator()
            .generateTestFileWithContent("testhtmlfile", ".html", htmlString);
    @DataPoint
    public static File QTTS_FILE = new TestFileGenerator()
            .generateTestFileWithContent("testtsfile", ".ts", qtTsString);
    @DataPoint
    public static File IDML_FILE =
            new TestFileGenerator().openTestFile("upload-idml.idml");
    @DataPoint
    public static File ODT_FILE =
            new TestFileGenerator().openTestFile("upload-odt.odt");
    @DataPoint
    public static File ODS_FILE =
            new TestFileGenerator().openTestFile("upload-ods.ods");
    @DataPoint
    public static File ODG_FILE =
            new TestFileGenerator().openTestFile("upload-odg.odg");
    @DataPoint
    public static File ODP_FILE =
            new TestFileGenerator().openTestFile("upload-odp.odp");

    @Theory
    @Feature(bugzilla = 980670,
            summary = "The administrator can upload raw files for translation",
            tcmsTestCaseIds = { 377743 }, tcmsTestPlanIds = { 5316 })
    public void uploadFileTypeDocument(File testFile) throws Exception {
        String testFileName = testFile.getName();
        log.info("[uploadFile] " + testFileName);
        VersionDocumentsPage versionDocumentsPage =
                new ProjectWorkFlow().goToProjectByName("doctype-test")
                        .gotoVersion("doctype-upload").gotoSettingsTab()
                        .gotoSettingsDocumentsTab().pressUploadFileButton()
                        .enterFilePath(testFile.getAbsolutePath())
                        .submitUpload().clickUploadDone().gotoDocumentTab();
        assertThat(versionDocumentsPage.expectSourceDocsContains(testFileName))
                .as("Document shows in table");
    }

    private static String sep() {
        return System.getProperty("line.separator");
    }
}
