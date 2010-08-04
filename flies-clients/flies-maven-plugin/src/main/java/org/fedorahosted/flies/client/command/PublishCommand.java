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
      // TODO needs DocSet support
   }

}
