package org.zanata.client;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.zanata.client.commands.AppAbortStrategy;
import org.zanata.client.commands.ArgsUtil;
import org.zanata.client.commands.BasicOptions;
import org.zanata.client.commands.BasicOptionsImpl;
import org.zanata.client.commands.ListRemoteOptionsImpl;
import org.zanata.client.commands.PublicanPullOptionsImpl;
import org.zanata.client.commands.PublicanPushOptionsImpl;
import org.zanata.client.commands.PutProjectOptionsImpl;
import org.zanata.client.commands.PutUserOptionsImpl;
import org.zanata.client.commands.PutVersionOptionsImpl;
import org.zanata.client.commands.SystemExitStrategy;
import org.zanata.client.commands.ZanataCommand;

@SuppressWarnings("deprecation")
public class ZanataClient extends BasicOptionsImpl
{
   private String command;
   private boolean version;

   @Argument(index = 1, multiValued = true)
   private final List<String> arguments = new ArrayList<String>();
   private final CmdLineParser parser = new CmdLineParser(this);
   private final LinkedHashMap<String, BasicOptions> optionsMap = new LinkedHashMap<String, BasicOptions>();
   private final AppAbortStrategy abortStrategy;
   private final PrintStream out;
   private final PrintStream err;

   public static void main(String[] args)
   {
      ZanataClient tool = new ZanataClient();
      tool.processArgs(args);
   }

   @Override
   public ZanataCommand initCommand()
   {
      return null;
   }

   /**
    * Only for testing (allows access to optionsMap)
    */
   public ZanataClient()
   {
      this(new SystemExitStrategy(), System.out, System.err);
   }

   public ZanataClient(AppAbortStrategy strategy, PrintStream out, PrintStream err)
   {
      this.abortStrategy = strategy;
      this.out = out;
      this.err = err;
      // getOptionsMap().put("listlocal", new ListLocalOptionsImpl());
      getOptionsMap().put("listremote", new ListRemoteOptionsImpl());
      getOptionsMap().put("publican-push", new PublicanPushOptionsImpl());
      getOptionsMap().put("publican-pull", new PublicanPullOptionsImpl());
      getOptionsMap().put("putproject", new PutProjectOptionsImpl());
      getOptionsMap().put("putuser", new PutUserOptionsImpl());
      getOptionsMap().put("putversion", new PutVersionOptionsImpl());
   }

   public String getCommandName()
   {
      return "zanataj";
   }

   public String getCommandDescription()
   {
      return "Zanata Java command-line client";
   }

   protected LinkedHashMap<String, BasicOptions> getOptionsMap()
   {
      return optionsMap;
   }

   protected void processArgs(String... args)
   {
      // workaround for failing test (client used in multiple tests)
      arguments.clear();
      try
      {
         parser.parseArgument(args);
      }
      catch (CmdLineException e)
      {
         if (!getHelp() && args.length != 0)
         {
            err.println(e.getMessage());
            printHelp(err);
            abortStrategy.abort(null);
         }
      }
      if (getHelp() && command == null)
      {
         printHelp(out);
         return;
      }
      if (version)
      {
         out.println(getCommandName());
         VersionUtility.printVersions(ZanataClient.class, out);
         return;
      }
      if ("help".equals(command))
      {
         setHelp(true);
         command = null;
         if (arguments.size() != 0)
            command = arguments.remove(0);
      }
      if (command == null)
      {
         printHelp(out);
         return;
      }
      String[] otherArgs = arguments.toArray(new String[0]);
      try
      {
         BasicOptions options = getOptionsMap().get(command);
         if (options == null)
         {
            err.println("Unknown command '" + command + "'");
            printHelp(err);
            abortStrategy.abort(null);
         }
         else
         {
            new ArgsUtil(abortStrategy, out, err).process(otherArgs, options);
         }
      }
      catch (Exception e)
      {
         ArgsUtil.handleException(e, getErrors(), abortStrategy);
      }
   }

   private void printHelp(PrintStream out)
   {
      out.println("Usage: " + getCommandName() + " [OPTION]... <command> [COMMANDOPTION]...");
      out.println(getCommandDescription());
      out.println();
      parser.printUsage(out);
      out.println();
      out.println("Type '" + getCommandName() + " help <command>' for help on a specific command.");
      out.println();
      out.println("Available commands:");
      for (String cmd : getOptionsMap().keySet())
      {
         out.println("  " + cmd);
      }
   }

   @Option(name = "--version", aliases = { "-v" }, usage = "Output version information and exit")
   public void setVersion(boolean version)
   {
      this.version = version;
   }

   @Argument(index = 0, usage = "Command name", metaVar = "<command>")
   public void setCommand(String command)
   {
      this.command = command;
      // save remaining options for the subcommand's parser
      parser.stopOptionParsing();
   }

}
