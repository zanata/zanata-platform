package org.zanata.maven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushType;

/**
 * Pushes source text to a Zanata project version so that it can be translated, and optionally push translated text as well.
 * NB: Any documents which exist on the server but not locally will be deleted as obsolete.
 * If deleteObsoleteModules is true, documents belonging to unknown/obsolete modules will be deleted as well.
 * 
 * @goal push
 * @requiresProject true
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PushMojo extends PushPullMojo<PushOptions> implements PushOptions
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
    * Language of source documents
    * 
    * @parameter expression="${zanata.sourceLang}" default-value="en-US"
    */
   private String sourceLang = "en-US";

   /**
    * Push translations from local files to the server (merge or import: see
    * mergeType). This option is deprecated, replaced by pushType.
    *
    * @parameter expression="${zanata.pushTrans}"
    */
   @Deprecated
   // Using string instead of boolean to know when pushTrans has been explicitly used.
   private String pushTrans;

   /**
    * Type of push to perform on the server: "source" pushes source documents only.
    * "trans" pushes translation documents only.
    * "both" pushes both source and translation documents.
    *
    * @parameter expression="${zanata.pushType}" default-value="source"
    */
   private String pushType;

   /**
    * Whether the server should copy latest translations from equivalent
    * messages/documents in the database (only applies to new documents)
    * 
    * @parameter expression="${zanata.copyTrans}" default-value="true"
    */
   private boolean copyTrans;

   /**
    * Obsolete option, only for backwards compatibility
    * 
    * @parameter expression="${zanata.useSrcOrder}" default-value="false"
    */
   @Deprecated
   private boolean useSrcOrder;

   /**
    * Merge type: "auto" (default) or "import" (DANGER!).
    * 
    * @parameter expression="${zanata.merge}" default-value="auto"
    */
   private String merge;

   /**
    * Wildcard pattern to include file and directory. This parameter is only
    * needed for some project types, eg XLIFF, Properties. Usage
    * -Dzanata.includes="src/myfile*.xml,**&#47*.xliff.xml"
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

   /**
    * @parameter expression="${zanata.deleteObsoleteModules}" default-value="false"
    */
   private boolean deleteObsoleteModules;


   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public PushType getPushType()
   {
      // if the deprecated 'pushTrans' option has been used
      if( pushTrans != null )
      {
         return Boolean.parseBoolean(pushTrans) ? PushType.Both : PushType.Source;
      }
      else
      {
         return PushType.fromString(pushType);
      }
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
   public boolean getDeleteObsoleteModules()
   {
      return this.deleteObsoleteModules;
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
