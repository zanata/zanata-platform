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

import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.RawPullCommand;
import org.zanata.client.commands.push.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;

/**
 * Pulls translated text from Zanata.
 * 
 * @goal pull
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PullMojo extends PushPullMojo<PullOptions> implements PullOptions
{

   /**
    * Export source-language text from Zanata to local files, overwriting or
    * erasing existing files (DANGER!). This option is deprecated, replaced by pullType.
    * 
    * @parameter expression="${zanata.pullSrc}"
    */
   @Deprecated
   // Using string instead of boolean to know when pullSrc has been explicitly used.
   private String pullSrc;

   /**
    * Whether to create skeleton entries for strings/files which have not been translated yet
    * @parameter expression="${zanata.createSkeletons}"
    */
   private boolean createSkeletons;

   /**
    * Whether to include fuzzy translations in translation files when using project type 'raw'.
    * If this option is false, source text will be used for any string that does not have an
    * approved translation.
    * 
    * @parameter expression="${zanata.includeFuzzy}" default-value="false"
    */
   private boolean includeFuzzy = false;

   /**
    * Whether to purge the cache before performing the pull operation. This means that all
    * documents will be fetched from the server anew.
    *
    * @parameter expression="${zanata.purgeCache}" default-value="false"
    */
   private boolean purgeCache;

   /**
    * Whether to use an Entity cache when fetching documents. When using the cache, documents
    * that have been retrieved previously and have not changed since then will not be retrieved
    * again.
    *
    * @parameter expression="${zanata.useCache}" default-value="true"
    */
   private boolean useCache;

   /**
    * Type of pull to perform from the server: "source" pulls source documents only.
    * "trans" pulls translation documents only.
    * "both" pulls both source and translation documents.
    *
    * @parameter expression="${zanata.pullType}" default-value="trans"
    */
   private String pullType;

   /**
    * Locales to pull from the server.
    * By default all locales in zanata.xml will be pulled.
    * Usage: -Dzanata.locales=locale1,locale2,locale3
    *
    * @parameter expression="${zanata.locales}"
    */
   private String[] locales;

   // Cached copy of the effective locales to avoid calculating it more than once
   private LocaleList effectiveLocales;


   public PullMojo() throws Exception
   {
      super();
   }

   public PushPullCommand<PullOptions> initCommand()
   {
      if ("raw".equals(getProjectType()))
      {
         return new RawPullCommand(this);
      }
      else
      {
         return new PullCommand(this);
      }
   }

   @Override
   public boolean getCreateSkeletons()
   {
      return createSkeletons;
   }

   @Override
   public boolean getIncludeFuzzy()
   {
      return includeFuzzy;
   }

   @Override
   public boolean getPurgeCache()
   {
      return purgeCache;
   }

   @Override
   public boolean getUseCache()
   {
      return useCache;
   }

   @Override
   public PushPullType getPullType()
   {
      // if the deprecated 'pushTrans' option has been used
      if( pullSrc != null )
      {
         return Boolean.parseBoolean(pullSrc) ? PushPullType.Both : PushPullType.Trans;
      }
      else
      {
         return PushPullType.fromString(pullType);
      }
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
      return "pull";
   }

}
