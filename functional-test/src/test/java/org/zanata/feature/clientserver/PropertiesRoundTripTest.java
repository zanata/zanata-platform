package org.zanata.feature.clientserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.fedorahosted.openprops.Properties;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TestFileGenerator;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This will cover TCMS case <a
 * href="https://tcms.engineering.redhat.com/case/139837/">139837</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class PropertiesRoundTripTest {
    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private ClientPushWorkFlow client = new ClientPushWorkFlow();
    private ZanataRestCaller restCaller;

    private File tempDir = Files.createTempDir();

    private String userConfigPath = ClientPushWorkFlow
            .getUserConfigPath("admin");

    @Before
    public void setUp() throws IOException {
        restCaller = new ZanataRestCaller("admin");
        // generate a properties source
        Properties properties = new Properties();
        properties.setProperty("hello", "hello world");
        properties.setProperty("greeting", "this is from Huston");
        properties.setProperty("hey", "hey hey");
        File propertiesSource = new File(tempDir, "test.properties");
        properties.store(new FileWriter(propertiesSource), "comment");
        // copy a pom file
        File pluralProjectRoot = client.getProjectRootPath("plural");
        Files.copy(new File(pluralProjectRoot, "pom.xml"), new File(tempDir,
                "pom.xml"));
    }

    @Test
    public void canPushAndPullProperties() throws IOException,
            InterruptedException {
        restCaller.createProjectAndVersion("properties-test", "master",
                "properties");
        // generate a zanata.xml
        new TestFileGenerator().generateZanataXml(new File(tempDir,
                "zanata.xml"), "properties-test", "master", "properties", Lists
                .newArrayList("pl"));
        List<String> output =
                client.callWithTimeout(tempDir,
                        "mvn -B zanata:push -Dzanata.srcDir=. -Dzanata.userConfig="
                                + userConfigPath);

        assertThat(client.isPushSuccessful(output), Matchers.equalTo(true));

        EditorPage editorPage = verifyPushedToEditor()
                .setSyntaxHighlighting(false);
        editorPage =
                editorPage.translateTargetAtRowIndex(2,
                        "translation updated approved")
                        .approveSelectedTranslation();

        editorPage.translateTargetAtRowIndex(1, "translation updated fuzzy")
                .saveAsFuzzySelectedTranslation();

        output =
                client.callWithTimeout(tempDir,
                        "mvn -B zanata:pull -Dzanata.userConfig="
                                + userConfigPath);

        assertThat(client.isPushSuccessful(output), Matchers.is(true));
        File transFile = new File(tempDir, "test_pl.properties");
        assertThat(transFile.exists(), Matchers.is(true));
        Properties translations = new Properties();
        translations.load(new FileReader(transFile));
        assertThat(translations.size(), Matchers.is(1));
        assertThat(translations.getProperty("hey"),
                Matchers.equalTo("translation updated approved"));

        // change on client side
        translations.setProperty("greeting", "translation updated on client");
        translations.store(new FileWriter(transFile), null);

        // push again
        client.callWithTimeout(
                tempDir,
                "mvn -B zanata:push -Dzanata.pushType=trans -Dzanata.srcDir=. -Dzanata.userConfig="
                        + userConfigPath);

        final EditorPage editor =
                new BasicWorkFlow().goToPage(String.format(
                        BasicWorkFlow.EDITOR_TEMPLATE, "properties-test",
                        "master", "pl", "test"), EditorPage.class);
        assertThat(editor.getMessageTargetAtRowIndex(1),
                Matchers.equalTo("translation updated on client"));
    }

    private static EditorPage verifyPushedToEditor() {
        new LoginWorkFlow().signIn("admin", "admin");
        EditorPage editorPage =
                new BasicWorkFlow().goToPage(String.format(
                        BasicWorkFlow.EDITOR_TEMPLATE, "properties-test",
                        "master", "pl", "test"), EditorPage.class);

        assertThat(editorPage.getMessageSourceAtRowIndex(0),
                Matchers.equalTo("hello world"));
        assertThat(editorPage.getMessageSourceAtRowIndex(1),
                Matchers.equalTo("this is from Huston"));
        assertThat(editorPage.getMessageSourceAtRowIndex(2),
                Matchers.equalTo("hey hey"));

        return editorPage;
    }
}
