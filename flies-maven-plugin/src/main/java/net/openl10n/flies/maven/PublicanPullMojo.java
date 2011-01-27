package net.openl10n.flies.maven;

import java.io.File;

import net.openl10n.flies.client.commands.PublicanPullCommand;
import net.openl10n.flies.client.commands.PublicanPullOptions;

/**
 * Pulls translated text from Flies.
 * 
 * @goal publican-pull
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PublicanPullMojo extends ConfigurableProjectMojo implements PublicanPullOptions
{

   /**
    * Base directory for publican files (with subdirectory "pot" and optional
    * locale directories), although the location of "pot" can be overridden with
    * the dstDirPot option.
    * 
    * @parameter expression="${flies.dstDir}"
    * @required
    */
   private File dstDir;

   /**
    * Base directory for pot files.
    * 
    * @parameter expression="${flies.dstDirPot}"
    *            default-value="${flies.dstDir}/pot"
    * @required
    */
   private File dstDirPot;

   /**
    * Export source text from Flies to local POT files, overwriting or erasing
    * existing POT files (DANGER!)
    * 
    * @parameter expression="${flies.exportPot}"
    */
   private boolean exportPot;

   public PublicanPullMojo() throws Exception
   {
      super();
   }

   public PublicanPullCommand initCommand()
   {
      return new PublicanPullCommand(this);
   }

   @Override
   public void setDstDir(File dstDir)
   {
      this.dstDir = dstDir;
   }

   @Override
   public void setDstDirPot(File dstDirPot)
   {
      this.dstDirPot = dstDirPot;
   }

   @Override
   public File getDstDir()
   {
      return dstDir;
   }

   @Override
   public File getDstDirPot()
   {
      return dstDirPot;
   }

   @Override
   public boolean getExportPot()
   {
      return exportPot;
   }

   @Override
   public void setExportPot(boolean exportPot)
   {
      this.exportPot = exportPot;
   }

}
