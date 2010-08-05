package org.fedorahosted.flies.client;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.fedorahosted.flies.client.command.ArgsUtil;
import org.fedorahosted.flies.client.command.FliesCommand;
import org.fedorahosted.flies.client.command.GlobalOptions;
import org.fedorahosted.flies.client.command.ListRemoteCommand;
import org.fedorahosted.flies.client.command.PutProjectCommand;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class FliesClient implements GlobalOptions
{
   private boolean help;
   private boolean errors;
   private boolean version;

   @Argument(index = 1, multiValued = true)
   private final List<String> arguments = new ArrayList<String>();
   private String command;
   private final CmdLineParser parser = new CmdLineParser(this);
   private final LinkedHashMap<String, Class<? extends FliesCommand>> commandMap = new LinkedHashMap<String, Class<? extends FliesCommand>>();

   public static void main(String[] args) throws Exception
   {
      FliesClient tool = new FliesClient();
      tool.processArgs(args);
   }

   public FliesClient()
   {
      getCommandMap().put("listremote", ListRemoteCommand.class);
      getCommandMap().put("putproject", PutProjectCommand.class);
   }

   public String getCommandName()
   {
      return "flies";
   }

   public String getCommandDescription()
   {
      return "Flies command-line client";
   }

   public LinkedHashMap<String, Class<? extends FliesCommand>> getCommandMap()
   {
      return commandMap;
   }

   protected void processArgs(String[] args) throws Exception
   {
      try
      {
         parser.parseArgument(args);
      }
      catch (CmdLineException e)
      {
         if (!getHelp() && args.length != 0)
         {
            System.err.println(e.getMessage());
            printHelp(System.err);
            System.exit(1);
         }
      }
      if (getHelp() && command == null)
      {
         printHelp(System.out);
         return;
      }
      if (version)
      {
         Utility.printJarVersion(System.out);
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
         printHelp(System.out);
         return;
      }
      String[] otherArgs = arguments.toArray(new String[0]);
      try
      {
         Class<? extends FliesCommand> taskClass = getCommandMap().get(command);
         if (taskClass == null)
         {
            System.err.println("Unknown command '" + command + "'");
            printHelp(System.err);
            System.exit(1);
         }
         else
         {
            FliesCommand task = taskClass.newInstance();
            ArgsUtil.processArgs(task, otherArgs, getGlobalOptions());
         }
      }
      catch (Exception e)
      {
         ArgsUtil.handleException(e, errors);
      }
   }

   private void printHelp(PrintStream out) throws IOException
   {
      out.println("Usage: " + getCommandName() + " [OPTION]... <command> [COMMANDOPTION]...");
      out.println(getCommandDescription());
      out.println();
      parser.printUsage(out);
      out.println();
      out.println("Type '" + getCommandName() + " help <command>' for help on a specific command.");
      out.println();
      out.println("Available commands:");
      for (String cmd : getCommandMap().keySet())
      {
         out.println("  " + cmd);
      }
   }

   private GlobalOptions getGlobalOptions()
   {
      return this;
   }

   @Override
   public boolean getHelp()
   {
      return help;
   }

   @Option(name = "--help", aliases = { "-h", "-help" }, usage = "Display this help and exit")
   public void setHelp(boolean help)
   {
      this.help = help;
      parser.stopOptionParsing(); // no point in validating other options now
   }

   @Override
   public boolean getErrors()
   {
      return errors;
   }

   @Option(name = "--errors", aliases = { "-e" }, usage = "Output full execution error messages")
   public void setErrors(boolean exceptionTrace)
   {
      this.errors = exceptionTrace;
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
      parser.stopOptionParsing(); // save remaining options for the subcommand's
                                  // parser
   }

}
