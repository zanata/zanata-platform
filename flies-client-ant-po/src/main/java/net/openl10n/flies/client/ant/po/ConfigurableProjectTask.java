package net.openl10n.flies.client.ant.po;

import net.openl10n.flies.client.config.LocaleList;

import org.kohsuke.args4j.Option;

public abstract class ConfigurableProjectTask extends ConfigurableTask
{
   private String projectConfig = "flies.xml";

   private String project;
   private String projectVersion;
   private LocaleList locales;

   public String getProj()
   {
      return project;
   }

   @Option(name = "--project", metaVar = "PROJ", usage = "Flies project ID/slug.  This value is required unless specified in flies.xml.")
   public void setProj(String projectSlug)
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

   public String getProjectConfig()
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
