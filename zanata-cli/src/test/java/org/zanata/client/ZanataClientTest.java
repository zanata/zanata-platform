package org.zanata.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.kohsuke.args4j.spi.SubCommand;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.zanata.client.commands.NullAbortStrategy;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@PrepareForTest(SubCommandHandler2.class)
public class ZanataClientTest
{
   ByteArrayOutputStream out;
   ByteArrayOutputStream err;
   ZanataClient client;

   @ObjectFactory
   public IObjectFactory getObjectFactory()
   {
      return new org.powermock.modules.testng.PowerMockObjectFactory();
   }

   @BeforeMethod
   void before()
   {
      out = new ByteArrayOutputStream();
      err = new ByteArrayOutputStream();
      client = new ZanataClient(new NullAbortStrategy(), new PrintStream(out), new PrintStream(err));
   }

   @DataProvider(name = "commands")
   public static Object[][] createCommandNames() throws Exception
   {
      return toGrid(Arrays.asList(/*"help",*/ "list-remote", "pull",
            "push", "put-project", "put-user", "put-version", "stats"));
   }

   /**
    * Useful for TestNG DataProvider methods.
    * 
    * @param collection
    * @return
    * @throws Exception
    */
   private static Object[][] toGrid(Collection<?> collection) throws Exception
   {
      Object[][] result = new Object[collection.size()][1];
      int i = 0;
      for (Object obj : collection)
      {
         result[i++] = new Object[] { obj };
      }
      return result;
   }

   /**
    * The top-level command, and each subcommand, should output a Usage
    * message which includes a --help option.
    */
   private static void assertOutputIncludesUsage(ByteArrayOutputStream out)
   {
      assertThat("Missing Usage", out.toString().contains("Usage"));
      assertThat("Missing --help", out.toString().contains("--help"));
   }

   /**
    * The top-level usage should include a list of subcommands.
    * The others should not.
    * @return
    */
   private static boolean outputListsCommands(ByteArrayOutputStream out)
   {
      return out.toString().contains("Available commands:");
   }

   /**
    * zanata-cli help
    * @throws Exception
    */
   @Test
   public void testHelpCommand() throws Exception
   {
      client.processArgs("help");
      assertOutputIncludesUsage(out);
      assertThat("Usage should list subcommands", outputListsCommands(out));
   }

   /**
    * zanata-cli --help
    * @throws Exception
    */
   @Test
   public void testHelpOption() throws Exception
   {
      client.processArgs("--help");
      assertOutputIncludesUsage(out);
      assertThat("Usage should list subcommands", outputListsCommands(out));
   }

   /**
    * zanata-cli help sub-command
    * @throws Exception
    */
   @Test(dataProvider = "commands")
   public void testHelpSubCommand(String cmdName) throws Exception
   {
      client.processArgs("help", cmdName);
      assertOutputIncludesUsage(out);
      assertThat("Usage should not list subcommands", !outputListsCommands(out));
   }

   /**
    * zanata-cli sub-command --help
    * @throws Exception
    */
   @Test(dataProvider = "commands")
   public void testHelpSubOption(String cmdName) throws Exception
   {
      client.processArgs(cmdName, "--help");
      assertOutputIncludesUsage(out);
      assertThat("Usage should not list subcommands", !outputListsCommands(out));
   }

   /**
    * zanata-cli nosuchcommand
    * @throws Exception
    */
   @Test
   public void testInvalidCommand() throws Exception
   {
      client.processArgs("nosuchcommand");
      assertOutputIncludesUsage(err);
      assertThat("Usage should list subcommands", outputListsCommands(err));
   }
   
   /**
    * zanata-cli --nosuchoption
    * @throws Exception
    */
   @Test
   public void testInvalidOption() throws Exception
   {
      client.processArgs("--nosuchoption");
      assertOutputIncludesUsage(err);
      assertThat("Usage should list subcommands", outputListsCommands(err));
   }
   
   /**
    * zanata-cli sub-command --nosuchoption
    * @throws Exception
    */
   @Test(dataProvider = "commands")
   public void testInvalidSubOption(String cmdName) throws Exception
   {
      client.processArgs(cmdName, "--nosuchoption");
      assertOutputIncludesUsage(err);
      // ideally, we would get the subcommand's help, but args4j doesn't return the subcommand name when something goes wrong:
//      assertThat("Usage should not list subcommands", !outputListsCommands(err));
   }

   /**
    * zanata-cli stats --url https://zanata.example.com/ --project aproject --project-version 1.2
    * @throws Exception
    */
   @Test
   public void testStatsCommand() throws Exception
   {
      mockStatic(SubCommandHandler2.class);
      GetStatisticsOptionsImpl mockOptions = mock(GetStatisticsOptionsImpl.class);
      ZanataCommand mockCommand = mock(ZanataCommand.class);

      when(SubCommandHandler2.instantiate(
            Mockito.<SubCommandHandler2>anyObject(),
            Mockito.<SubCommand>anyObject())).
            thenReturn(mockOptions);
      when(mockOptions.initCommand()).thenReturn(mockCommand);

      String command = "stats";
      String url = "https://zanata.example.com/";
      String project = "aproject";
      String version = "1.2";
      client.processArgs(command, "--url", url, "--project", project,
            "--project-version", version);

      verify(mockOptions).setUrl(new URL(url));
      verify(mockOptions).setProj(project);
      verify(mockOptions).setProjectVersion(version);
      verify(mockCommand).run();
   }

   /**
    * zanata-cli --version
    * @throws Exception
    */
   @Test
   public void testVersionOption() throws Exception
   {
      client.processArgs("--version");
      assertThat("Version", out.toString().contains("version"));
   }

}
