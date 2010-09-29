package net.openl10n.flies.client.commands;


/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class RetrieveCommand extends ConfigurableProjectCommand
{

   public RetrieveCommand(ConfigurableProjectOptions opts)
   {
      super(opts);
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("retrieve");
      // TODO needs DocSet support
   }

}
