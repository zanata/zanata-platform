package net.openl10n.flies.client.commands;

import org.kohsuke.args4j.Option;

public interface PutVersionOptions extends ConfigurableOptions
{

   @Option(name = "--version-project", metaVar = "PROJ", usage = "Flies project version's project", required = true)
   public void setVersionProject(String id);

   @Option(name = "--version-slug", metaVar = "VER", usage = "Flies project version ID", required = true)
   public void setVersionSlug(String id);

   @Option(name = "--version-name", metaVar = "NAME", usage = "Flies project version name", required = true)
   public void setVersionName(String name);

   @Option(name = "--version-desc", metaVar = "DESC", usage = "Flies project version description", required = true)
   public void setVersionDesc(String desc);

   public String getVersionProject();

   public String getVersionSlug();

   public String getVersionDesc();

   public String getVersionName();

}