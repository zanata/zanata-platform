package org.zanata.client;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.zanata.client.commands.AppAbortException;
import org.zanata.client.commands.AppAbortStrategy;
import org.zanata.client.commands.ArgsUtil;
import org.zanata.client.commands.BasicOptions;
import org.zanata.client.commands.BasicOptionsImpl;
import org.zanata.client.commands.ListRemoteOptionsImpl;
//import org.zanata.client.commands.PublicanPullOptionsImpl;
//import org.zanata.client.commands.PublicanPushOptionsImpl;
import org.zanata.client.commands.PutProjectOptionsImpl;
import org.zanata.client.commands.PutUserOptionsImpl;
import org.zanata.client.commands.PutVersionOptionsImpl;
import org.zanata.client.commands.SystemExitStrategy;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;
import org.zanata.util.VersionUtility;

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
   ZanataClient()
   {
      this(new SystemExitStrategy(), System.out, System.err);
   }

   public ZanataClient(AppAbortStrategy strategy, PrintStream out, PrintStream err)
   {
      this.abortStrategy = strategy;
      this.out = out;
      this.err = err;
      // addCommand(new ListLocalOptionsImpl());
      addCommand(new ListRemoteOptionsImpl());
      addCommand(new PullOptionsImpl());
      addCommand(new PushOptionsImpl());
      addCommand(new PutProjectOptionsImpl());
      addCommand(new PutUserOptionsImpl());
      addCommand(new PutVersionOptionsImpl());
      addCommand(new GetStatisticsOptionsImpl());
   }

   private void addCommand(BasicOptions opts)
   {
      getOptionsMap().put(opts.getCommandName(), opts);
   }

   public String getCommandName()
   {
      return System.getProperty("app.name", "zanata-cli");
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
            String msg = e.getMessage();
            err.println(msg);
            printHelp(err);
            abortStrategy.abort(msg);
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
            String msg = "Unknown command '" + command + "'";
            err.println(msg);
            printHelp(err);
            abortStrategy.abort(msg);
         }
         else
         {
            copyGlobalOptionsTo(options);
            new ArgsUtil(abortStrategy, out, err, getCommandName()).process(otherArgs, options);
         }
      }
      catch (AppAbortException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         ArgsUtil.handleException(e, getErrors(), abortStrategy);
      }
   }

   /**
    * @param options
    */
   private void copyGlobalOptionsTo(BasicOptions options)
   {
      options.setDebug(getDebug());
      options.setErrors(getErrors());
      options.setHelp(getHelp());
      options.setInteractiveMode(isInteractiveMode());
      options.setQuiet(getQuiet());
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
