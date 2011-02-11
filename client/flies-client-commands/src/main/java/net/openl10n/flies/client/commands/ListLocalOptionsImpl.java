package net.openl10n.flies.client.commands;

public class ListLocalOptionsImpl extends ConfigurableProjectOptionsImpl
{

   @Override
   public FliesCommand initCommand()
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
      return "Lists all local files in the project which are considered to be Flies " + "documents. These are the files which will be sent to Flies when using the " + "'publish' goal.";
   }

}
