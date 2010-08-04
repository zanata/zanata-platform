package org.fedorahosted.flies.client.command;

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
      System.out.println("listlocal");

      // TODO needs DocSet support
   }

}
