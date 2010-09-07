package net.openl10n.flies.client;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;

import net.openl10n.flies.client.FliesClient;
import net.openl10n.flies.client.commands.FliesCommand;
import net.openl10n.flies.client.commands.RuntimeExceptionStrategy;

import org.kohsuke.args4j.CmdLineParser;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class TestFliesClient
{
   static FliesClient client = new FliesClient(new RuntimeExceptionStrategy(), nullPrintStream(), nullPrintStream());
   static LinkedHashMap<String, Class<? extends FliesCommand>> commandMap = client.getCommandMap();

   static PrintStream nullPrintStream()
   {
      return new PrintStream(new ByteArrayOutputStream());
   }

   @DataProvider(name = "commandClasses")
   public static Object[][] createCommandClasses() throws Exception
   {
      // return toGrid(/* commandMap.keySet(), */commandMap.values());
      return toTupleGrid(commandMap.values());
   }

   /**
    * Takes corresponding elements from each collection and puts them into
    * arrays. Useful for TestNG DataProvider methods.
    * 
    * @param collections
    * @return
    */
   private static Object[][] toTupleGrid(Collection<?>... collections)
   {
      if (collections.length == 0)
         return new Object[0][0];
      Object[][] result = new Object[collections[0].size()][collections.length];
      for (int i = 0; i < collections[0].size(); i++)
      {
         result[i] = new Object[collections.length];
      }
      for (int i = 0; i < collections.length; i++)
      {
         Collection<?> collection = collections[i];
         int j = 0;
         for (Object el : collection)
         {
            result[j++][i] = el;
         }
      }
      return result;
   }

   @DataProvider(name = "commands")
   public static Object[][] createCommands() throws Exception
   {
      return toGridOfInstances(commandMap.values());
   }

   /**
    * Takes a collection of classes: each class is instantiated, and the
    * resulting object is placed in an array[1]. Useful for TestNG DataProvider
    * methods.
    * 
    * @param <C>
    * @param collection
    * @return
    * @throws Exception
    */
   private static <C extends Class<?>> Object[][] toGridOfInstances(Collection<C> collection) throws Exception
   {
      Object[][] result = new Object[collection.size()][1];
      int i = 0;
      for (C clazz : collection)
      {
         result[i++] = new Object[] { clazz.newInstance() };
      }
      return result;
   }

   public TestFliesClient() throws Exception
   {
   }

   @Test(dataProvider = "commands")
   public void testForAmbiguousOptions(FliesCommand cmd) throws Exception
   {
      new CmdLineParser(cmd);
   }

   @Test(dataProvider = "commands")
   public void testHelpCommand(FliesCommand cmd) throws Exception
   {
      client.processArgs("help", cmd.getCommandName());
   }

   @Test(dataProvider = "commands")
   public void testHelpOption(FliesCommand cmd) throws Exception
   {
      client.processArgs(cmd.getCommandName(), "--help");
   }

   @Test(dataProvider = "commands")
   public void testNoArgs(FliesCommand cmd) throws Exception
   {
      client.processArgs(cmd.getCommandName());
   }
}
