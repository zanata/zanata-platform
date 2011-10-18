package org.zanata.maven;

import org.zanata.client.commands.ConfigurableOptions;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.config.LocaleList;

/**
 * Base class for mojos which support configuration by the user's
 * zanata.ini and by a project's zanata.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableProjectMojo<O extends ConfigurableOptions> extends ConfigurableMojo<O> implements ConfigurableProjectOptions
{

   // @formatter:off
   /*
    * @parameter should work on setter methods - see
    * http://www.sonatype.com/books/mvnref-book/reference/writing-plugins-sect-param-annot.html
    * - but it doesn't.  So we have to put @parameter on the fields instead.
    */
   // @formatter:on  

   /**
    * Zanata project configuration file.
    * 
    * @parameter expression="${zanata.projectConfig}"
    *            default-value="${basedir}/zanata.xml"
    */
   private String projectConfig;

   /**
    * Project slug (id) within Zanata server.
    * 
    * @parameter expression="${zanata.project}"
    */
   private String project;

   /**
    * Project version slug (id) within Zanata server.
    * 
    * @parameter expression="${zanata.projectVersion}"
    */
   private String projectVersion;

   /**
    * Type of project ("properties" = Java .properties, "podir" = publican-style
    * gettext directories).
    * 
    * @parameter expression="${zanata.projectType}"
    */
   private String projectType;

   private LocaleList locales;

   public ConfigurableProjectMojo()
   {
      super();
   }

   @Override
   public String getProjectConfig()
   {
      return projectConfig;
   }

   @Override
   public void setProjectConfig(String projectConfig)
   {
      this.projectConfig = projectConfig;
   }

   @Override
   public String getProj()
   {
      return project;
   }

   @Override
   public void setProj(String project)
   {
      this.project = project;
   }

   @Override
   public String getProjectVersion()
   {
      return projectVersion;
   }

   @Override
   public void setProjectVersion(String projectVersion)
   {
      this.projectVersion = projectVersion;
   }

   @Override
   public String getProjectType()
   {
      return projectType;
   }

   @Override
   public void setProjectType(String projectType)
   {
      this.projectType = projectType;
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
