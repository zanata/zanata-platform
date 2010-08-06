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

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ArgsUtil
{

   public static void processArgs(FliesCommand cmd, String[] args, GlobalOptions globals) throws IOException, JAXBException, MalformedURLException, URISyntaxException
   {
      CmdLineParser parser = new CmdLineParser(cmd);

      if (globals.getHelp())
         cmd.setHelp(true);
      if (globals.getErrors())
         cmd.setErrors(true);

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
         cmd.initConfig();
         cmd.run();
      }
      catch (Exception e)
      {
         handleException(e, cmd.getErrors());
      }
   }

   private static void printHelp(FliesCommand cmd, PrintStream output) throws IOException
   {
      output.println("Usage: " + cmd.getCommandName() + " [options]");
      output.println(cmd.getCommandDescription());
      output.println();
   }

   public static void handleException(Exception e, boolean outputErrors)
   {
      System.err.println("Execution failed: " + e.getMessage());
      if (outputErrors)
      {
         e.printStackTrace();
      }
      else
      {
         System.err.println("Use -e/--errors for full stack trace (or when reporting bugs)");
      }
      System.exit(1);
   }

}
