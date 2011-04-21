package org.zanata.maven;

import org.zanata.client.commands.PutProjectCommand;
import org.zanata.client.commands.PutProjectOptions;

/**
 * Creates or updates a Zanata project.
 * 
 * @goal putproject
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutProjectMojo extends ConfigurableMojo implements PutProjectOptions
{

   /**
    * Project slug/ID
    * 
    * @parameter expression="${zanata.project.slug}"
    * @required
    */
   private String projectSlug;

   /**
    * Project name
    * 
    * @parameter expression="${zanata.project.name}"
    * @required
    */
   private String projectName;

   /**
    * Project description
    * 
    * @parameter expression="${zanata.project.desc}"
    * @required
    */
   private String projectDesc;

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


}
