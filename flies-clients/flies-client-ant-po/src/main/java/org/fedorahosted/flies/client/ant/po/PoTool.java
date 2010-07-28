package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class PoTool implements GlobalOptions
{
   private boolean help;
   private boolean errors;
   private boolean version;

   @Argument(index = 1, multiValued = true)
   private List<String> arguments = new ArrayList<String>();
   private String command;
   private CmdLineParser parser = new CmdLineParser(this);
   private LinkedHashMap<String, Class<? extends Subcommand>> commandMap = new LinkedHashMap<String, Class<? extends Subcommand>>();

   public static void main(String[] args) throws Exception
   {
      // System.out.println(Arrays.asList(args));
      PoTool tool = new PoTool();
      tool.processArgs(args);
   }

   public PoTool()
   {
      commandMap.put("putuser", PutUserTask.class);
      commandMap.put("createproj", CreateProjectTask.class);
      commandMap.put("createiter", CreateIterationTask.class);
      commandMap.put("upload", UploadPoTask.class);
      commandMap.put("download", DownloadPoTask.class);
   }

   public String getCommandName()
   {
      return "flies-publican";
   }

   public String getCommandDescription()
   {
      return "Flies command-line client";
   }

   private void processArgs(String[] args) throws Exception
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
         Class<? extends Subcommand> taskClass = commandMap.get(command);
         if (taskClass == null)
         {
            System.err.println("Unknown command '" + command + "'");
            printHelp(System.err);
            System.exit(1);
         }
         else
         {
            Subcommand task = taskClass.newInstance();
            ArgsUtil.processArgs(task, otherArgs, getGlobalOptions());
         }
      }
      catch (Exception e)
      {
         Utility.handleException(e, errors);
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
      for (String cmd : commandMap.keySet())
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
