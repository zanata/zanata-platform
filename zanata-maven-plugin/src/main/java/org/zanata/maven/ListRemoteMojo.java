package org.zanata.maven;

import org.zanata.client.commands.ListRemoteCommand;

/**
 * Lists all remote documents in the configured Zanata project version.
 * 
 * @goal listremote
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class ListRemoteMojo extends ConfigurableProjectMojo
{

   public ListRemoteMojo() throws Exception
   {
      super();
   }

   public ListRemoteCommand initCommand()
   {
      return new ListRemoteCommand(this);
   }

   private final String zanataServerProjectType = "remote";

   @Override
   public String getProjectType()
   {
      return zanataServerProjectType;
   }

}
