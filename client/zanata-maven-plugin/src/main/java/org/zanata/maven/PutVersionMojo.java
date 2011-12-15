package org.zanata.maven;

import org.zanata.client.commands.PutVersionCommand;
import org.zanata.client.commands.PutVersionOptions;

/**
 * Creates or updates a Zanata project version.
 * 
 * @goal putversion
 * @requiresProject false
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutVersionMojo extends ConfigurableMojo<PutVersionOptions> implements PutVersionOptions
{

   /**
    * ID of Zanata project
    * 
    * @parameter expression="${zanata.versionProject}"
    * @required
    */
   private String versionProject;

   /**
    * Project version ID
    * 
    * @parameter expression="${zanata.versionSlug}"
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
