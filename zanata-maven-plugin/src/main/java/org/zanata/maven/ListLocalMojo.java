package org.zanata.maven;

import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ListLocalCommand;

/* Javadoc disabled so that maven won't include this unfinished mojo in the plugin:
 * [NOT YET IMPLEMENTED] Lists all local files in the project which are
 * considered to be Zanata documents. These are the files which will be sent to
 * Zanata when using the 'push' goal.
 * 
 * @goal listlocal
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class ListLocalMojo extends ConfigurableProjectMojo<ConfigurableProjectOptions>
{

   public ListLocalMojo() throws Exception
   {
      super();
   }

   public ListLocalCommand initCommand()
   {
      return new ListLocalCommand(this);
   }

}
