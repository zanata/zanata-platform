package org.zanata.maven;

import org.zanata.client.commands.PutProjectCommand;
import org.zanata.client.commands.PutProjectOptions;

/**
 * Creates or updates a Zanata project.
 * 
 * @goal put-project
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutProjectMojo extends ConfigurableMojo<PutProjectOptions> implements PutProjectOptions
{

   /**
    * Project slug/ID
    * 
    * @parameter expression="${zanata.projectSlug}"
    * @required
    */
   private String projectSlug;

   /**
    * Project name
    * 
    * @parameter expression="${zanata.projectName}"
    * @required
    */
   private String projectName;

   /**
    * Project description
    * 
    * @parameter expression="${zanata.projectDesc}"
    * @required
    */
   private String projectDesc;
   
   /**
    * Default Project type {utf8properties, properties, gettext, podir, xliff,
    * xml, raw}
    * 
    * @parameter expression="${zanata.defaultProjectType}"
    * @required
    */
   private String defaultProjectType;

   public PutProjectMojo() throws Exception
   {
      super();
   }

   public PutProjectCommand initCommand()
   {
      return new PutProjectCommand(this);
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getProjectName()
   {
      return projectName;
   }

   public void setProjectName(String projectName)
   {
      this.projectName = projectName;
   }

   public String getProjectDesc()
   {
      return projectDesc;
   }

   public void setProjectDesc(String projectDesc)
   {
      this.projectDesc = projectDesc;
   }

   public String getDefaultProjectType()
   {
      return defaultProjectType;
   }

   public void setDefaultProjectType(String defaultProjectType)
   {
      this.defaultProjectType = defaultProjectType;
   }

   @Override
   public String getCommandName()
   {
      return "put-project";
   }
}
