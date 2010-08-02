package org.fedorahosted.flies.maven;

import org.fedorahosted.flies.client.command.PutProjectCommand;

/**
 * Creates or updates a Flies project.
 * 
 * @goal putproject
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutProjectMojo extends ConfigurableMojo<PutProjectCommand>
{

   public PutProjectMojo() throws Exception
   {
      super(new PutProjectCommand());
   }

}
