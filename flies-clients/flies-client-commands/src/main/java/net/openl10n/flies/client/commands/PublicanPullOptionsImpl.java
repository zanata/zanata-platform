package net.openl10n.flies.client.commands;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class PublicanPullOptionsImpl extends ConfigurableProjectOptionsImpl implements PublicanPullOptions
{
   private File dstDir;

   @Override
   public FliesCommand initCommand()
   {
      return new PublicanPullCommand(this);
   }

   @Override
   public String getCommandName()
   {
      return "publican-pull";
   }

   @Override
   public String getCommandDescription()
   {
      return "Pulls translated text from Flies.";
   }

   @Option(aliases = { "-d" }, name = "--dst", metaVar = "DIR", required = true, usage = "Base directory for publican files (with subdirectory \"pot\" and locale directories)")
   @Override
   public void setDstDir(File dstDir)
   {
      this.dstDir = dstDir;
   }

   @Override
   public File getDstDir()
   {
      return dstDir;
   }

}
