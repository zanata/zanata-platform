package net.openl10n.flies.client.commands;



public interface FliesCommand
{
   /**
    * Executes the flies Command, using the parameters which have been
    * previously set. This method must be called after initConfig().
    */
   public void run() throws Exception;

}
