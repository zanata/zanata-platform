package org.zanata.maven;

import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptions;

/**
 * Pulls translated text from Zanata.
 * 
 * @goal pull
 * @requiresProject true
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PullMojo extends PushPullMojo<PullOptions> implements PullOptions
{

   /**
    * Export source-language text from Zanata to local files, overwriting or
    * erasing existing files (DANGER!)
    * 
    * @parameter expression="${zanata.pullSrc}"
    */
   private boolean pullSrc;

   public PullMojo() throws Exception
   {
      super();
   }

   public PullCommand initCommand()
   {
      return new PullCommand(this);
   }

   @Override
   public boolean getPullSrc()
   {
      return pullSrc;
   }

}
