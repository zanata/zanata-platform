package org.zanata.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
    * Should we ensure that translations appear in the same order as the source
    * strings? This is only needed for compatibility with Zanata server below
    * v1.4.
    * 
    * @parameter expression="${zanata.useSrcOrder}" default-value="false"
    */
   private boolean useSrcOrder;

   /**
    * Merge type: "auto" (default) or "import" (DANGER!).
    * 
    * @parameter expression="${zanata.merge}" default-value="auto"
    */
   private String merge;

   /**
    * Wildcard pattern to include file and directory. This parameter is only
    * needed for some project types, eg XLIFF. Usage
    * -Dzanata.includes="Pattern1,Pattern2,Pattern3"
    * 
    * @parameter expression="${zanata.includes}"
    */
   private String includes;

   /**
    * Wildcard pattern to exclude file and directory. Usage
    * -Dzanata.excludes="Pattern1,Pattern2,Pattern3"
    * 
    * @parameter expression="${zanata.excludes}"
    */
   private String excludes;

   /**
    * Add default excludes to the exclude filters.
    * 
    * @parameter expression="${zanata.defaultExcludes}" default-value="true"
    */
   private boolean defaultExcludes = true;

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
   public boolean getUseSrcOrder()
   {
      return useSrcOrder;
   }

   @Override
   public List<String> getIncludes()
   {
      String[] includeList = StringUtils.split(includes, ",");
      List<String> list = new ArrayList<String>();
      if (includeList != null && includeList.length > 0)
      {
         Collections.addAll(list, includeList);
      }
      return list;
   }

   @Override
   public List<String> getExcludes()
   {
      String[] excludeList = StringUtils.split(excludes, ",");
      List<String> list = new ArrayList<String>();
      if (excludeList != null && excludeList.length > 0)
      {
         Collections.addAll(list, excludeList);
      }
      return list;
   }

   @Override
   public boolean getDefaultExcludes()
   {
      return defaultExcludes;
   }

}
