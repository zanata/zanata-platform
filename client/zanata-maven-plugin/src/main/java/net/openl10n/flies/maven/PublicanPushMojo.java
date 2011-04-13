package net.openl10n.flies.maven;

import java.io.File;

import net.openl10n.flies.client.commands.PublicanPushCommand;
import net.openl10n.flies.client.commands.PublicanPushOptions;

/**
 * Publishes publican source text to a Zanata project version so that it can be
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
    * @parameter expression="${zanata.srcDir}"
    * @required
    */
   private File srcDir;

   /**
    * Base directory for pot files.
    * 
    * @parameter expression="${zanata.srcDirPot}"
    *            default-value="${zanata.srcDir}/pot"
    */
   private File srcDirPot;

   /**
    * Language of source (defaults to en-US)
    * 
    * @parameter expression="${zanata.sourceLang}"
    */
   private String sourceLang = "en-US";

   /**
    * Import/merge translations from local PO files to the server, overwriting or erasing
    * existing translations (DANGER!)
    * 
    * @parameter expression="${zanata.importPo}"
    */
   private boolean importPo;
   
   /**
    * Whether the server should copy latest translations from equivalent messages/documents in the database.
    * @parameter expression="${zanata.copyTrans}" default-value="true"
    */
   private boolean copyTrans;

   /**
    * Should the client validate XML before sending request to server (debugging).
    * 
    * @parameter expression="${zanata.validate}"
    */
   private boolean validate;

   /**
    * Merge type: "auto" (default) or "import" (DANGER!).  
    * 
    * @parameter expression="${zanata.merge}" default-value="auto"
    */
   private String merge;
   
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
   
   @Override
   public String getMergeType()
   {
      return merge;
   }

}
