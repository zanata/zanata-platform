package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.client.commands.HTTPMockContainer.Builder.readFromClasspath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.HTTPMockContainer;
import org.zanata.client.commands.Messages;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.VersionInfo;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

public class InitCommandTest {
    private static final Logger log =
            LoggerFactory.getLogger(InitCommandTest.class);

    @Rule
    public ExpectedException expectException = ExpectedException.none();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private InitCommand command;
    private InitOptionsImpl opts;
    private SocketConnection connection;
    @Mock
    private ConsoleInteractor console;
    @Mock
    private ZanataProxyFactory requestFactory;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        opts = new InitOptionsImpl();
        command = new InitCommand(opts, console, null);
    }

    @After
    public void cleanUp() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    private void startMockServer(Container container) throws IOException {
        ContainerServer server = new ContainerServer(container);
        connection = new SocketConnection(server);
        InetSocketAddress address = (InetSocketAddress) connection
                .connect(new InetSocketAddress(0));
        int port = address.getPort();

        opts.setUrl(URI.create("http://localhost:" + port).toURL());
        opts.setUsername("admin");
        opts.setKey("abcde");
        opts.setLogHttp(false);
    }

    @Test
    public void willDownloadProjectConfigFromServer() throws IOException {
        String configContent =
                readFromClasspath("serverresponse/projectConfig.xml");
        HTTPMockContainer container =
                HTTPMockContainer.Builder
                        .builder()
                        .onPathReturnOk(
                                Matchers.endsWith("/version"),
                                readFromClasspath("serverresponse/version.xml"))
                        .onPathReturnOk(
                                Matchers.endsWith("/config"),
                                configContent).build();
        startMockServer(container);

        File configFileDest = new File(tempFolder.getRoot(), "zanata.xml");
        command.downloadZanataXml("gcc", "master", configFileDest);

        assertThat(configFileDest.exists(), Matchers.is(true));
        List<String> lines = Files.readLines(configFileDest, Charsets.UTF_8);
        String content = Joiner.on("\n").join(lines);
        assertThat(content, Matchers.equalTo(configContent));
        assertThat(opts.getProjectConfig(), Matchers.equalTo(configFileDest));
    }

    @Test
    public void willWriteSrcDirIncludesExcludesToConfigFile() throws Exception {
        File configFile = new File(tempFolder.getRoot(), "zanata.xml");
        configFile.createNewFile();
        BufferedWriter writer =
                Files.newWriter(configFile, Charsets.UTF_8);
        writer.write(readFromClasspath("serverresponse/projectConfig.xml"));
        writer.flush();
        writer.close();

        command.writeToConfig(new File("pot"), null, "", new File("po"),
                configFile);

        List<String> lines = Files.readLines(configFile, Charsets.UTF_8);
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
                ._("server.incompatible")));

        when(requestFactory.getServerVersionInfo()).thenReturn(
                new VersionInfo("3.3.1", "unknown", "unknown"));
        command = new InitCommand(opts, console, requestFactory);

        command.ensureServerVersion();
    }

}
