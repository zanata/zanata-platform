package org.zanata.client.ant.po;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ArgsUtil;
import org.zanata.client.commands.PutVersionCommand;
import org.zanata.client.commands.PutVersionOptions;
import org.zanata.client.commands.ZanataCommand;

public class CreateIterationTask extends ConfigurableTask implements PutVersionOptions
{
   // private static final Logger log =
   // LoggerFactory.getLogger(CreateIterationTask.class);

   private String proj;
   private String iter;

   public static void main(String[] args)
   {
      CreateIterationTask task = new CreateIterationTask();
      ArgsUtil.processArgs(args, task);
   }

   @Override
   public String getCommandName()
   {
      return "createiter";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates a project version";
   }


   @Option(name = "--proj", metaVar = "PROJ", usage = "Project ID", required = true)
   public void setVersionProject(String id)
   {
      this.proj = id;
   }

   @Option(name = "--version-slug", metaVar = "VER", usage = "Project version ID", required = true)
   public void setVersionSlug(String id)
   {
      this.iter = id;
   }

   @Override
   public ZanataCommand initCommand()
   {
      return new PutVersionCommand(this);
   }


   public String getVersionProject()
   {
      return this.proj;
   }

   @Override
   public String getVersionSlug()
   {
      return this.iter;
   }

}
