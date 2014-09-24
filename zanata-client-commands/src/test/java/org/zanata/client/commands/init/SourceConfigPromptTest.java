package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.URI;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ConfigurableProjectOptionsImpl;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;

public class SourceConfigPromptTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private SourceConfigPrompt prompt;
    private ConfigurableProjectOptions opts;

    @Before
    public void setUp() throws Exception {
        opts = new ConfigurableProjectOptionsImpl() {
            @Override
            public ZanataCommand initCommand() {
                return null;
            }

            @Override
            public String getCommandName() {
                return "testCommand";
            }

            @Override
            public String getCommandDescription() {
                return "test command";
            }
        };
        opts.setUrl(new URI("http://localhost:1234").toURL());
        opts.setUsername("admin");
        opts.setKey("abc");
        prompt = null;
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

        assertThat(new File(folder, "messages.properties").createNewFile(),
                Matchers.is(true));
        assertThat(
                new File(folder, "shouldBeExcluded.properties").createNewFile(),
                Matchers.is(true));

        prompt = new SourceConfigPrompt(console, opts);

        prompt = prompt.promptUser();

        assertThat(opts.getSrcDir(),
                Matchers.equalTo(new File(expectedSrcDir)));

        assertThat(prompt.getIncludes(),
                Matchers.equalTo("messages.properties"));
        assertThat(prompt.getExcludes(), Matchers.equalTo("*Excluded.properties"));
        assertThat(opts.getIncludes(),
                Matchers.contains("messages.properties"));
        assertThat(opts.getExcludes(), Matchers.contains("*Excluded.properties"));
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

        assertThat(opts.getSrcDir(),
                Matchers.equalTo(expectedDir));

        assertThat(prompt.getIncludes(),
                Matchers.equalTo("*.*"));
        assertThat(prompt.getExcludes(), Matchers.equalTo("a.pot"));
        assertThat(opts.getIncludes(),
                Matchers.contains("*.*"));
        assertThat(opts.getExcludes(), Matchers.contains("a.pot"));
    }
}
