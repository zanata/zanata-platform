package org.fedorahosted.flies.client.command;

import javax.xml.bind.JAXBException;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectCommand extends ConfigurableCommand
{

   public PutProjectCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run()
   {
      // TODO remove this
      System.out.println("putproject");
      // TODO
   }

   @Override
   public String getCommandName()
   {
      return "putproject";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Flies project.";
   }

}
