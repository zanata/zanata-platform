package org.zanata.maven;

import java.io.File;

import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptions;


/**
 * Pulls translated text from Zanata.
 * 
 * @goal pull
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PullMojo extends ConfigurableProjectMojo implements PullOptions
{

   /**
    * Base directory for source-language files
    * 
    * @parameter expression="${zanata.srcDir}" default-value="."
    */
   private File srcDir;

   /**
    * Base directory for target-language files (translations)
    * 
    * @parameter expression="${zanata.transDir}" default-value="."
    */
   private File transDir;

   /**
    * Type of project ("properties" = Java .properties, "podir" = publican-style
    * gettext directories)
    * 
    * @parameter expression="${zanata.projectType}"
    * @required
    */
   private String projectType;

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
   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public File getTransDir()
   {
      return transDir;
   }

   @Override
   public String getProjectType()
   {
      return projectType;
   }

   @Override
   public boolean getPullSrc()
   {
      return pullSrc;
   }

}
