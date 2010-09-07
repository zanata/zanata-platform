package net.openl10n.flies.client.ant.po;

import java.io.PrintStream;

import net.openl10n.flies.client.FliesClient;
import net.openl10n.flies.client.commands.AppAbortStrategy;
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
      getCommandMap().clear();
      getCommandMap().put("putuser", PutUserTask.class);
      getCommandMap().put("createproj", CreateProjectTask.class);
      getCommandMap().put("createiter", CreateIterationTask.class);
      getCommandMap().put("upload", UploadPoTask.class);
      getCommandMap().put("download", DownloadPoTask.class);
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
