package net.openl10n.flies.client.ant.po;

import java.io.File;

import net.openl10n.flies.client.commands.ArgsUtil;
import net.openl10n.flies.client.commands.FliesCommand;
import net.openl10n.flies.client.commands.PublicanPullCommand;
import net.openl10n.flies.client.commands.PublicanPullOptions;
import net.openl10n.flies.client.config.LocaleList;

import org.kohsuke.args4j.Option;

public class DownloadPoTask extends ConfigurableProjectTask implements PublicanPullOptions
{
   private String projectConfig = "flies.xml";

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
      return "Downloads a Publican project's PO/POT files from Flies after translation, to allow document generation";
   }

   @Override
   public FliesCommand initCommand()
   {
      return new PublicanPullCommand(this);
   }

   public String getProj()
   {
      return project;
   }

   @Option(name = "--project", metaVar = "PROJ", usage = "Flies project ID/slug.  This value is required unless specified in flies.xml.")
   public void setProj(String projectSlug)
   {
      this.project = projectSlug;
   }

   @Override
   @Option(name = "--project-config", metaVar = "FILENAME", usage = "Flies project configuration, eg flies.xml", required = false)
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
   @Option(name = "--project-version", metaVar = "VER", usage = "Flies project version ID  This value is required unless specified in flies.xml.")
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
   @Option(name = "--export-pot", usage = "Export source text from Flies to local POT files")
   public void setExportPot(boolean exportPot)
   {
      this.exportPot = exportPot;
   }

}
