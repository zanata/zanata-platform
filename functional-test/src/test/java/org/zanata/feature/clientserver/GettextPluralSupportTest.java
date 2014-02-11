package org.zanata.feature.clientserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.LocaleId;
import org.zanata.feature.DetailedTest;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.page.webtrans.Plurals;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.io.Files;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This covers TCMS case <a
 * href="https://tcms.engineering.redhat.com/case/217601/">217601</a> and case
 * <a href="https://tcms.engineering.redhat.com/case/217905/">217905</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class GettextPluralSupportTest {
    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private ClientPushWorkFlow client = new ClientPushWorkFlow();
    private ZanataRestCaller restCaller;

    private File tempDir = Files.createTempDir();

    private String userConfigPath = ClientPushWorkFlow
            .getUserConfigPath("admin");
    private File projectRootPath;

    @Before
    public void setUp() throws IOException {
        projectRootPath = client.getProjectRootPath("plural");
        Files.copy(new File(projectRootPath, "pom.xml"), new File(tempDir,
                "pom.xml"));
        Files.copy(new File(projectRootPath, "zanata.xml"), new File(tempDir,
                "zanata.xml"));
        File potDir = new File(tempDir, "pot");
        potDir.mkdirs();
        File plDir = new File(tempDir, "pl");
        plDir.mkdirs();
        Files.copy(new File(projectRootPath + "/pot", "test.pot"), new File(
                potDir, "test.pot"));
        Files.copy(new File(projectRootPath + "/pl", "test.po"), new File(
                plDir, "test.po"));
        restCaller = new ZanataRestCaller();
    }

    @Test
    public void canPushAndPullPlural() throws IOException {
        restCaller.createProjectAndVersion("plurals", "master", "podir");
        List<String> output =
                client.callWithTimeout(tempDir,
                        "mvn -B zanata:push -Dzanata.pushType=both -Dzanata.userConfig="
                                + userConfigPath);

        assertThat(client.isPushSuccessful(output), Matchers.is(true));

        EditorPage editorPage = verifyPluralPushedToEditor();

        File pullDir = Files.createTempDir();
        String pullDirPath = pullDir.getAbsolutePath();
        String command =
                "mvn -B zanata:pull -Dzanata.pullType=both -Dzanata.srcDir="
                        + pullDirPath + " -Dzanata.transDir=" + pullDirPath
                        + " -Dzanata.userConfig=" + userConfigPath;
        output = client.callWithTimeout(tempDir, command);
        assertThat(client.isPushSuccessful(output), Matchers.is(true));

        // source round trip
        List<TextFlow> originalTextFlows =
                getTextFlows(new File(projectRootPath + "/pot/test.pot"));
        List<TextFlow> pulledTextFlows =
                getTextFlows(new File(pullDir, "test.pot"));
        assertThat(pulledTextFlows, Matchers.equalTo(originalTextFlows));

        // translation round trip
        List<TextFlowTarget> originalTargets =
                getTextFlowTargets(new File(projectRootPath + "/pl/test.po"));
        List<TextFlowTarget> pulledTargets =
                getTextFlowTargets(new File(pullDir + "/pl/test.po"));
        assertThat(pulledTargets, Matchers.equalTo(originalTargets));

        // translate on web UI and pull again
        editorPage.setSyntaxHighlighting(false)
                .translateTargetAtRowIndex(0, "one aoeuaouaou")
                .saveAsFuzzyAtRow(0);


        client.callWithTimeout(tempDir, command);
        List<String> newContents =
                getTextFlowTargets(new File(pullDir + "/pl/test.po")).get(0)
                        .getContents();
        assertThat(newContents, Matchers.hasItem("one aoeuaouaou"));

    }

    private static EditorPage verifyPluralPushedToEditor() {
        // verify first message
        new LoginWorkFlow().signIn("admin", "admin");
        EditorPage editorPage =
                new BasicWorkFlow().goToPage(String.format(
                        BasicWorkFlow.EDITOR_TEMPLATE, "plurals", "master",
                        "pl", "test"), EditorPage.class);

        assertThat(editorPage.getMessageSourceAtRowIndex(0, Plurals.SourceSingular),
                Matchers.equalTo("One file removed"));
        assertThat(editorPage.getMessageSourceAtRowIndex(0, Plurals.SourcePlural),
                Matchers.equalTo("%d files removed"));
        // nplural for Polish is 3
        assertThat(editorPage.getMessageTargetAtRowIndex(0, Plurals.TargetSingular),
                Matchers.equalTo("1 aoeuaouaou"));
        assertThat(editorPage.getMessageTargetAtRowIndex(0, Plurals.TargetPluralOne),
                Matchers.equalTo("%d aoeuaouao"));
        assertThat(editorPage.getMessageTargetAtRowIndex(0, Plurals.TargetPluralTwo),
                Matchers.equalTo(" "));

        return editorPage;
    }

    private static List<TextFlow> getTextFlows(File file)
            throws FileNotFoundException {
        return new PoReader2().extractTemplate(
                new InputSource(new FileInputStream(file)), LocaleId.EN_US,
                file.getName()).getTextFlows();
    }

    private static List<TextFlowTarget> getTextFlowTargets(File file)
            throws FileNotFoundException {
        return new PoReader2().extractTarget(
                new InputSource(new FileInputStream(file)))
                .getTextFlowTargets();
    }
}
