/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.maven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zanata.client.commands.push.RawPushCommand;
import org.zanata.client.commands.push.RawPushOptions;
import org.zanata.client.commands.push.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;

/**
 * Pushes source documents to a Zanata project-version so that it can be translated, and optionally pushes translated text as well.
 * 
 * NB: Any documents which exist on the server but not locally will be deleted as obsolete.
 * If deleteObsoleteModules is true, documents belonging to unknown/obsolete modules will be deleted as well.
 * 
 * @goal rawpush
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class RawPushMojo extends PushPullMojo<RawPushOptions> implements RawPushOptions
{

   public RawPushMojo() throws Exception
   {
      super();
   }

   @Override
   public RawPushCommand initCommand()
   {
      return new RawPushCommand(this);
   }

   /**
    * Language of source documents
    * 
    * @parameter expression="${zanata.sourceLang}" default-value="en-US"
    */
   private String sourceLang = "en-US";

   /**
    * Type of push to perform on the server: "source" pushes source documents only.
    * "trans" pushes translation documents only.
    * "both" pushes both source and translation documents.
    *
    * @parameter expression="${zanata.pushType}" default-value="source"
    */
   private String pushType;

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

   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public PushPullType getPushType()
   {
      return PushPullType.fromString(pushType);
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
   public String getCommandName()
   {
      return "rawpush";
   }

   @Override
   public int getChunkSize()
   {
      return maxChunkSize;
   }
}
