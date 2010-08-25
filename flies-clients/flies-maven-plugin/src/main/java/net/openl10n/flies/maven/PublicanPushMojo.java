package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PublicanPushCommand;

/**
 * Publishes publican source text to a Flies project version so that it can be
 * translated.
 * 
 * @goal publican-push
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PublicanPushMojo extends ConfigurableProjectMojo<PublicanPushCommand>
{

   public PublicanPushMojo() throws Exception
   {
      super(new PublicanPushCommand());
   }

}
