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
package org.fedorahosted.flies.client.commands;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fedorahosted.flies.client.config.FliesConfig;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Flies commands which supports configuration by the user's
 * flies.ini and by a project's flies.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableProjectCommand extends ConfigurableCommand
{

   private static final Logger log = LoggerFactory.getLogger(ConfigurableProjectCommand.class);
   private final JAXBContext jc;
   private final Unmarshaller unmarshaller;

   /**
    * Project configuration file for Flies client.
    */
   // When used as a CLI command, the default path (specified here) is relative
   // to CWD. ConfigurableProjectMojo specifies another default, which is
   // relative to project's basedir.
   protected String projectConfig = "flies.xml";

   private String project;
   private String projectVersion;

   public ConfigurableProjectCommand()
   {
      try
      {
         jc = JAXBContext.newInstance(FliesConfig.class);
         unmarshaller = jc.createUnmarshaller();
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Loads the config files (controlled by the properties userConfig and
    * projectConfig) to supply any values which haven't already been set via
    * parameters.
    * 
    * @throws Exception
    */
   @Override
   public void initConfig() throws Exception
   {
      if (projectConfig != null)
      {
         File projectConfigFile = null;
         String userDir = System.getProperty("user.dir");
         File projectDir = new File(userDir);
         while (projectDir != null && !(projectConfigFile = new File(projectDir, projectConfig)).exists())
         {
            projectDir = projectDir.getParentFile();
         }

         if (projectConfigFile.exists())
         {
            log.info("Loading flies project config from {}", projectConfigFile);
            FliesConfig fliesConfig = (FliesConfig) unmarshaller.unmarshal(projectConfigFile);
            // local project config is supposed to override user's flies.ini, so
            // we
            // apply it first
            applyProjectConfig(fliesConfig);
         }
         else
         {
            log.warn("Flies project config file '{}' not found in '{}' or parent directories; ignoring.", projectConfig, userDir);
         }
      }
      super.initConfig();
   }

   /**
    * Applies values from the project configuration unless they have been set
    * directly via parameters.
    * 
    * @param config
    */
   private void applyProjectConfig(FliesConfig config)
   {
      if (project == null)
      {
         project = config.getProject();
      }
      if (getUrl() == null)
      {
         setUrl(config.getUrl());
      }
      if (projectVersion == null)
      {
         projectVersion = config.getProjectVersion();
      }
   }

   public String getProject()
   {
      return project;
   }

   @Option(name = "--project", metaVar = "PROJ", usage = "Flies project ID/slug.  This value is required unless specified in flies.xml.")
   public void setProject(String projectSlug)
   {
      this.project = projectSlug;
   }

   @Option(name = "--project-config", metaVar = "FILENAME", usage = "Flies project configuration, eg flies.xml", required = false)
   public void setProjectConfig(String projectConfig)
   {
      this.projectConfig = projectConfig;
   }

   public String getProjectVersion()
   {
      return projectVersion;
   }

   @Option(name = "--project-version", metaVar = "VER", usage = "Flies project version ID  This value is required unless specified in flies.xml.")
   public void setProjectVersion(String versionSlug)
   {
      this.projectVersion = versionSlug;
   }

}
