package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PublishCommand;

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
