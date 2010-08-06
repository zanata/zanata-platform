package org.fedorahosted.flies.client.command;

import javax.xml.bind.JAXBException;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class RetrieveCommand extends ConfigurableProjectCommand
{

   public RetrieveCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("retrieve");
      // TODO needs DocSet support
   }

   @Override
   public String getCommandName()
   {
      return "retrieve";
   }

   @Override
   public String getCommandDescription()
   {
      return "Retrieves translated text from a Flies project version.";
   }

}
