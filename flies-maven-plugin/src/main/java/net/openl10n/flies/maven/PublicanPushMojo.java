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
    * locale directories), although the location of "pot" can be overridden with
    * the srcDirPot option.
    * 
    * @parameter expression="${flies.srcDir}"
    * @required
    */
   private File srcDir;

   /**
    * Base directory for pot files.
    * 
    * @parameter expression="${flies.srcDirPot}"
    *            default-value="${flies.srcDir}/pot"
    */
   private File srcDirPot;

   /**
    * Language of source (defaults to en-US)
    * 
    * @parameter expression="${flies.sourceLang}"
    */
   private String sourceLang = "en-US";

   /**
    * Import translations from local PO files to Flies, overwriting or erasing
    * existing translations (DANGER!)
    * 
    * @parameter expression="${flies.importPo}"
    */
   private boolean importPo;
   
   /**
    * Whether Flies should copy latest translation from equivalent documents from other versions of the same project
    * @parameter expression="${flies.copyTrans}" default-value="true"
    */
   private boolean copyTrans;

   /**
    * Validate XML before sending request to server
    * 
    * @parameter expression="${flies.validate}"
    */
   private boolean validate;

   @Override
   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public File getSrcDirPot()
   {
      return srcDirPot;
   }

   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public boolean getImportPo()
   {
      return importPo;
   }

   @Override
   public boolean getCopyTrans()
   {
      return copyTrans;
   }

   public boolean getValidate()
   {
      return validate;
   }

}
