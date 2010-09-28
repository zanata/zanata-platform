package net.openl10n.flies.client.commands;

import org.kohsuke.args4j.Option;

/**
 * Base options for Flies commands which support configuration by the user's
 * flies.ini and by a project's flies.xml
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public interface ConfigurableProjectOptions extends ConfigurableOptions
{

   public String getProject();

   @Option(name = "--project", metaVar = "PROJ", usage = "Flies project ID/slug.  This value is required unless specified in flies.xml.")
   public void setProject(String projectSlug);

   @Option(name = "--project-config", metaVar = "FILENAME", usage = "Flies project configuration, eg flies.xml", required = false)
   public void setProjectConfig(String projectConfig);

   public String getProjectVersion();

   @Option(name = "--project-version", metaVar = "VER", usage = "Flies project version ID  This value is required unless specified in flies.xml.")
   public void setProjectVersion(String versionSlug);

   public String getProjectConfig();

}