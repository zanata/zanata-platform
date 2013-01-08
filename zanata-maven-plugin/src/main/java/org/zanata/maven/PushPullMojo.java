package org.zanata.maven;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.zanata.client.commands.PushPullOptions;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;

/**
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public abstract class PushPullMojo<O extends PushPullOptions> extends ConfigurableProjectMojo<O> implements PushPullOptions
{

   /**
    * Separator used between components of the module ID
    */
   private static final char MODULE_SEPARATOR = '/';

   private static final char MODULE_SUFFIX = '/';

   @Override
   protected void runCommand() throws Exception
   {
      if (skip)
      {
         getLog().info("skipping");
         return;
      }
      super.runCommand();
   }

   @Override
   public boolean isRootModule()
   {
      return project.isExecutionRoot();
   }

   @Override
   public String getCurrentModule()
   {
      if (project == null || !enableModules)
      {
         return "";
      }
      else
      {
         return toModuleID(project);
      }
   }

   @Override
   public String getModuleSuffix()
   {
      return "" + MODULE_SUFFIX;
   }

   @Override
   public String getDocNameRegex()
   {
      return "^([^/]+/[^" + MODULE_SUFFIX + "]+)" + MODULE_SUFFIX + "(.+)";
   }

   //   @Override
   public Set<String> getAllModules()
   {
      Set<String> localModules = new LinkedHashSet<String>();
      for (MavenProject module : reactorProjects)
      {
         String modID = toModuleID(module);
         localModules.add(modID);
      }
      return localModules;
   }

   private String toModuleID(MavenProject module)
   {
      return module.getGroupId() + MODULE_SEPARATOR + module.getArtifactId();
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
    * @parameter expression="${zanata.skip}"
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

   /**
    * Locales to push to/pull from the server.
    * By default all locales in zanata.xml will be pushed/pulled.
    * Usage: -Dzanata.locales=locale1,locale2,locale3
    *
    * @parameter expression="${zanata.locales}"
    */
   private String[] locales;

   private LocaleList effectiveLocales;

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

   /**
    * Override the default {@link org.zanata.maven.ConfigurableProjectMojo#getLocaleMapList()} method as the push
    * command can have locales specified via command line.
    *
    * @return The locale map list taking into account the global locales in zanata.xml as well as the command line
    * argument ones.
    */
   @Override
   public LocaleList getLocaleMapList()
   {
      if( effectiveLocales == null )
      {
         if(locales != null && locales.length > 0)
         {
            // filter the locales that are specified in both the global config and the parameter list
            effectiveLocales = new LocaleList();

            for( String locale : locales )
            {
               boolean foundLocale = false;
               for(LocaleMapping lm : super.getLocaleMapList())
               {
                  if( lm.getLocale().equals(locale) ||
                        (lm.getMapFrom() != null && lm.getMapFrom().equals( locale )) )
                  {
                     effectiveLocales.add(lm);
                     foundLocale = true;
                     break;
                  }
               }

               if(!foundLocale)
               {
                  throw new ConfigException("Specified locale '" + locale + "' was not found in zanata.xml!" );
               }
            }
         }
         else
         {
            effectiveLocales = super.getLocaleMapList();
         }
      }

      return effectiveLocales;
   }

}
