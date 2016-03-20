package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.client.TestUtils.readFromClasspath;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.Messages;
import org.zanata.rest.client.ProjectIterationClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.VersionInfo;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

public class InitCommandTest {
    private static final Logger log =
            LoggerFactory.getLogger(InitCommandTest.class);

    @Rule
    public ExpectedException expectException = ExpectedException.none();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private InitCommand command;
    private InitOptionsImpl opts;
    @Mock
    private ConsoleInteractor console;
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private ProjectIterationClient projectIterationClient;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        opts = new InitOptionsImpl();
        command = new InitCommand(opts, console, clientFactory);
    }

    @Test
    public void createCommandWithoutMandatoryOptionsWillNotCauseException() {
        // we don't have server url etc yet
        command = new InitCommand(opts, console);
    }

    @Test
    public void willDownloadProjectConfigFromServer() throws IOException {
        when(clientFactory.getProjectIterationClient("gcc", "master")).thenReturn(projectIterationClient);
        when(projectIterationClient.sampleConfiguration()).thenReturn(
                readFromClasspath("serverresponse/projectConfig.xml"));

        File configFileDest = new File(tempFolder.getRoot(), "zanata.xml");
        command.downloadZanataXml("gcc", "master", configFileDest);

        assertThat(configFileDest.exists(), Matchers.is(true));
        List<String> lines = FileUtils.readLines(configFileDest, Charsets.UTF_8);
        String content = Joiner.on("\n").join(lines);
        assertThat(content, Matchers.containsString("<project>"));
        assertThat(opts.getProjectConfig(), Matchers.equalTo(configFileDest));
    }

    @Test
    public void willWriteSrcDirIncludesExcludesToConfigFile() throws Exception {
        File configFile = new File(tempFolder.getRoot(), "zanata.xml");
        configFile.createNewFile();
        FileUtils.write(configFile, readFromClasspath("serverresponse/projectConfig.xml"), Charsets.UTF_8);

        command.writeToConfig(new File("pot"), null, "", new File("po"),
                configFile);

        List<String> lines = FileUtils.readLines(configFile, Charsets.UTF_8);
        StringBuilder content = new StringBuilder();
        for (String line : lines) {
            log.debug(line);
            content.append(line.trim());
        }
        assertThat(content.toString(), Matchers.containsString(
                "<src-dir>pot</src-dir><trans-dir>po</trans-dir>"));
    }

    @Test
    public void willQuitIfServerApiVersionDoesNotSupportInit()
            throws Exception {
        expectException.expect(RuntimeException.class);
        expectException.expectMessage(Matchers.equalTo(Messages
                .get("server.incompatible")));

        when(clientFactory.getServerVersionInfo()).thenReturn(
                new VersionInfo("3.3.1", "unknown", "unknown"));
        command = new InitCommand(opts, console, clientFactory);

        command.ensureServerVersion();
    }

}
