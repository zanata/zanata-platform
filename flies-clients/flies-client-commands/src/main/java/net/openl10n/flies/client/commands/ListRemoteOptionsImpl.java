package net.openl10n.flies.client.commands;

public class ListRemoteOptionsImpl extends ConfigurableProjectOptionsImpl
{

   @Override
   public FliesCommand initCommand()
   {
      return new ListRemoteCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "listremote";
   }

   @Override
   public String getCommandDescription()
   {
      return "Lists all remote documents in the configured Flies project version.";
   }


}
