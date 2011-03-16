package net.openl10n.flies.client.ant.po;

import net.openl10n.flies.client.commands.ArgsUtil;
import net.openl10n.flies.client.commands.FliesCommand;
import net.openl10n.flies.client.commands.PutVersionCommand;
import net.openl10n.flies.client.commands.PutVersionOptions;
import org.kohsuke.args4j.Option;

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
      return "Creates a project iteration in Flies";
   }


   @Option(name = "--proj", metaVar = "PROJ", usage = "Flies project ID", required = true)
   public void setVersionProject(String id)
   {
      this.proj = id;
   }

   @Option(name = "--version-slug", metaVar = "VER", usage = "Flies project version ID", required = true)
   public void setVersionSlug(String id)
   {
      this.iter = id;
   }

   @Override
   public FliesCommand initCommand()
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
