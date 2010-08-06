package org.fedorahosted.flies.client.commands;

import javax.xml.bind.JAXBException;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutUserCommand extends ConfigurableCommand
{

   public PutUserCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("putuser");
      // TODO
   }

   @Override
   public String getCommandName()
   {
      return "putuser";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Flies user.";
   }

}
