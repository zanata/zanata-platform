package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PutProjectCommand;

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
    * Flies project slug/ID
    * 
    * @parameter expression="${flies.project.slug}"
    * @required
    */
   @SuppressWarnings("unused")
   private String projectSlug;

   /**
    * Flies project name
    * 
    * @parameter expression="${flies.project.name}"
    * @required
    */
   @SuppressWarnings("unused")
   private String projectName;

   /**
    * Flies project description
    * 
    * @parameter expression="${flies.project.desc}"
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
      getCommand().setProjectSlug(id);
   }

   public void setProjectName(String name)
   {
      getCommand().setProjectName(name);
   }

   public void setProjectDesc(String desc)
   {
      getCommand().setProjectDesc(desc);
   }

}
