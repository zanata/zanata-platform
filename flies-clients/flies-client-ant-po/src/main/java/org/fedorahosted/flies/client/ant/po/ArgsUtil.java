/**
 * 
 */
package org.fedorahosted.flies.client.ant.po;

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
class ArgsUtil
{

   public static void processArgs(Subcommand cmd, String[] args, GlobalOptions globals) throws IOException, JAXBException, MalformedURLException, URISyntaxException
   {
      CmdLineParser parser = new CmdLineParser(cmd);

      if (globals.getHelp())
         cmd.setHelp(true);
      if (globals.getErrors())
         cmd.setErrors(true);

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
         cmd.process();
      }
      catch (Exception e)
      {
         Utility.handleException(e, cmd.getErrors());
      }
   }

   private static void printHelp(Subcommand cmd, PrintStream output) throws IOException
   {
      output.println("Usage: " + cmd.getCommandName() + " [options]");
      output.println(cmd.getCommandDescription());
      output.println();
   }

}
