package net.openl10n.flies.maven;

import java.io.File;

import net.openl10n.flies.client.commands.PublicanPushCommand;
import net.openl10n.flies.client.commands.PublicanPushOptions;

/**
 * Publishes publican source text to a Flies project version so that it can be
 * translated.
 * 
 * @goal publican-push
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PublicanPushMojo extends ConfigurableProjectMojo implements PublicanPushOptions
{

   public PublicanPushMojo() throws Exception
   {
      super();
   }

   @Override
   public PublicanPushCommand initCommand()
   {
      return new PublicanPushCommand(this);
   }

   /**
    * Base directory for publican files (with subdirectory "pot" and optional
    * locale directories)
    * 
    * @parameter expression="${flies.srcDir}"
    * @required
    */
   private File srcDir;

   /**
    * Language of source (defaults to en-US)
    * 
    * @parameter expression="${flies.sourceLang}"
    */
   private String sourceLang;

   /**
    * Import translations from local PO files to Flies, overwriting or erasing
    * existing translations (DANGER!)
    * 
    * @parameter expression="${flies.importPo}"
    */
   private boolean importPo;

   /**
    * Validate XML before sending request to server
    * 
    * @parameter expression="${flies.validate}"
    */
   private boolean validate;

   public File getSrcDir()
   {
      return srcDir;
   }

   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
   }

   public String getSourceLang()
   {
      return sourceLang;
   }

   public void setSourceLang(String sourceLang)
   {
      this.sourceLang = sourceLang;
   }

   public boolean getImportPo()
   {
      return importPo;
   }

   public void setImportPo(boolean importPo)
   {
      this.importPo = importPo;
   }

   public boolean getValidate()
   {
      return validate;
   }

   public void setValidate(boolean validate)
   {
      this.validate = validate;
   }

}
