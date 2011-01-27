package net.openl10n.flies.client.ant.po;

import java.io.PrintStream;

import net.openl10n.flies.client.FliesClient;
import net.openl10n.flies.client.commands.AppAbortStrategy;
import net.openl10n.flies.client.commands.PutUserOptionsImpl;
import net.openl10n.flies.client.commands.SystemExitStrategy;

public class PoTool extends FliesClient
{
   public static void main(String[] args)
   {
      PoTool tool = new PoTool(new SystemExitStrategy(), System.out, System.err);
      tool.processArgs(args);
   }

   public PoTool(AppAbortStrategy strategy, PrintStream out, PrintStream err)
   {
      super(strategy, out, err);
      getOptionsMap().clear();
      getOptionsMap().put("putuser", new PutUserOptionsImpl());
      getOptionsMap().put("createproj", new CreateProjectTask());
      getOptionsMap().put("createiter", new CreateIterationTask());
      getOptionsMap().put("upload", new UploadPoTask());
      getOptionsMap().put("download", new DownloadPoTask());
   }

   public String getCommandName()
   {
      return "flies-publican";
   }

   public String getCommandDescription()
   {
      return "Flies command-line client for publican projects";
   }

}
