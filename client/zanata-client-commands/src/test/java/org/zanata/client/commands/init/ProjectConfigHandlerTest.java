package org.zanata.client.commands.init;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(lastMessage).contains("Old project config has been renamed to ");

        String backupPath =
                lastMessage.replace("Old project config has been renamed to ", "");
        assertThat(new File(backupPath).exists()).isTrue();
        assertThat(opts.getProj()).isNull();
        assertThat(opts.getProjectVersion()).isNull();
        assertThat(opts.getProjectType()).isNull();
        assertThat(opts.getProjectConfig()).isNull();
        assertThat(opts.getSrcDir()).isNull();
        assertThat(opts.getTransDir()).isNull();
        assertThat(opts.getIncludes()).isEmpty();
        assertThat(opts.getExcludes()).isEmpty();
    }

}
