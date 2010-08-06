package org.fedorahosted.flies.client.ant.po;

import org.fedorahosted.flies.client.FliesClient;

public class PoTool extends FliesClient
{
   public static void main(String[] args) throws Exception
   {
      PoTool tool = new PoTool();
      tool.processArgs(args);
   }

   public PoTool()
   {
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
