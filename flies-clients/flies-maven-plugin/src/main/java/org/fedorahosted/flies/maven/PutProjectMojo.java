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
    * @parameter expression="${flies.proj}"
    * @required
    */
   @SuppressWarnings("unused")
   private String proj;

   /**
    * Flies project name
    * 
    * @parameter expression="${flies.proj.name}"
    * @required
    */
   @SuppressWarnings("unused")
   private String name;

   /**
    * Flies project description
    * 
    * @parameter expression="${flies.proj.desc}"
    * @required
    */
   @SuppressWarnings("unused")
   private String desc;

   public PutProjectMojo() throws Exception
   {
      super(new PutProjectCommand());
   }

   public void setProj(String id)
   {
      getCommand().setProj(id);
   }

   public void setName(String name)
   {
      getCommand().setName(name);
   }

   public void setDesc(String desc)
   {
      getCommand().setDesc(desc);
   }

}
