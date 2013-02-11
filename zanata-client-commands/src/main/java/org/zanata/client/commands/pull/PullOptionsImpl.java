/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands.pull;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.BooleanValueHandler;
import org.zanata.client.commands.ConfigurableProjectOptionsImpl;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.PushPullType;
import org.zanata.client.config.LocaleList;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PullOptionsImpl extends ConfigurableProjectOptionsImpl implements PullOptions
{
   private static final String DEFAULT_PULL_TYPE = "trans";
   private static final boolean DEFAULT_CREATE_SKELETONS = false;
   private static final boolean DEFAULT_DRY_RUN = false;
   private static final boolean DEFAULT_ENCODE_TABS = true;
   private static final boolean DEFAULT_INCLUDE_FUZZY = false;
   private static final boolean DEFAULT_USE_CACHE = true;
   private static final boolean DEFAULT_PURGE_CACHE = false;

   private String pullType = DEFAULT_PULL_TYPE;
   private File transDir;
   private File srcDir;

   private boolean createSkeletons = DEFAULT_CREATE_SKELETONS;
   private boolean dryRun = DEFAULT_DRY_RUN;
   private boolean encodeTabs = DEFAULT_ENCODE_TABS;
   private boolean includeFuzzy = DEFAULT_INCLUDE_FUZZY;
   private boolean useCache = DEFAULT_USE_CACHE;
   private boolean purgeCache = DEFAULT_PURGE_CACHE;
   private String[] locales;
   private LocaleList effectiveLocales;

   @Override
   public ZanataCommand initCommand()
   {
      return new PullCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "pull";
   }

   @Override
   public String getCommandDescription()
   {
      return "Pull translated text from Zanata.";
   }

   @Option(aliases = { "-l" }, name = "--locales", metaVar = "LOCALE1,LOCALE2", usage = "Locales to pull from the server.\n" +
       "By default all locales in zanata.xml will be pulled.")
   public void setLocales(String locales)
   {
      this.locales = locales.split(",");
   }

   /**
    * Override the parent method as the pull
    * command can have locales specified via command line.
    *
    * @return The locale map list taking into account the global locales in zanata.xml as well as the command line
    * argument ones.
    */
   @Override
   public LocaleList getLocaleMapList()
   {
      if(effectiveLocales == null)
      {
         effectiveLocales = PushPullCommand.getLocaleMapList(super.getLocaleMapList(), locales);
      }
      return effectiveLocales;
   }

   @Override
   public File getSrcDir()
   {
      return srcDir;
   }

   @Option(aliases = { "-s" }, name = "--src-dir", metaVar = "DIR", required = true, usage = "Base directory for source files (eg \".\", \"pot\", \"src/main/resources\")")
   public void setSrcDir(File file)
   {
      this.srcDir = file;
   }

   @Override
   public File getTransDir()
   {
      return transDir;
   }

   @Option(aliases = { "-t" }, name = "--trans-dir", metaVar = "DIR", required = true, usage = "Base directory for translated files (eg \".\", \"po\", \"src/main/resources\")")
   public void setTransDir(File transDir)
   {
      this.transDir = transDir;
   }

   @Override
   public PushPullType getPullType()
   {
      return PushPullType.fromString(pullType);
   }

   @Option(name = "--pull-type", metaVar = "TYPE", required = false,
         usage = "Type of pull to perform from the server: \"source\" pulls source documents only.\n" +
            "\"trans\" (default) pulls translation documents only.\n" +
            "\"both\" pulls both source and translation documents.")
   public void setPullType(String pullType)
   {
      this.pullType = pullType;
   }


   @Override
   public boolean getEnableModules()
   {
      // modules are currently only supported by Maven Mojos:
      return false;
   }

   @Override
   public boolean isDryRun()
   {
      return this.dryRun;
   }

   @Option(aliases = {"-n" }, name = "--dry-run", usage = "Dry run: don't change any data, on the server or on the filesystem.")
   public void setDryRun(boolean dryRun)
   {
      this.dryRun = dryRun;
   }

   @Override
   public boolean isRootModule()
   {
      return false;
   }

   @Override
   public String getCurrentModule()
   {
      return "";
   }

   @Override
   public Set<String> getAllModules()
   {
      return Collections.emptySet();
   }

   @Override
   public String getDocNameRegex()
   {
      // modules are currently only supported by Maven Mojos:
      return null;
   }

   @Override
   public String getModuleSuffix()
   {
      // modules are currently only supported by Maven Mojos:
      return null;
   }

   @Override
   public boolean getCreateSkeletons()
   {
      return createSkeletons;
   }

   @Option(name = "--create-skeletons", usage = "Create skeleton entries for strings/files which have not been translated yet." +
                                                " Skeletons are not created by default.")
   public void setCreateSkeletons(boolean createSkeletons)
   {
      this.createSkeletons = createSkeletons;
   }

   @Override
   public boolean getEncodeTabs()
   {
      return this.encodeTabs;
   }

   @Option(name = "--encode-tabs", handler = BooleanValueHandler.class,
      usage = "Whether tabs should be encoded as \\t (true, default) or left as tabs (false).")
   public void setEncodeTabs(boolean encodeTabs)
   {
      this.encodeTabs = encodeTabs;
   }

   @Override
   public boolean getIncludeFuzzy()
   {
      return this.includeFuzzy;
   }

   @Option(name = "--include-fuzzy", usage = "[project type 'raw' only] Whether to include fuzzy " +
      "translations in translation files. " +
      "If this option is false (default), source text will be used for any string " +
      "that does not have an approved translation.")
   public void setIncludeFuzzy(boolean includeFuzzy)
   {
      this.includeFuzzy = includeFuzzy;
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
}
