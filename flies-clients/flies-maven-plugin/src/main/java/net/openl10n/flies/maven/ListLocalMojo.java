package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.ListLocalCommand;

/* Javadoc disabled so that maven won't include this unfinished mojo in the plugin:
 * [NOT YET IMPLEMENTED] Lists all local files in the project which are
 * considered to be Flies documents. These are the files which will be sent to
 * Flies when using the 'publican-push' goal.
 * 
 * @goal listlocal
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class ListLocalMojo extends ConfigurableProjectMojo
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
