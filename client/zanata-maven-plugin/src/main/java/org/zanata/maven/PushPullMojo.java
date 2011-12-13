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
public abstract class PushPullMojo<O extends PushPullOptions> extends ConfigurableProjectMojo<O> implements PushPullOptions
{

   @Override
   protected void runCommand() throws Exception
   {
      if (skip)
      {
         getLog().info("skipping");
         return;
      }

   }

   @Override
   public boolean isRootModule()
   {
      return session.getExecutionRootDirectory().equalsIgnoreCase(basedir.toString());
   }

   @Override
   public String getCurrentModule()
   {
      if (project == null || !enableModules)
         return "";
      return project.getGroupId() + ":" + project.getArtifactId();
   }

   @Override
   public Set<String> getAllModules()
   {
      Set<String> localModules = new HashSet<String>();
      for (MavenProject module : reactorProjects)
      {
         String modID = module.getGroupId() + ':' + module.getArtifactId();
         localModules.add(modID);
      }
      getLog().info("modules in the reactor: " + localModules);
      return localModules;
   }

   /**
    * Whether module processing should be enabled
    * @parameter expression="${zanata.enableModules}"
    */
   private boolean enableModules = false;

   /**
    * @parameter expression="${project}"
    * @readonly
    */
   private MavenProject project;

   /**
    * Dry run: don't change any data, on the server or on the filesystem.
    * @parameter expression="${dryRun}"
    */
   private boolean dryRun = false;

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
   private MavenSession session;

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

   /**
    * @return the dryRun
    */
   @Override
   public boolean isDryRun()
   {
      return dryRun;
   }

   @Override
   public boolean getEnableModules()
   {
      return enableModules;
   }

   @Override
   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public File getTransDir()
   {
      return transDir;
   }

}
