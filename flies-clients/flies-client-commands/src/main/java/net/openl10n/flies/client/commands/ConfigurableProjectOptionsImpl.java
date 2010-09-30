/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package net.openl10n.flies.client.commands;

import net.openl10n.flies.client.config.LocaleList;

import org.kohsuke.args4j.Option;

/**
 * Base options for Flies commands which supports configuration by the user's
 * flies.ini and by a project's flies.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableProjectOptionsImpl extends ConfigurableOptionsImpl implements ConfigurableProjectOptions
{

   /**
    * Project configuration file for Flies client.
    */
   // When used as a CLI command, the default path (specified here) is relative
   // to CWD. ConfigurableProjectMojo specifies another default, which is
   // relative to project's basedir.
   private String projectConfig = "flies.xml";

   private String project;
   private String projectVersion;
   private LocaleList locales;

   @Override
   public String getProject()
   {
      return project;
   }

   @Override
   @Option(name = "--project", metaVar = "PROJ", usage = "Flies project ID/slug.  This value is required unless specified in flies.xml.")
   public void setProject(String projectSlug)
   {
      this.project = projectSlug;
   }

   @Override
   @Option(name = "--project-config", metaVar = "FILENAME", usage = "Flies project configuration, eg flies.xml", required = false)
   public void setProjectConfig(String projectConfig)
   {
      this.projectConfig = projectConfig;
   }

   @Override
   public String getProjectVersion()
   {
      return projectVersion;
   }

   @Override
   @Option(name = "--project-version", metaVar = "VER", usage = "Flies project version ID  This value is required unless specified in flies.xml.")
   public void setProjectVersion(String versionSlug)
   {
      this.projectVersion = versionSlug;
   }

   @Override
   public String getProjectConfig()
   {
      return projectConfig;
   }

   @Override
   public LocaleList getLocales()
   {
      return locales;
   }

   @Override
   public void setLocales(LocaleList locales)
   {
      this.locales = locales;
   }

}
