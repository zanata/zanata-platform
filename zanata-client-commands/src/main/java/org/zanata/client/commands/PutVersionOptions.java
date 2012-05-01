package org.zanata.client.commands;

import org.kohsuke.args4j.Option;

public interface PutVersionOptions extends ConfigurableOptions
{

   @Option(name = "--version-project", metaVar = "PROJ", usage = "ID of Zanata project", required = true)
   public void setVersionProject(String id);

   @Option(name = "--version-slug", metaVar = "VER", usage = "Project version ID", required = true)
   public void setVersionSlug(String id);

   public String getVersionProject();

   public String getVersionSlug();

}