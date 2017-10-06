package org.zanata.client.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;

import org.fedorahosted.openprops.Properties;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.client.commands.Messages.get;

public class UpdateCheckerTest {
    private final DateTimeFormatter dateFormat =
            DateTimeFormat.forPattern("yyyy-MM-dd");
    private final String currentVersion = "3.0.1";
    @Rule
    public TemporaryFolder tempFoler = new TemporaryFolder();
    private UpdateChecker checker;

    private Connection connection;
    @Mock
    private ConsoleInteractor mockConsole;
    @Captor
    private ArgumentCaptor<String> outputStringCaptor;
    @Captor
    private ArgumentCaptor<Object> outputArgsCaptor;
    private File marker;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        File configFolder = tempFoler.newFolder();

        marker = new File(configFolder, "zanata-client-update");
        checker = new UpdateChecker("http://localhost", marker,
                mockConsole,
                currentVersion);
    }

    @After
    public void cleanUp() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void noMarkerFileYetWillCreateFileAndCheckUpdate() throws Exception {
        boolean result = checker.needToCheckUpdates(true);
        assertThat(result).isTrue();

        assertThat(marker.exists()).isTrue();
        Properties properties = new Properties();
        properties.load(new FileReader(marker));
        String today = dateFormat.print(new DateTime());

        assertThat(properties.getProperty("lastChecked")).isEqualTo(today);
        assertThat(properties.getProperty("frequency")).isEqualTo("weekly");
        assertThat(properties.getComment("frequency")).isEqualTo(
                get("valid.frequency"));
    }
    @Test
    public void willNotCheckIfUserSaysNo() throws Exception {
        String sevenDaysAgo = dateFormat.print(new DateTime().minusDays(7));
        writeLinesToMarkerFile("lastChecked=" + sevenDaysAgo, "noAsking=false");
        ConsoleInteractor console = MockConsoleInteractor.predefineAnswers("n");
        checker = new UpdateChecker("localhost", marker, console, "3.3.3");

        boolean result = checker.needToCheckUpdates(true);
        assertThat(result).isFalse();
    }

    @Test
    public void byDefaultMarkerFileHasLastCheckedDateOverOneWeekWillCheck()
            throws Exception {
        String sevenDaysAgo = dateFormat.print(new DateTime().minusDays(7));
        writeLinesToMarkerFile("lastChecked=" + sevenDaysAgo);

        when(mockConsole.expectAnswerWithRetry(ConsoleInteractor.AnswerValidator.YES_NO)).thenReturn("y");
        boolean result = checker.needToCheckUpdates(true);
        assertThat(result).isTrue();
    }

    @Test
    public void
            byDefaultMarkerFileHasLastCheckedDateWithinOneWeekWillNotCheck()
                    throws Exception {
        String twoDaysAgo = dateFormat.print(new DateTime().minusDays(2));
        writeLinesToMarkerFile("lastChecked=" + twoDaysAgo);

        boolean result = checker.needToCheckUpdates(true);
        assertThat(result).isFalse();
    }

    @Test
    public void canConfigureCheckFrequency() throws Exception {
        String twoDaysAgo = dateFormat.print(new DateTime().minusDays(2));
        writeLinesToMarkerFile("lastChecked=" + twoDaysAgo, "frequency=daily");
        when(mockConsole.expectAnswerWithRetry(ConsoleInteractor.AnswerValidator.YES_NO)).thenReturn("y");

        boolean result = checker.needToCheckUpdates(true);
        assertThat(result).isTrue();
    }

    private void writeLinesToMarkerFile(String... lines)
            throws FileNotFoundException {
        PrintWriter writer =
                new PrintWriter(Files.newWriter(marker, Charsets.UTF_8));
        for (String line : lines) {
            writer.println(line);
        }
        writer.close();
    }

    @Test
    public void canCheckForUpdates() throws Exception {
        HTTPMockContainer container =
                HTTPMockContainer.Builder
                        .builder()
                        .onPathReturnOk(
                                "/artifact/maven/resolve",
                                // simplified response
                                "<artifact-resolution><version>3.3.2</version></artifact-resolution>")
                        .build();
        String url = startMockServer(container);
        // comment above line and uncomment below will talk to real server
        // String url = "https://oss.sonatype.org/service/local/";

        String sevenDaysAgo = dateFormat.print(new DateTime().minusDays(7));
        writeLinesToMarkerFile("lastChecked=" + sevenDaysAgo);

        checker = new UpdateChecker(url, marker, mockConsole, currentVersion);
        checker.checkNewerVersion();
        String expectedString = Messages.get("suggest.update");

        verify(mockConsole, atLeastOnce()).printfln(
                outputStringCaptor.capture(), outputArgsCaptor.capture());
        List<String> allValues = outputStringCaptor.getAllValues();
        String lastMessage = allValues.get(allValues.size() - 1);
        assertThat(lastMessage).isEqualTo(expectedString);
        assertThat(outputArgsCaptor.getAllValues()).contains("3.3.2");

        Properties props = new Properties();
        props.load(new FileReader(marker));
        assertThat(props.getProperty("lastChecked"))
                .isEqualTo(dateFormat.print(new DateTime()));

    }
    private String startMockServer(Container container) throws IOException {
        ContainerServer server = new ContainerServer(container);
        connection = new SocketConnection(server);
        InetSocketAddress address = (InetSocketAddress) connection
                .connect(new InetSocketAddress(0));
        int port = address.getPort();
        return "http://localhost:" + port + "/";
    }

}
