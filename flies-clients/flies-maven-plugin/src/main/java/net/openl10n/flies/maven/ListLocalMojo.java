package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.ListLocalCommand;

/**
 * Lists all local files in the project which are considered to be Flies
 * documents. These are the files which will be sent to Flies when using the
 * 'publish' goal.
 * 
 * @goal listlocal
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class ListLocalMojo extends ConfigurableProjectMojo<ListLocalCommand>
{

   public ListLocalMojo() throws Exception
   {
      super(new ListLocalCommand());
   }

}
