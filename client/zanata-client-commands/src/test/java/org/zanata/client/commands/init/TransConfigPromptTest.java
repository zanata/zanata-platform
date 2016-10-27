package org.zanata.client.commands.init;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.zanata.client.commands.ConfigurableProjectOptionsImpl;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;

import com.google.common.collect.Sets;

import static org.hamcrest.MatcherAssert.assertThat;

public class TransConfigPromptTest {
    private TransConfigPrompt prompt;
    private ConfigurableProjectOptionsImpl opts;

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
    public void willAskUserForTransDirAndDisplayTransFiles() throws Exception {
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(".", "y");
        opts.setProjectType("podir");
        opts.setProj("about-fedora");
        opts.setProjectVersion("master");
        // provides locale list
        LocaleList locales = new LocaleList();
        locales.add(new LocaleMapping("de"));
        opts.setLocaleMapList(locales);
        prompt =
                new TransConfigPrompt(console, opts, Sets.newHashSet("a",
                        "b"));

        prompt = prompt.promptUser();

        assertThat(opts.getTransDir(), Matchers.equalTo(new File(".")));
        List<String> capturedPrompts =
                MockConsoleInteractor.getCapturedPrompts(console);
        assertThat(capturedPrompts, Matchers.hasItems(
                "        ./de/a.po",
                "        ./de/b.po"));
    }

    @Test
    public void willMakeExampleLocaleMappingIfNoneIsProvided() throws Exception {
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(".", "y");
        opts.setProjectType("podir");
        opts.setProj("about-fedora");
        opts.setProjectVersion("master");
        // not providing a locale list
        prompt =
                new TransConfigPrompt(console, opts, Sets.newHashSet("a",
                        "b"));

        prompt = prompt.promptUser();

        assertThat(opts.getTransDir(), Matchers.equalTo(new File(".")));
        List<String> capturedPrompts =
                MockConsoleInteractor.getCapturedPrompts(console);
        assertThat(capturedPrompts, Matchers.hasItems(
                "        ./zh/a.po",
                "        ./zh/b.po"));
    }

    @Test
    public void allowUserToRetry() throws Exception {
        ConsoleInteractor console =
                MockConsoleInteractor
                        .predefineAnswers(".", "n", "po", "y");
        opts.setProjectType("podir");
        opts.setProj("about-fedora");
        opts.setProjectVersion("master");
        prompt =
                new TransConfigPrompt(console, opts, Sets.newHashSet("a",
                        "b"));

        prompt = prompt.promptUser();

        assertThat(opts.getTransDir(), Matchers.equalTo(new File("po")));
    }

    @Test
    public void canHandleFileTypeProjectAndDisplayTransFiles() throws Exception {
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(".", "y");
        opts.setProjectType("file");
        opts.setProj("about-fedora");
        opts.setProjectVersion("master");
        // provides locale list
        LocaleList locales = new LocaleList();
        locales.add(new LocaleMapping("de"));
        opts.setLocaleMapList(locales);
        prompt =
                new TransConfigPrompt(console, opts, Sets.newHashSet("a.txt",
                        "b.txt"));

        prompt = prompt.promptUser();

        assertThat(opts.getTransDir(), Matchers.equalTo(new File(".")));
        List<String> capturedPrompts =
                MockConsoleInteractor.getCapturedPrompts(console);
        assertThat(capturedPrompts, Matchers.hasItems(
                "        ./de/a.txt",
                "        ./de/b.txt"));
    }
}
