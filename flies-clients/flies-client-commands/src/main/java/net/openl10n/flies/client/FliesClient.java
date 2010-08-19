package net.openl10n.flies.client;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.openl10n.flies.client.commands.AppAbortStrategy;
import net.openl10n.flies.client.commands.ArgsUtil;
import net.openl10n.flies.client.commands.BasicOptions;
import net.openl10n.flies.client.commands.FliesCommand;
import net.openl10n.flies.client.commands.ListRemoteCommand;
import net.openl10n.flies.client.commands.PutProjectCommand;
import net.openl10n.flies.client.commands.PutUserCommand;
import net.openl10n.flies.client.commands.PutVersionCommand;
import net.openl10n.flies.client.commands.SystemExitStrategy;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class FliesClient implements BasicOptions
{
   private String command;
   private boolean debug;
   private boolean errors;
   private boolean help;
   private boolean quiet;
   private boolean version;

   @Argument(index = 1, multiValued = true)
   private final List<String> arguments = new ArrayList<String>();
   private final CmdLineParser parser = new CmdLineParser(this);
   private final LinkedHashMap<String, Class<? extends FliesCommand>> commandMap = createLinkedHashMap();
   private final AppAbortStrategy abortStrategy;
   private final PrintStream out;
   private final PrintStream err;

   public static void main(String[] args)
   {
      FliesClient tool = new FliesClient(new SystemExitStrategy(), System.out, System.err);
      tool.processArgs(args);
   }

   private static <K, V> LinkedHashMap<K, V> createLinkedHashMap()
   {
      return new LinkedHashMap<K, V>();
   }

   public FliesClient(AppAbortStrategy strategy, PrintStream out, PrintStream err)
   {
      this.abortStrategy = strategy;
      this.out = out;
      this.err = err;
      // getCommandMap().put("listlocal", ListLocalCommand.class);
      getCommandMap().put("listremote", ListRemoteCommand.class);
      // getCommandMap().put("publish", PublishCommand.class);
      // getCommandMap().put("retrieve", RetrieveCommand.class);
      getCommandMap().put("putproject", PutProjectCommand.class);
      getCommandMap().put("putuser", PutUserCommand.class);
      getCommandMap().put("putversion", PutVersionCommand.class);
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

   protected void processArgs(String... args)
   {
      // FIXME remove this workaround for failing test (client used in multiple
      // tests)
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
            abortStrategy.abort();
         }
      }
      if (getHelp() && command == null)
      {
         printHelp(out);
         return;
      }
      if (version)
      {
         VersionUtility.printJarVersion(out);
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
         Class<? extends FliesCommand> taskClass = getCommandMap().get(command);
         if (taskClass == null)
         {
            err.println("Unknown command '" + command + "'");
            printHelp(err);
            abortStrategy.abort();
         }
         else
         {
            FliesCommand task = taskClass.newInstance();
            ArgsUtil.processArgs(task, otherArgs, getGlobalOptions());
         }
      }
      catch (Exception e)
      {
         ArgsUtil.handleException(e, errors, abortStrategy);
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
      for (String cmd : getCommandMap().keySet())
      {
         out.println("  " + cmd);
      }
   }

   private BasicOptions getGlobalOptions()
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
   public boolean getDebug()
   {
      return debug;
   }

   @Option(name = "--debug", aliases = { "-X" }, usage = "Enable debug logging")
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   @Override
   public boolean getErrors()
   {
      return errors;
   }

   @Option(name = "--errors", aliases = { "-e" }, usage = "Output full execution error messages (stacktraces)")
   public void setErrors(boolean exceptionTrace)
   {
      this.errors = exceptionTrace;
   }

   @Override
   public boolean getQuiet()
   {
      return quiet;
   }

   @Option(name = "--quiet", aliases = { "-q" }, usage = "Quiet mode - error messages only")
   public void setQuiet(boolean quiet)
   {
      this.quiet = quiet;
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
