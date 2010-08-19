package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.ListRemoteCommand;

/**
 * Lists all remote documents in the configured Flies project version.
 * 
 * @goal listremote
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class ListRemoteMojo extends ConfigurableProjectMojo<ListRemoteCommand>
{

   public ListRemoteMojo() throws Exception
   {
      super(new ListRemoteCommand());
   }

}
