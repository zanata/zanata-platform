package org.zanata.client;

import java.io.StringWriter;
import java.net.URL;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.spi.SubCommand;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zanata.client.commands.NullAbortStrategy;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@PrepareForTest(SubCommandHandler2.class)
@PowerMockIgnore({ "com.sun.*", "org.apache.log4j.*", "org.xml.*", "javax.xml.*" })
@RunWith(PowerMockRunner.class)
public class ZanataClientTest {
    @Rule
    public ParameterRule<String> cmdNameRule = new ParameterRule<String>(
            "list-remote", "pull", "push", "put-project", "put-user",
            "put-version", "stats");

    private StringWriter out;
    private StringWriter err;
    private ZanataClient client;

    @Before
    public void before() {
        out = new StringWriter();
        err = new StringWriter();
        client =
                new ZanataClient(new NullAbortStrategy(), out, err);
    }

    /**
     * The top-level command, and each subcommand, should output a Usage message
     * which includes a --help option.
     */
    private static void assertOutputIncludesUsage(StringWriter out) {
        assertThat("Missing Usage", out.toString().contains("Usage"));
        assertThat("Missing --help", out.toString().contains("--help"));
    }

    /**
     * The top-level usage should include a list of subcommands. The others
     * should not.
     *
     * @return
     */
    private static boolean outputListsCommands(StringWriter out) {
        return out.toString().contains("Available commands:");
    }

    /**
     * zanata-cli help
     *
     * @throws Exception
     */
    @Test
    public void testHelpCommand() throws Exception {
        client.processArgs("help");
        assertOutputIncludesUsage(out);
        assertThat("Usage should list subcommands", outputListsCommands(out));
    }

    /**
     * zanata-cli --help
     *
     * @throws Exception
     */
    @Test
    public void testHelpOption() throws Exception {
        client.processArgs("--help");
        assertOutputIncludesUsage(out);
        assertThat("Usage should list subcommands", outputListsCommands(out));
    }

    /**
     * zanata-cli help sub-command
     *
     * @throws Exception
     */
    @Test
    public void testHelpSubCommand() throws Exception {
        client.processArgs("help", cmdNameRule.getParameter());
        assertOutputIncludesUsage(out);
        assertThat("Usage should not list subcommands",
                !outputListsCommands(out));
    }

    /**
     * zanata-cli sub-command --help
     *
     * @throws Exception
     */
    @Test
    public void testHelpSubOption() throws Exception {
        client.processArgs(cmdNameRule.getParameter(), "--help");
        assertOutputIncludesUsage(out);
        assertThat("Usage should not list subcommands",
                !outputListsCommands(out));
    }

    /**
     * zanata-cli help nosuchcommand
     *
     * @throws Exception
     */
    @Test
    public void testHelpInvalidSubOption() throws Exception {
        client.processArgs("help", "nosuchcommand");
        assertOutputIncludesUsage(out);
        assertThat("Usage should list subcommands", outputListsCommands(out));
    }

    /**
     * zanata-cli nosuchcommand
     *
     * @throws Exception
     */
    @Test
    public void testInvalidCommand() throws Exception {
        client.processArgs("nosuchcommand");
        assertOutputIncludesUsage(err);
        assertThat("Usage should list subcommands", outputListsCommands(err));
    }

    /**
     * zanata-cli --nosuchoption
     *
     * @throws Exception
     */
    @Test
    public void testInvalidOption() throws Exception {
        client.processArgs("--nosuchoption");
        assertOutputIncludesUsage(err);
        assertThat("Usage should list subcommands", outputListsCommands(err));
    }

    /**
     * zanata-cli sub-command --nosuchoption
     *
     * @throws Exception
     */
    @Test
    public void testInvalidSubOption() throws Exception {
        client.processArgs(cmdNameRule.getParameter(), "--nosuchoption");
        assertOutputIncludesUsage(err);
        // ideally, we would get the subcommand's help, but args4j doesn't
        // return the subcommand name when something goes wrong:
        // assertThat("Usage should not list subcommands",
        // !outputListsCommands(err));
    }

    /**
     * zanata-cli stats --url https://zanata.example.com/ --project aproject
     * --project-version 1.2
     *
     * @throws Exception
     */
    @Test
    public void testStatsCommand() throws Exception {
        mockStatic(SubCommandHandler2.class);
        GetStatisticsOptionsImpl mockOptions =
                mock(GetStatisticsOptionsImpl.class);
        ZanataCommand mockCommand = mock(ZanataCommand.class);

        when(
                SubCommandHandler2.instantiate(
                        Mockito.<SubCommandHandler2> anyObject(),
                        Mockito.<SubCommand> anyObject())).thenReturn(
                mockOptions);
        when(mockOptions.initCommand()).thenReturn(mockCommand);

        String command = "stats";
        String url = "https://zanata.example.com/";
        String project = "aproject";
        String version = "1.2";
        client.processArgs(command, "--url", url, "--project", project,
                "--project-version", version);

        assertThat(err.toString(), CoreMatchers.is(""));
        verify(mockOptions).setUrl(new URL(url));
        verify(mockOptions).setProj(project);
        verify(mockOptions).setProjectVersion(version);
        verify(mockCommand).runWithActions();
    }

    /**
     * zanata-cli --version
     *
     * @throws Exception
     */
    @Test
    public void testVersionOption() throws Exception {
        client.processArgs("--version");
        assertThat("Version", out.toString().contains("version"));
    }

}
