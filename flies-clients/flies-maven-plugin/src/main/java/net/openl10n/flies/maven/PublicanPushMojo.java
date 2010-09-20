package net.openl10n.flies.maven;

import java.io.File;

import net.openl10n.flies.client.commands.PublicanPushCommand;

/**
 * Publishes publican source text to a Flies project version so that it can be
 * translated.
 * 
 * @goal publican-push
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PublicanPushMojo extends ConfigurableProjectMojo<PublicanPushCommand>
{

   public PublicanPushMojo() throws Exception
   {
      super(new PublicanPushCommand());
   }

   /**
    * Base directory for publican files (with subdirectory "pot" and optional
    * locale directories)
    * 
    * @parameter expression="${flies.srcDir}"
    * @required
    */
   @SuppressWarnings("unused")
   private File srcDir;

   /**
    * Language of source (defaults to en-US)
    * 
    * @parameter expression="${flies.sourceLang}"
    */
   @SuppressWarnings("unused")
   private String sourceLang;

   /**
    * Import translations from local PO files to Flies, overwriting or erasing
    * existing translations (DANGER!)
    * 
    * @parameter expression="${flies.importPo}"
    */
   @SuppressWarnings("unused")
   private boolean importPo;

   /**
    * Validate XML before sending request to server
    * 
    * @parameter expression="${flies.validate}"
    */
   @SuppressWarnings("unused")
   private boolean validate;

   public void setSrcDir(File srcDir)
   {
      getCommand().setSrcDir(srcDir);
   }

   public void setSourceLang(String sourceLang)
   {
      getCommand().setSourceLang(sourceLang);
   }

   public void setImportPo(boolean importPo)
   {
      getCommand().setImportPo(importPo);
   }

   public void setValidate(boolean validate)
   {
      getCommand().setValidate(validate);
   }
}
