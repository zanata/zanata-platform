package org.zanata.client.commands;

import org.kohsuke.args4j.Option;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectOptionsImpl extends ConfigurableOptionsImpl implements PutProjectOptions
{

   private String projectSlug;
   private String projectName;
   private String projectDesc;

   @Override
   public String getCommandName()
   {
      return "putproject";
   }

   @Override
   public String getCommandDescription()
   {
      return "Creates or updates a Zanata project.";
   }

   @Override
   public PutProjectCommand initCommand()
   {
      return new PutProjectCommand(this);
   }

   @Override
   @Option(name = "--project-slug", metaVar = "PROJ", usage = "Project ID", required = true)
   public void setProjectSlug(String id)
   {
      this.projectSlug = id;
   }

   @Override
   @Option(name = "--project-name", metaVar = "NAME", required = true, usage = "Project name")
   public void setProjectName(String name)
   {
      this.projectName = name;
   }

   @Override
   @Option(name = "--project-desc", metaVar = "DESC", required = true, usage = "Project description")
   public void setProjectDesc(String desc)
   {
      this.projectDesc = desc;
   }

   @Override
   public String getProjectSlug()
   {
      return projectSlug;
   }

   @Override
   public String getProjectDesc()
   {
      return projectDesc;
   }

   @Override
   public String getProjectName()
   {
      return projectName;
   }

}
