package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;

public class ProjectConfigHandlerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void willBackupExistingProjectConfig() throws Exception {
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers("y");
        InitOptions opts = new InitOptionsImpl();
        ProjectConfigHandler handler = new ProjectConfigHandler(console, opts);
        File projectConfig = tempFolder.newFile("zanata.xml");
        opts.setProjectConfig(projectConfig);

        handler.handleExistingProjectConfig();

        List<String> capturedPrompts =
                MockConsoleInteractor.getCapturedPrompts(console);
        String lastMessage = capturedPrompts.get(capturedPrompts.size() - 1);
        assertThat(lastMessage,
                containsString("Old project config has been renamed to "));

        String backupPath =
                lastMessage.replace("Old project config has been renamed to ", "");
        assertThat(new File(backupPath).exists(), is(true));
        assertThat(opts.getProj(), nullValue());
        assertThat(opts.getProjectVersion(), nullValue());
        assertThat(opts.getProjectType(), nullValue());
        assertThat(opts.getProjectConfig(), nullValue());
        assertThat(opts.getSrcDir(), nullValue());
        assertThat(opts.getTransDir(), nullValue());
        assertThat(opts.getIncludes(), emptyIterable());
        assertThat(opts.getExcludes(), emptyIterable());
    }

}
