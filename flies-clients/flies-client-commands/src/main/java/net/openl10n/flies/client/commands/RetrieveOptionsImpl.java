package net.openl10n.flies.client.commands;

public class RetrieveOptionsImpl extends ConfigurableProjectOptionsImpl
{

   @Override
   public FliesCommand initCommand()
   {
      return new RetrieveCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "retrieve";
   }

   @Override
   public String getCommandDescription()
   {
      return "Fetches translated text from Flies.";
   }

}
