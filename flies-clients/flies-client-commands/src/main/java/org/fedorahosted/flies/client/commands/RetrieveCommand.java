package org.fedorahosted.flies.client.commands;

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
   public String getCommandName()
   {
      return "retrieve";
   }

   @Override
   public String getCommandDescription()
   {
      return "Fetches translated text from Flies.";
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("retrieve");
      // TODO needs DocSet support
   }

}
