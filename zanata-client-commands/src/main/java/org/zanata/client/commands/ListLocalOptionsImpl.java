package org.zanata.client.commands;

public class ListLocalOptionsImpl extends ConfigurableProjectOptionsImpl
{

   @Override
   public ZanataCommand initCommand()
   {
      return new ListLocalCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "listlocal";
   }

   @Override
   public String getCommandDescription()
   {
      return "Lists all local files in the project which are considered to be translatable " + "documents. These are the files which will be sent to the server when using the " + "'push' goal.";
   }

}
