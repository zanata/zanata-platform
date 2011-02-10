package net.openl10n.flies.client.commands;

import java.io.File;

import org.kohsuke.args4j.Option;

public interface PublicanPushOptions extends ConfigurableProjectOptions
{

   @Option(aliases = { "-s" }, name = "--src", metaVar = "DIR", required = true, usage = "Base directory for publican files (with subdirectory \"pot\" and optional locale directories)")
   public void setSrcDir(File srcDir);

   public File getSrcDir();

   public void setSrcDirPot(File srcDirPot);

   public File getSrcDirPot();

   @Option(aliases = { "-l" }, name = "--src-lang", usage = "Language of source (defaults to en-US)")
   public void setSourceLang(String sourceLang);

   public String getSourceLang();

   @Option(name = "--import-po", usage = "Import translations from local PO files to Flies, overwriting or erasing existing translations (DANGER!)")
   public void setImportPo(boolean importPo);

   public boolean getImportPo();

   @Option(name = "--validate", usage = "Validate XML before sending request to server")
   public void setValidate(boolean validate);
   public boolean getValidate();

}