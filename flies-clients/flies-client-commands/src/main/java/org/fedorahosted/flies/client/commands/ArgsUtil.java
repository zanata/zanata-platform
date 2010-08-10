/**
 * 
 */
package org.fedorahosted.flies.client.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ArgsUtil
{
   private static final Logger log = LoggerFactory.getLogger(ArgsUtil.class);

   public static void processArgs(FliesCommand cmd, String[] args, GlobalOptions globals) throws IOException, JAXBException, MalformedURLException, URISyntaxException
   {
      CmdLineParser parser = new CmdLineParser(cmd);

      if (globals.getDebug())
      {
         cmd.setDebug(true);
      }
      if (globals.getErrors())
      {
         cmd.setErrors(true);
      }
      if (globals.getHelp())
      {
         cmd.setHelp(true);
      }
      if (globals.getQuiet())
      {
         cmd.setQuiet(true);
      }

      try
      {
         parser.setUsageWidth(Integer.parseInt(System.getenv("COLUMNS")));
      }
      catch (Exception e)
      {
         parser.setUsageWidth(120);
      }
      try
      {
         parser.parseArgument(args);
      }
      catch (CmdLineException e)
      {
         if (!cmd.getHelp() && args.length != 0)
         {
            System.err.println(e.getMessage());
            printHelp(cmd, System.err);
            parser.printUsage(System.err);
            System.exit(1);
         }
      }

      if (cmd.getHelp() || args.length == 0)
      {
         printHelp(cmd, System.out);
         parser.printUsage(System.out);
         System.exit(0);
      }

      try
      {
         setLogLevels(globals);
         cmd.initConfig();
      }
      catch (Exception e)
      {
         handleException(e, globals.getErrors());
      }
      try
      {
         setLogLevels(cmd);
         if (cmd.getErrors())
         {
            log.info("Error stacktraces are turned on.");
         }
         cmd.run();
      }
      catch (Exception e)
      {
         handleException(e, cmd.getErrors());
      }
   }

   private static void setLogLevels(GlobalOptions opts)
   {
      if (opts.getDebug())
      {
         enableDebugLogging();
      }
      else if (opts.getQuiet())
      {
         enableQuietLogging();
      }
   }

   /**
    * Maven's --debug/-X flag sets the Maven LoggerManager to LEVEL_DEBUG. The
    * slf4j framework doesn't provide any way of doing this, so we have to go to
    * the underlying framework (assumed to be log4j).
    */
   private static void enableDebugLogging()
   {
      org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
      root.setLevel(org.apache.log4j.Level.DEBUG);
   }

   /**
    * Maven's --quiet/-q flag sets the Maven LoggerManager to LEVEL_ERROR. The
    * slf4j framework doesn't provide any way of doing this, so we have to go to
    * the underlying framework (assumed to be log4j).
    */
   private static void enableQuietLogging()
   {
      org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
      root.setLevel(org.apache.log4j.Level.ERROR);
   }

   private static void printHelp(FliesCommand cmd, PrintStream output) throws IOException
   {
      output.println("Usage: " + cmd.getCommandName() + " [options]");
      output.println(cmd.getCommandDescription());
      output.println();
   }

   public static void handleException(Exception e, boolean outputErrors)
   {
      if (outputErrors)
      {
         log.error("Execution failed: ", e);
      }
      else
      {
         log.error("Execution failed: " + e.getMessage());
         log.error("Use -e/--errors for full stack trace (or when reporting bugs)");
      }
      System.exit(1);
   }

}
