package org.zanata.maven;

import java.io.File;

import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;

/**
 * Pushes source text to a Zanata project version so that it can be translated.
 * 
 * @goal push
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PushMojo extends ConfigurableProjectMojo implements PushOptions
{

   public PushMojo() throws Exception
   {
      super();
   }

   @Override
   public PushCommand initCommand()
   {
      return new PushCommand(this);
   }

   /**
    * Base directory for source-language files
    * 
    * @parameter expression="${zanata.sourceDir}" default-value="."
    */
   private File sourceDir;

   /**
    * Base directory for target-language files (translations)
    * 
    * @parameter expression="${zanata.transDir}" default-value="."
    */
   private File transDir;

   /**
    * Type of project ("properties" is the only supported type at present)
    * 
    * @parameter expression="${zanata.projectType}"
    * @required
    */
   private String projectType;

   /**
    * Language of source documents
    * 
    * @parameter expression="${zanata.sourceLang}" default-value="en-US"
    */
   private String sourceLang = "en-US";

   /**
    * Push translations from local files to the server (merge or import: see
    * mergeType)
    * 
    * @parameter expression="${zanata.pushTrans}"
    */
   private boolean pushTrans;

   /**
    * Whether the server should copy latest translations from equivalent
    * messages/documents in the database (only applies to new documents)
    * 
    * @parameter expression="${zanata.copyTrans}" default-value="true"
    */
   private boolean copyTrans;

   /**
    * Merge type: "auto" (default) or "import" (DANGER!).
    * 
    * @parameter expression="${zanata.merge}" default-value="auto"
    */
   private String merge;

   /**
    * Name pattern of source file
    * 
    * @parameter expression="${zanata.sourcePattern}"
    */
   private String sourcePattern;

   @Override
   public File getSourceDir()
   {
      return sourceDir;
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
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public boolean getPushTrans()
   {
      return pushTrans;
   }

   @Override
   public boolean getCopyTrans()
   {
      return copyTrans;
   }

   @Override
   public String getMergeType()
   {
      return merge;
   }

   @Override
   public String getSourcePattern()
   {
      return sourcePattern;
   }

}
