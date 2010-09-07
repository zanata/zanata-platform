package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PutVersionCommand;

/**
 * Creates or updates a Flies project version.
 * 
 * @goal putversion
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutVersionMojo extends ConfigurableMojo<PutVersionCommand>
{

   /**
    * Flies project version's project
    * 
    * @parameter expression="${flies.version.project}"
    * @required
    */
   @SuppressWarnings("unused")
   private String versionProject;

   /**
    * Flies project version ID
    * 
    * @parameter expression="${flies.version.slug}"
    * @required
    */
   @SuppressWarnings("unused")
   private String versionSlug;

   /**
    * Flies project version name
    * 
    * @parameter expression="${flies.version.name}"
    * @required
    */
   @SuppressWarnings("unused")
   private String versionName;

   /**
    * Flies project version description
    * 
    * @parameter expression="${flies.version.desc}"
    * @required
    */
   @SuppressWarnings("unused")
   private String versionDesc;

   public PutVersionMojo() throws Exception
   {
      super(new PutVersionCommand());
   }

   public void setVersionProject(String id)
   {
      getCommand().setVersionProject(id);
   }

   public void setVersionSlug(String id)
   {
      getCommand().setVersionSlug(id);
   }

   public void setVersionName(String name)
   {
      getCommand().setVersionName(name);
   }

   public void setVersionDesc(String desc)
   {
      getCommand().setVersionDesc(desc);
   }

}
