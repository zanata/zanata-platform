package net.openl10n.flies.client.commands;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class PublicanPushOptionsImpl extends ConfigurableProjectOptionsImpl implements PublicanPushOptions
{
   private File srcDir;
   private File srcDirPot;

   private String sourceLang = "en-US";

   private boolean importPo;

   private boolean validate;

   public PublicanPushOptionsImpl()
   {
      super();
   }

   @Override
   public String getCommandName()
   {
      return "publican-push";
   }

   @Override
   public String getCommandDescription()
   {
      return "Publishes publican source text to a Flies project version so that it can be translated.";
   }

   @Override
   public PublicanPushCommand initCommand()
   {
      return new PublicanPushCommand(this);
   }

   @Override
   @Option(aliases = { "-s" }, name = "--src", metaVar = "DIR", required = true, usage = "Base directory for publican files (with subdirectory \"pot\" and optional locale directories)")
   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
      if (srcDirPot == null)
         srcDirPot = new File(srcDir, "pot");
   }

   @Override
   public void setSrcDirPot(File srcDirPot)
   {
      this.srcDirPot = srcDirPot;
   }

   @Override
   @Option(aliases = { "-l" }, name = "--src-lang", usage = "Language of source (defaults to en-US)")
   public void setSourceLang(String sourceLang)
   {
      this.sourceLang = sourceLang;
   }

   @Override
   @Option(name = "--import-po", usage = "Import translations from local PO files to Flies, overwriting or erasing existing translations (DANGER!)")
   public void setImportPo(boolean importPo)
   {
      this.importPo = importPo;
   }

   @Override
   @Option(name = "--validate", usage = "Validate XML before sending request to server")
   public void setValidate(boolean validate)
   {
      this.validate = validate;
   }


   @Override
   public boolean getValidate()
   {
      return validate;
   }

   @Override
   public boolean getImportPo()
   {
      return importPo;
   }

   @Override
   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public File getSrcDirPot()
   {
      return srcDirPot;
   }

   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

}
