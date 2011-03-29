package net.openl10n.flies.client.commands;



public interface ZanataCommand
{
   /**
    * Executes the command, using the parameters which have been
    * previously set. This method must be called after initConfig().
    */
   public void run() throws Exception;

}
