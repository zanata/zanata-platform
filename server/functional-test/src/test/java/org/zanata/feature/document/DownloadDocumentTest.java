package org.zanata.feature.document;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Category(TestPlan.DetailedTest.class)
public class DownloadDocumentTest {

    private final String DOWNLOADEDPO = "/tmp/About_Fedora.po";
    private final String DOWNLOADEDPOT = "/tmp/About_Fedora.pot";

    @Before
    public void before() throws Exception {
        List<File> files = new ArrayList<>();
        files.add(new File(DOWNLOADEDPO));
        files.add(new File(DOWNLOADEDPOT));
        for (File file : files) {
            if (file.exists()) {
                assertThat(file.delete()).isTrue();
            }
        }
    }

    @Test
    public void testSourceDownload() throws Exception {
        new LoginWorkFlow().signIn("admin", "admin")
                .gotoProjectsTab();
        new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .gotoDocumentTab()
                .clickDownloadPotOnDocument("About_Fedora");

        File downloadedFile = new File(DOWNLOADEDPOT);
        assertThat(downloadedFile.exists()).isTrue();
    }

    @Test
    public void testTranslationDownload() throws Exception {
        new LoginWorkFlow().signIn("admin", "admin")
                .gotoProjectsTab();
        new ProjectWorkFlow()
                .goToProjectByName("about fedora")
                .gotoVersion("master")
                .clickLocale("pl")
                .clickDownloadTranslatedPo("About_Fedora");

        File downloadedFile = new File(DOWNLOADEDPO);
        assertThat(downloadedFile.exists()).isTrue();
    }
}
