package org.fedorahosted.flies.client.commands;

import javax.xml.bind.JAXBException;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutVersionCommand extends ConfigurableCommand
{

   public PutVersionCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("putversion");
      // TODO
   }

   @Override
   public String getCommandName()
   {
      return "putversion";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Flies project version.";
   }

}
