package org.zanata.client.ant.po;

import java.io.File;


import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ArgsUtil;
import org.zanata.client.commands.PublicanPullCommand;
import org.zanata.client.commands.PublicanPullOptions;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;

public class DownloadPoTask extends ConfigurableProjectTask implements PublicanPullOptions
{
   private String projectConfig = "zanata.xml";

   private String project;
   private String projectVersion;
   private LocaleList locales;
   private File dstDir;
   private File dstDirPot;
   private boolean exportPot;

   public static void main(String[] args)
   {
      DownloadPoTask task = new DownloadPoTask();
      ArgsUtil.processArgs(args, task);
   }

   @Override
   public String getCommandName()
   {
      return "downloadpo";
   }

   @Override
   public String getCommandDescription()
   {
      return "Downloads a Publican project's PO/POT files from the server after translation, to allow document generation";
   }

   @Override
   public ZanataCommand initCommand()
   {
      return new PublicanPullCommand(this);
   }

   public String getProj()
   {
      return project;
   }

   @Option(name = "--project", metaVar = "PROJ", usage = "Project ID.  This value is required unless specified in zanata.xml.")
   public void setProj(String projectSlug)
   {
      this.project = projectSlug;
   }

   @Override
   @Option(name = "--project-config", metaVar = "FILENAME", usage = "Project configuration file, eg zanata.xml", required = false)
   public void setProjectConfig(String projectConfig)
   {
      this.projectConfig = projectConfig;
   }

   @Override
   public String getProjectVersion()
   {
      return projectVersion;
   }

   @Override
   @Option(name = "--project-version", metaVar = "VER", usage = "Project version ID  This value is required unless specified in zanata.xml.")
   public void setProjectVersion(String versionSlug)
   {
      this.projectVersion = versionSlug;
   }

   @Override
   public String getProjectConfig()
   {
      return projectConfig;
   }

   @Override
   public LocaleList getLocales()
   {
      return locales;
   }

   @Override
   public void setLocales(LocaleList locales)
   {
      this.locales = locales;
   }

   @Override
   public void setDstDir(File dstDir)
   {
      this.dstDir = dstDir;
      if (dstDirPot == null)
         dstDirPot = new File(dstDir, "pot");
   }

   @Override
   public File getDstDir()
   {
      return dstDir;
   }

   @Override
   public void setDstDirPot(File dstDirPot)
   {
      this.dstDirPot = dstDirPot;
   }

   @Override
   public File getDstDirPot()
   {
      return dstDirPot;
   }

   @Override
   public boolean getExportPot()
   {
      return exportPot;
   }

   @Override
   @Option(name = "--export-pot", usage = "Export source text from the server to local POT files")
   public void setExportPot(boolean exportPot)
   {
      this.exportPot = exportPot;
   }

}
