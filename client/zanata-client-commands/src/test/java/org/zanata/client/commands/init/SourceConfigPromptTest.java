package org.zanata.client.commands.init;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.LocaleList;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.DocumentType;
import org.zanata.rest.client.FileResourceClient;
import org.zanata.rest.client.RestClientFactory;
import com.google.common.collect.ImmutableList;

public class SourceConfigPromptTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private SourceConfigPrompt prompt;
    private PushOptions opts;
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private FileResourceClient fileClient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        opts = new PushOptionsImpl();
        opts.setUrl(new URI("http://localhost:1234").toURL());
        opts.setUsername("admin");
        opts.setKey("abc");
        prompt = null;
        when(clientFactory.getFileResourceClient()).thenReturn(fileClient);
    }

    @Test
    public void testPromptUser() throws Exception {
        // here we use absolute path because we create temp files in there
        String expectedSrcDir = tempFolder.getRoot().getAbsolutePath() + "/resources";
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(
                        expectedSrcDir, "messages.properties",
                        "*Excluded.properties", "y");
        opts.setProj("weld");
        opts.setProjectVersion("master");
        opts.setProjectType("properties");
        opts.setLocaleMapList(new LocaleList());
        File folder = tempFolder.newFolder("resources");

        assertThat(new File(folder, "messages.properties").createNewFile()).isTrue();
        assertThat(
                new File(folder, "shouldBeExcluded.properties").createNewFile())
                .isTrue();

        prompt = new SourceConfigPrompt(console, opts);

        prompt = prompt.promptUser();

        assertThat(opts.getSrcDir()).isEqualTo(new File(expectedSrcDir));

        assertThat(prompt.getIncludes()).isEqualTo("messages.properties");
        assertThat(prompt.getExcludes()).isEqualTo("*Excluded.properties");
        assertThat(opts.getIncludes()).contains("messages.properties");
        assertThat(opts.getExcludes()).contains("*Excluded.properties");
    }

    @Test
    public void allowUserToRetry() throws Exception {
        opts.setProj("fake");
        opts.setProjectVersion("1");
        opts.setProjectType("gettext");
        File expectedDir = tempFolder.newFolder("po");

        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(
                        // first round answers ends with n(no)
                        tempFolder.getRoot().getAbsolutePath(), "", "", "n",
                        // second round answers
                        expectedDir.getAbsolutePath(), "*.*", "a.pot", "y");
        prompt = new SourceConfigPrompt(console, opts);

        prompt = prompt.promptUser();

        assertThat(opts.getSrcDir()).isEqualTo(expectedDir);

        assertThat(prompt.getIncludes()).isEqualTo("*.*");
        assertThat(prompt.getExcludes()).isEqualTo("a.pot");
        assertThat(opts.getIncludes()).contains("*.*");
        assertThat(opts.getExcludes()).contains("a.pot");
    }

    @Test
    public void canHandleFileProjectType() throws Exception {
        List<FileTypeInfo> docTypeList = ImmutableList.of(DocumentType.PLAIN_TEXT.toFileTypeInfo(), DocumentType.HTML.toFileTypeInfo());
        when(fileClient.fileTypeInfoList()).thenReturn(docTypeList);
        // here we use absolute path because we create temp files in there
        String expectedSrcDir = tempFolder.getRoot().getAbsolutePath() + "/resources";
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(
                        expectedSrcDir, "messages.md",
                        "*Excluded.txt", "PLAIN_TEXT[md;txt]", "y");
        opts.setProj("fileProject");
        opts.setProjectVersion("master");
        opts.setProjectType("file");
        opts.setLocaleMapList(new LocaleList());
        File folder = tempFolder.newFolder("resources");

        assertThat(new File(folder, "messages.md").createNewFile()).isTrue();
        assertThat(
                new File(folder, "shouldBeExcluded.txt").createNewFile()).isTrue();

        prompt = new SourceConfigPrompt(console, opts) {
            @Override
            protected RestClientFactory getClientFactory(PushOptions pushOptions) {
                return clientFactory;
            }
        };

        prompt = prompt.promptUser();

        assertThat(opts.getSrcDir()).isEqualTo(new File(expectedSrcDir));

        assertThat(prompt.getIncludes()).isEqualTo("messages.md");
        assertThat(prompt.getExcludes()).isEqualTo("*Excluded.txt");
        assertThat(opts.getIncludes()).contains("messages.md");
        assertThat(opts.getExcludes()).contains("*Excluded.txt");
    }
}
