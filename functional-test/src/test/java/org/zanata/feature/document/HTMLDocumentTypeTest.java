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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projectversion.VersionDocumentsPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.CleanDocumentStorageRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.FunctionalTestHelper.assumeFalse;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class HTMLDocumentTypeTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Rule
    public CleanDocumentStorageRule documentStorageRule =
            new CleanDocumentStorageRule();

    private TestFileGenerator testFileGenerator = new TestFileGenerator();

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        String documentStorageDirectory =
                CleanDocumentStorageRule.getDocumentStoragePath()
                        .concat(File.separator).concat("documents")
                        .concat(File.separator);
        assumeFalse("", new File(documentStorageDirectory).exists());
    }

    @Test
    public void uploadHTMLFile() {
        File htmlfile =
                testFileGenerator
                        .generateTestFileWithContent("testhtmlfile", ".html",
                                "<html><title>Test content</title><br>This is <b>Bold</b> text</html>");
        String testFileName = htmlfile.getName();
        String successfullyUploaded = "Document " + testFileName + " uploaded.";
        VersionLanguagesPage projectVersionPage =
                new LoginWorkFlow().signIn("admin", "admin")
                        .goToProjects()
                        .goToProject("about fedora")
                        .gotoVersion("master")
                        .gotoSettingsTab()
                        .gotoSettingsDocumentsTab()
                        .pressUploadFileButton()
                        .enterFilePath(htmlfile.getAbsolutePath())
                        .submitUpload();
        assertThat("Document uploaded notification shows",
                projectVersionPage.getNotificationMessage(),
                Matchers.equalTo(successfullyUploaded));

        VersionDocumentsPage versionDocumentsPage =
                projectVersionPage.gotoDocumentTab();

        assertThat("Document shows in table", versionDocumentsPage
                .sourceDocumentsContains(htmlfile.getName()));

        EditorPage editorPage =
                projectVersionPage.goToProjects().goToProject("about fedora")
                        .gotoVersion("master").translate("pl", testFileName);

        assertThat("The first translation source is correct",
                editorPage.getMessageSourceAtRowIndex(0),
                Matchers.equalTo("Test content"));
        assertThat("The second translation source is correct",
                editorPage.getMessageSourceAtRowIndex(1),
                Matchers.equalTo("This is <g2>Bold</g2> text"));

    }
}
