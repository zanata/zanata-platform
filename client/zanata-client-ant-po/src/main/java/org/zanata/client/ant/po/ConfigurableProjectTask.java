package org.zanata.client.ant.po;


import java.io.File;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.config.LocaleList;

public abstract class ConfigurableProjectTask extends ConfigurableTask implements ConfigurableProjectOptions
{
   // FIXME when running in Ant, interpret relative to getProject().getBaseDir()
   private File projectConfig = new File("zanata.xml");

   private String project;
   private String projectVersion;
   private String projectType;
   private LocaleList locales;

   public String getProj()
   {
      return project;
   }

   @Option(name = "--project", metaVar = "PROJ", usage = "Project ID.  This value is required unless specified in zanata.xml.")
   public void setProj(String projectSlug)
   {
      this.project = projectSlug;
   }

   @Option(name = "--project-config", metaVar = "FILENAME", usage = "Project configuration, eg zanata.xml", required = false)
   public void setProjectConfig(File projectConfig)
   {
      this.projectConfig = projectConfig;
   }

   public String getProjectVersion()
   {
      return projectVersion;
   }

   @Option(name = "--project-version", metaVar = "VER", usage = "Project version ID  This value is required unless specified in zanata.xml.")
   public void setProjectVersion(String versionSlug)
   {
      this.projectVersion = versionSlug;
   }

   public String getProjectType()
   {
      return projectType;
   }

   @Option(name = "--project-type", metaVar = "PROJTYPE", usage = "Type of project ('properties' = Java .properties, 'podir' = publican-style gettext directories)")
   public void setProjectType(String projectType)
   {
      this.projectType = projectType;
   }

   public File getProjectConfig()
   {
      return projectConfig;
   }

   public LocaleList getLocales()
   {
      return locales;
   }

   public void setLocales(LocaleList locales)
   {
      this.locales = locales;
   }
}
