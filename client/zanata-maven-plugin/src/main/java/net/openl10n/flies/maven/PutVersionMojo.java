package net.openl10n.flies.maven;

import net.openl10n.flies.client.commands.PutVersionCommand;
import net.openl10n.flies.client.commands.PutVersionOptions;

/**
 * Creates or updates a Zanata project version.
 * 
 * @goal putversion
 * @requiresProject false
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutVersionMojo extends ConfigurableMojo implements PutVersionOptions
{

   /**
    * ID of Zanata project
    * 
    * @parameter expression="${zanata.version.project}"
    * @required
    */
   private String versionProject;

   /**
    * Project version ID
    * 
    * @parameter expression="${zanata.version.slug}"
    * @required
    */
   private String versionSlug;


   public PutVersionMojo() throws Exception
   {
      super();
   }

   public PutVersionCommand initCommand()
   {
      return new PutVersionCommand(this);
   }

   public String getVersionProject()
   {
      return versionProject;
   }

   public void setVersionProject(String versionProject)
   {
      this.versionProject = versionProject;
   }

   public String getVersionSlug()
   {
      return versionSlug;
   }

   public void setVersionSlug(String versionSlug)
   {
      this.versionSlug = versionSlug;
   }

}
