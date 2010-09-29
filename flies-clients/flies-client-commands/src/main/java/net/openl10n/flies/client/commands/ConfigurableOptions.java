package net.openl10n.flies.client.commands;

import java.io.File;
import java.net.URL;

import org.kohsuke.args4j.Option;

/**
 * Base options for Flies commands which supports configuration by the user's
 * flies.ini
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public interface ConfigurableOptions extends BasicOptions
{

   /**
    * API key for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    */
   public String getKey();

   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)")
   public void setKey(String key);

   /**
    * Base URL for the Flies server. Defaults to the value in flies.xml (if
    * present), or else to flies.ini.
    */
   public URL getUrl();

   @Option(name = "--url", metaVar = "URL", usage = "Flies base URL, eg http://flies.example.com/flies/")
   public void setUrl(URL url);

   /**
    * Client configuration file for Flies.
    */
   public File getUserConfig();

   @Option(name = "--user-config", metaVar = "FILE", usage = "Flies user configuration, eg /home/user/.config/flies.ini")
   public void setUserConfig(File userConfig);

   /**
    * Username for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    */
   public String getUsername();

   @Option(name = "--username", metaVar = "USER", usage = "Flies user name")
   public void setUsername(String username);


}