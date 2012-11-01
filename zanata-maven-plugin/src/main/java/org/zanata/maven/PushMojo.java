package org.zanata.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushPullType;
import org.zanata.client.commands.push.RawPushCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;

/**
 * Pushes source text to a Zanata project version so that it can be translated, and optionally push translated text as well.
 * NB: Any documents which exist on the server but not locally will be deleted as obsolete.
 * If deleteObsoleteModules is true, documents belonging to unknown/obsolete modules will be deleted as well.
 * 
 * @goal push
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PushMojo extends PushPullMojo<PushOptions> implements PushOptions
{

   public PushMojo() throws Exception
   {
      super();
   }

   @Override
   public PushPullCommand<PushOptions> initCommand()
   {
      if ("raw".equals(getProjectType()))
      {
         return new RawPushCommand(this);
      }
      else
      {
         return new PushCommand(this);
      }
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

   /**
    * Locales to push to the server.
    * By default all locales in zanata.xml will be pushed.
    * Usage: -Dzanata.locales=locale1,locale2,locale3
    *
    * @parameter expression="${zanata.locales}"
    */
   private String[] locales;

   // Cached copy of the effective locales to avoid calculating it more than once
   private LocaleList effectiveLocales;

   /**
    * Maximum size, in bytes, of document chunks to transmit. Documents smaller
    * than this size will be transmitted in a single request, larger documents
    * will be sent over multiple requests.
    * 
    * Usage -Dzanata.maxChunkSize=12345
    * 
    * @parameter expression="${zanata.maxChunkSize}" default-value="1048576"
    */
   private int maxChunkSize = 1024 * 1024;

   /**
    * File types to locate and transmit to the server.
    * 
    * @parameter expression="${zanata.fileTypes}" default-value="txt,dtd,odt,fodt,odp,fodp,ods,fods,odg,fodg,odf,odb"
    */
   private String[] fileTypes;

   /**
    * Case sensitive for includes and excludes options.
    * 
    * @parameter expression="${zanata.caseSensitive}" default-value="true"
    */
   private boolean caseSensitive = true;

   /**
    * Exclude filenames which match locales in zanata.xml (other than the
    * source locale).  For instance, if zanata.xml includes de and fr,
    * then the files messages_de.properties and messages_fr.properties
    * will not be treated as source files.
    * <p>
    * NB: This parameter will be ignored for some project types which use
    * different file naming conventions (eg podir, gettext).
    *
    * @parameter expression="${zanata.excludeLocaleFilenames}" default-value="true"
    */
   private boolean excludeLocaleFilenames = true;
   
   
   /**
    * Run validation check against file. Only apply to Xliff project type.
    * "CONTENT" - content validation check. "XSD" - validation check against
    * xliff 1.1 schema -
    * http://www.oasis-open.org/committees/xliff/documents/xliff-core-1.1.xsd.
    * 
    * @parameter expression="${zanata.validate}" default-value="content"
    */
   private String validate = "content";


   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public PushPullType getPushType()
   {
      // if the deprecated 'pushTrans' option has been used
      if( pushTrans != null )
      {
         return Boolean.parseBoolean(pushTrans) ? PushPullType.Both : PushPullType.Source;
      }
      else
      {
         return PushPullType.fromString(pushType);
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

   /**
    * Override the default {@link org.zanata.maven.ConfigurableProjectMojo#getLocaleMapList()} method as the push
    * command can have locales specified via command line.
    *
    * @return The locale map list taking into account the global locales in zanata.xml as well as the command line
    * argument ones.
    */
   @Override
   public LocaleList getLocaleMapList()
   {
      if( effectiveLocales == null )
      {
         if(locales != null && locales.length > 0)
         {
            // filter the locales that are specified in both the global config and the parameter list
            effectiveLocales = new LocaleList();

            for( String locale : locales )
            {
               boolean foundLocale = false;
               for(LocaleMapping lm : super.getLocaleMapList())
               {
                  if( lm.getLocale().equals(locale) ||
                        (lm.getMapFrom() != null && lm.getMapFrom().equals( locale )) )
                  {
                     effectiveLocales.add(lm);
                     foundLocale = true;
                     break;
                  }
               }

               if(!foundLocale)
               {
                  throw new ConfigException("Specified locale '" + locale + "' was not found in zanata.xml!" );
               }
            }
         }
         else
         {
            effectiveLocales = super.getLocaleMapList();
         }
      }

      return effectiveLocales;
   }

   @Override
   public int getChunkSize()
   {
      return maxChunkSize;
   }

   @Override
   public List<String> getFileTypes()
   {
      return Arrays.asList(fileTypes);
   }

   @Override
   public String getCommandName()
   {
      return "push";
   }

   @Override
   public boolean getCaseSensitive()
   {
      return caseSensitive;
   }

   @Override
   public boolean getExcludeLocaleFilenames()
   {
      return excludeLocaleFilenames;
   }

   @Override
   public String getValidate()
   {
      return validate;
   }
}
