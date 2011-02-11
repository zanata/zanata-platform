package net.openl10n.flies.client.ant.po;

import java.io.File;
import java.net.URL;

import org.kohsuke.args4j.Option;

public abstract class ConfigurableTask extends FliesTask
{
   /**
    * Client configuration file for Flies.
    */
   private File userConfig = new File(System.getProperty("user.home"), ".config/flies.ini");

   /**
    * Base URL for the Flies server. Defaults to the value in flies.xml (if
    * present), or else to flies.ini.
    */
   private URL url;

   /**
    * Username for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    */
   private String username;

   /**
    * API key for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    */
   private String key;

   public String getKey()
   {
      return key;
   }

   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)")
   public void setKey(String key)
   {
      this.key = key;
   }

   public URL getUrl()
   {
      return url;
   }

   @Option(name = "--url", metaVar = "URL", usage = "Flies base URL, eg http://flies.example.com/flies/")
   public void setUrl(URL url)
   {
      this.url = url;
   }

   @Option(name = "--user-config", metaVar = "FILE", usage = "Flies user configuration, eg /home/user/.config/flies.ini")
   public void setUserConfig(File userConfig)
   {
      this.userConfig = userConfig;
   }

   public String getUsername()
   {
      return username;
   }

   @Option(name = "--username", metaVar = "USER", usage = "Flies user name")
   public void setUsername(String username)
   {
      this.username = username;
   }

   public File getUserConfig()
   {
      return userConfig;
   }

}
