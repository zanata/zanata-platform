package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PutProjectCommand;
import net.openl10n.flies.client.commands.PutProjectOptions;

/**
 * Creates or updates a Flies project.
 * 
 * @goal putproject
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutProjectMojo extends ConfigurableMojo implements PutProjectOptions
{

   /**
    * Flies project slug/ID
    * 
    * @parameter expression="${flies.project.slug}"
    * @required
    */
   private String projectSlug;

   /**
    * Flies project name
    * 
    * @parameter expression="${flies.project.name}"
    * @required
    */
   private String projectName;

   /**
    * Flies project description
    * 
    * @parameter expression="${flies.project.desc}"
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
