/**
 * 
 */
package org.zanata.client.commands;

import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
   private final AppAbortStrategy abortStrategy;
   private final BasicOptions opts;
   private final CmdLineParser parser;

   public ArgsUtil(AppAbortStrategy strategy, BasicOptions opts)
   {
      this.abortStrategy = strategy;
      this.opts = opts;
      this.parser = new CmdLineParser(opts);
      try
      {
         parser.setUsageWidth(Integer.parseInt(System.getenv("COLUMNS")));
      }
      catch (Exception e)
      {
         parser.setUsageWidth(120);
      }
   }

   public void runCommand()
   {
      // while loading config, we use the global logging options
      setLogLevels(opts);

      try
      {
         if (opts instanceof ConfigurableOptions)
            OptionsUtil.applyConfigFiles((ConfigurableOptions) opts);
         // just in case the logging options were changed by a config file:
         setLogLevels(opts);
         if (opts.getErrors())
         {
            log.info("Error stacktraces are turned on.");
         }
         ZanataCommand cmd = opts.initCommand();
         cmd.run();
      }
      catch (Exception e)
      {
         handleException(e, opts.getErrors(), abortStrategy);
      }
   }

   private static void setLogLevels(BasicOptions opts)
   {
      if (opts.getDebug())
      {
         enableDebugLogging(opts);
      }
      else if (opts.getQuiet())
      {
         enableQuietLogging(opts);
      }
   }

   /**
    * Maven's --debug/-X flag sets the Maven LoggerManager to LEVEL_DEBUG. The
    * slf4j framework doesn't provide any way of doing this, so we have to go to
    * the underlying framework (assumed to be log4j).
    */
   private static void enableDebugLogging(BasicOptions opts)
   {
      setRootLoggerLevel("DEBUG", opts);
   }

   /**
    * Maven's --quiet/-q flag sets the Maven LoggerManager to LEVEL_ERROR. The
    * slf4j framework doesn't provide any way of doing this, so we have to go to
    * the underlying framework (assumed to be log4j).
    */
   private static void enableQuietLogging(BasicOptions opts)
   {
      setRootLoggerLevel("ERROR", opts);
   }

   private static void setRootLoggerLevel(String level, BasicOptions opts)
   {
      try
      {
         LogManager.getRootLogger().setLevel(Level.toLevel(level));
      }
      catch (Exception e)
      {
         System.err.println("Unable to change logging level: "+e.toString());
         if (opts.getErrors())
         {
            e.printStackTrace();
         }
      }
   }

   public void printHelp(PrintStream output, String clientName)
   {
      output.print("Usage: " + clientName + " " + opts.getCommandName());
      parser.printSingleLineUsage(output);
      output.println();
      output.println();
      output.println(opts.getCommandDescription());
      output.println();
      parser.printUsage(output);
   }

   public static void handleException(Exception e, boolean outputErrors, AppAbortStrategy abortStrategy)
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
      abortStrategy.abort(e);
   }

}
