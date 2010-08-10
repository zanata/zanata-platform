package org.fedorahosted.flies.maven;

import java.io.File;

import org.fedorahosted.flies.client.commands.ConfigurableProjectCommand;

/**
 * Base class for Flies mojos which support configuration by the user's
 * flies.ini and by a project's flies.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableProjectMojo<C extends ConfigurableProjectCommand> extends ConfigurableMojo<C>
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
   @SuppressWarnings("unused")
   private File projectConfig;

   /**
    * Project slug (id) within Flies server.
    * 
    * @parameter expression="${flies.project}"
    */
   @SuppressWarnings("unused")
   private String project;

   /**
    * Project version slug (id) within Flies server.
    * 
    * @parameter expression="${flies.projectVersion}"
    */
   @SuppressWarnings("unused")
   private String projectVersion;

   public ConfigurableProjectMojo(C command)
   {
      super(command);
   }

   public void setProjectConfig(File projectConfig)
   {
      getCommand().setProjectConfig(projectConfig.getPath());
   }

   public void setProject(String projectSlug)
   {
      getCommand().setProject(projectSlug);
   }

   public void setProjectVersion(String versionSlug)
   {
      getCommand().setProjectVersion(versionSlug);
   }

}
