package org.fedorahosted.flies.maven;

import org.fedorahosted.flies.client.commands.PutProjectCommand;

/**
 * Creates or updates a Flies project.
 * 
 * @goal putproject
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutProjectMojo extends ConfigurableMojo<PutProjectCommand>
{

   /**
    * Flies project ID
    * 
    * @parameter expression="${flies.projectSlug}"
    * @required
    */
   @SuppressWarnings("unused")
   private String projectSlug;

   /**
    * Flies project name
    * 
    * @parameter expression="${flies.projectName}"
    * @required
    */
   @SuppressWarnings("unused")
   private String projectName;

   /**
    * Flies project description
    * 
    * @parameter expression="${flies.projectDesc}"
    * @required
    */
   @SuppressWarnings("unused")
   private String projectDesc;

   public PutProjectMojo() throws Exception
   {
      super(new PutProjectCommand());
   }

   public void setProjectSlug(String id)
   {
      getCommand().setProj(id);
   }

   public void setProjectName(String name)
   {
      getCommand().setName(name);
   }

   public void setProjectDesc(String desc)
   {
      getCommand().setDesc(desc);
   }

}
