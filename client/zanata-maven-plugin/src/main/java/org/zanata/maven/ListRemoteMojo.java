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

   private static final String ZANATA_SERVER_PROJECT_TYPE = "remote";

   public ListRemoteMojo() throws Exception
   {
      super();
   }

   public ListRemoteCommand initCommand()
   {
      return new ListRemoteCommand(this);
   }


   @Override
   public String getProjectType()
   {
      return ZANATA_SERVER_PROJECT_TYPE;
   }

}
