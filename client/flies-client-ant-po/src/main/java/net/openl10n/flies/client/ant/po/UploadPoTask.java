package net.openl10n.flies.client.ant.po;

import java.io.File;

import org.kohsuke.args4j.Option;

import net.openl10n.flies.client.commands.ArgsUtil;
import net.openl10n.flies.client.commands.PublicanPushCommand;
import net.openl10n.flies.client.commands.PublicanPushOptions;

public class UploadPoTask extends ConfigurableProjectTask implements PublicanPushOptions
{
   // private static final Logger log =
   // LoggerFactory.getLogger(UploadPoTask.class);
   private File srcDir;
   private File srcDirPot;

   private String sourceLang = "en-US";

   private boolean importPo;
   private boolean copyTrans = true;
   private boolean validate;

   public static void main(String[] args)
   {
      UploadPoTask task = new UploadPoTask();
      ArgsUtil.processArgs(args, task);
   }

   @Override
   public String getCommandName()
   {
      return "uploadpo";
   }

   @Override
   public String getCommandDescription()
   {
      return "Uploads a Publican project's PO/POT files to the server for translation";
   }

   public PublicanPushCommand initCommand()
   {
      return new PublicanPushCommand(this);
   }

   @Option(aliases = { "-s" }, name = "--src", metaVar = "DIR", required = true, usage = "Base directory for publican files (with subdirectory \"pot\" and optional locale directories)")
   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
      if (srcDirPot == null)
         srcDirPot = new File(srcDir, "pot");
   }

   public void setSrcDirPot(File srcDirPot)
   {
      this.srcDirPot = srcDirPot;
   }

   @Option(aliases = { "-l" }, name = "--src-lang", usage = "Language of source (defaults to en-US)")
   public void setSourceLang(String sourceLang)
   {
      this.sourceLang = sourceLang;
   }

   @Option(name = "--import-po", usage = "Import translations from local PO files to the server, overwriting or erasing existing translations (DANGER!)")
   public void setImportPo(boolean importPo)
   {
      this.importPo = importPo;
   }

   @Option(name = "--validate", usage = "Validate XML before sending request to server")
   public void setValidate(boolean validate)
   {
      this.validate = validate;
   }

   public boolean getValidate()
   {
      return validate;
   }

   public boolean getImportPo()
   {
      return importPo;
   }

   @Override
   public boolean getCopyTrans()
   {
      return copyTrans;
   }
   
   public void setCopyTrans(boolean copyTrans)
   {
      this.copyTrans = copyTrans;
   }

   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public File getSrcDirPot()
   {
      return srcDirPot;
   }

   public String getSourceLang()
   {
      return sourceLang;
   }


}
