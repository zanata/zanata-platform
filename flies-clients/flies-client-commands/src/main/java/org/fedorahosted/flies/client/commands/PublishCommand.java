package org.fedorahosted.flies.client.commands;

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
   public String getCommandName()
   {
      return "publish";
   }

   @Override
   public String getCommandDescription()
   {
      return "Sends source text to Flies so that it can be translated.";
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("publish");
      // TODO needs DocSet support
   }

}
