package org.zanata.client.commands.init;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.client.commands.Messages.get;

public class UserConfigHandlerTest {
    @Rule
    public ExpectedException expectException = ExpectedException.none();
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private InitOptionsImpl opts;
    private UserConfigHandler handler;
    private File userConfig;

    @Before
    public void setUp() throws IOException {
        opts = new InitOptionsImpl();
        ensureUserConfigExistsWithOneServer();
        ConsoleInteractor console = Mockito.mock(ConsoleInteractor.class);

        handler = new UserConfigHandler(console, opts);

    }

    private void ensureUserConfigExistsWithOneServer() throws IOException {
        userConfig = tempFolder.newFile("zanata.ini");
        BufferedWriter writer =
                Files.newWriter(userConfig, Charsets.UTF_8);
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println("[servers]");
        printWriter.println("a.url=http://localhost:8080/zanata/");
        printWriter.println("a.username=admin");
        printWriter.println("a.key=abcde");
        printWriter.flush();
        printWriter.close();
        opts.setUserConfig(userConfig);
    }

    @Test
    public void exitWhenThereIsNoUserConfig() throws Exception {
        expectException.expect(RuntimeException.class);
        expectException.expectMessage(get("missing.user.config"));
        opts.setUserConfig(new File("/planet/Mars/zanata.ini"));

        handler.verifyUserConfig();
    }

    @Test
    public void willExitWhenThereIsNoServerUrlInFile() throws Exception {
        expectException.expect(RuntimeException.class);
        expectException.expectMessage(get("missing.server.url"));

        // wipe contents in the file
        BufferedWriter writer =
                Files.newWriter(userConfig, Charsets.UTF_8);
        writer.write("[servers]");
        writer.close();

        handler.verifyUserConfig();
    }

    @Test
    public void willUseUserConfigIfThereIsOnlyOneServer() throws Exception {
        handler.verifyUserConfig();
        assertThat(handler.getOpts().getUrl().toString(),
                Matchers.equalTo("http://localhost:8080/zanata/"));
        assertThat(handler.getOpts().getUsername(), Matchers.equalTo("admin"));
        assertThat(handler.getOpts().getKey(), Matchers.equalTo("abcde"));
    }

    @Test
    public void willAskUserIfUserConfigHasMoreThanOneServerEntries()
            throws Exception {
        Files.append("\nb.url=https://translate.zanata.org\n", userConfig,
                Charsets.UTF_8);
        Files.append("b.username=admin\n", userConfig, Charsets.UTF_8);
        Files.append("b.key=blah\n", userConfig, Charsets.UTF_8);

        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers("2");
        handler = new UserConfigHandler(console, opts);

        handler.verifyUserConfig();
        assertThat(handler.getOpts().getUrl().toString(),
                Matchers.equalTo("https://translate.zanata.org"));
        assertThat(handler.getOpts().getUsername(), Matchers.equalTo("admin"));
        assertThat(handler.getOpts().getKey(), Matchers.equalTo("blah"));
    }

}
