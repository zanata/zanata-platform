package net.openl10n.flies.client.commands;

import javax.xml.bind.JAXBException;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ListLocalCommand extends ConfigurableProjectCommand
{

   public ListLocalCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("listlocal");

      // TODO needs DocSet support
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
