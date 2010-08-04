package org.fedorahosted.flies.maven;

import org.fedorahosted.flies.client.command.PutUserCommand;

/**
 * Creates or updates a Flies user.
 * 
 * @goal putuser
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutUserMojo extends ConfigurableMojo<PutUserCommand>
{

   public PutUserMojo() throws Exception
   {
      super(new PutUserCommand());
   }

}
