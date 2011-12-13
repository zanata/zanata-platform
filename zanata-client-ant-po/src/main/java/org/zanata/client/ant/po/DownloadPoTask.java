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
