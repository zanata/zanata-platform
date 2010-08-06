package org.fedorahosted.flies.maven;

import org.fedorahosted.flies.client.commands.PutVersionCommand;

/**
 * Creates or updates a Flies project version.
 * 
 * @goal putversion
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutVersionMojo extends ConfigurableMojo<PutVersionCommand>
{

   public PutVersionMojo() throws Exception
   {
      super(new PutVersionCommand());
   }

}
