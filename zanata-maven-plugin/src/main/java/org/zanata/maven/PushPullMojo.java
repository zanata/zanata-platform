package org.zanata.maven;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.zanata.client.commands.PushPullOptions;


/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public abstract class PushPullMojo<O extends PushPullOptions> extends ConfigurableProjectMojo<O>
{

   @Override
   protected void runCommand() throws Exception
   {
      if (skip)
      {
         getLog().info("skipping");
         return;
      }
      if (modules)
      {
         runModule();
      }
      else
      {
         //         getLog().info("run non-module");
         super.runCommand();
      }
   }

   private void runModule()
   {
      if (isRootModule())
      {
         runRootModule();
      }
      else
      {
         runSubmodule();
      }
   }

   private void runSubmodule()
   {
      // TODO Auto-generated method stub

   }

   private void runRootModule()
   {
      Set<String> localModules = new HashSet<String>();
      for (MavenProject module : reactorProjects)
      {
         String modID = module.getGroupId() + ':' + module.getArtifactId();
         localModules.add(modID);
      }
      getLog().info("modules in the reactor: " + localModules);

      // TODO get doc list from server
      // convert doc names to modules, add to serverModules
      // offer to delete documents from obsolete serverModules
   }

   private boolean isRootModule()
   {
      return session.getExecutionRootDirectory().equalsIgnoreCase(basedir.toString());
   }

   /**
    * @parameter expression="${zanata.modules}"
    */
   private boolean modules = false;

   /**
    * Base directory of the project.
    *
    * @parameter expression="${basedir}"
    * @required
    * @readonly
    */
   private File basedir;

   /**
    * The Maven Session.
    *
    * @parameter expression="${session}"
    * @required
    * @readonly
    */
   protected MavenSession session;

   /**
    * @parameter skip
    */
   private boolean skip;

   /**
    * The projects in the reactor.
    *
    * @parameter expression="${reactorProjects}"
    * @readonly
    */
   private List<MavenProject> reactorProjects;

   /**
    * Base directory for source-language files
    * 
    * @parameter expression="${zanata.srcDir}" default-value="."
    */
   private File srcDir;

   /**
    * Base directory for target-language files (translations)
    * 
    * @parameter expression="${zanata.transDir}" default-value="."
    */
   private File transDir;

   public PushPullMojo() throws Exception
   {
      super();
   }

   public boolean isModules()
   {
      return modules;
   }

   public File getSrcDir()
   {
      return srcDir;
   }

   public File getTransDir()
   {
      return transDir;
   }

}
