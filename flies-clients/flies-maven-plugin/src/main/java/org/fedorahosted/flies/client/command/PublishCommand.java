package org.fedorahosted.flies.client.command;

import javax.xml.bind.JAXBException;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PublishCommand extends ConfigurableProjectCommand
{

   public PublishCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("publish");
      // TODO needs DocSet support
   }

   @Override
   public String getCommandName()
   {
      return "publish";
   }

   @Override
   public String getCommandDescription()
   {
      return "Publishes source text to a Flies project version.";
   }

}
