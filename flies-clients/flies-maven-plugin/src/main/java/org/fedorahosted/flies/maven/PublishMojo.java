package org.fedorahosted.flies.maven;

import org.fedorahosted.flies.client.command.PublishCommand;

/**
 * Publishes source text to a Flies project version.
 * 
 * @goal publish
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PublishMojo extends ConfigurableProjectMojo<PublishCommand>
{

   public PublishMojo() throws Exception
   {
      super(new PublishCommand());
   }

}
