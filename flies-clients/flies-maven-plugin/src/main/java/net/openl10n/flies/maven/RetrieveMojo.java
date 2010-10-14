package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.RetrieveCommand;

/**
 * [NOT YET IMPLEMENTED] Retrieves translated text from a Flies project version.
 * 
 * @goal retrieve
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class RetrieveMojo extends ConfigurableProjectMojo
{

   public RetrieveMojo() throws Exception
   {
      super();
   }

   public RetrieveCommand initCommand()
   {
      return new RetrieveCommand(this);
   }

}
