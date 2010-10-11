package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.ConfigurableProjectOptions;
import net.openl10n.flies.client.config.LocaleList;

/**
 * Base class for Flies mojos which support configuration by the user's
 * flies.ini and by a project's flies.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableProjectMojo extends ConfigurableMojo implements ConfigurableProjectOptions
{

   // @formatter:off
   /*
    * Note: The following fields are only here to hold Maven's @parameter 
    * markup, since all the setter methods actually delegate to the 
    * FliesCommand.  @parameter should work on setter methods - see
    * http://www.sonatype.com/books/mvnref-book/reference/writing-plugins-sect-param-annot.html
    * - but it doesn't.
    */
   // @formatter:on  

   /**
    * Project configuration file for Flies client.
    * 
    * @parameter expression="${flies.projectConfig}"
    *            default-value="${basedir}/flies.xml"
    */
   private String projectConfig;

   /**
    * Project slug (id) within Flies server.
    * 
    * @parameter expression="${flies.project}"
    */
   private String project;

   /**
    * Project version slug (id) within Flies server.
    * 
    * @parameter expression="${flies.projectVersion}"
    */
   private String projectVersion;

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
