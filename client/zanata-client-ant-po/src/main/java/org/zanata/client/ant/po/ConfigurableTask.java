package org.zanata.client.ant.po;

import java.io.File;
import java.net.URL;

import org.kohsuke.args4j.Option;

public abstract class ConfigurableTask extends ZanataTask
{
   /**
    * Client configuration file.
    */
   private File userConfig = new File(System.getProperty("user.home"), ".config/zanata.ini");

   /**
    * Base URL for the server. Defaults to the value in zanata.xml.
    */
   private URL url;

   /**
    * Username for accessing the REST API. Defaults to the value in
    * zanata.ini.
    */
   private String username;

   /**
    * API key for accessing the REST API. Defaults to the value in
    * zanata.ini.
    */
   private String key;

   public String getKey()
   {
      return key;
   }

   @Option(name = "--key", metaVar = "KEY", usage = "API key (from user's profile page)")
   public void setKey(String key)
   {
      this.key = key;
   }

   public URL getUrl()
   {
      return url;
   }

   @Option(name = "--url", metaVar = "URL", usage = "Base URL, eg http://zanata.example.com/zanata/")
   public void setUrl(URL url)
   {
      this.url = url;
   }

   @Option(name = "--user-config", metaVar = "FILE", usage = "User configuration, eg /home/user/.config/zanata.ini")
   public void setUserConfig(File userConfig)
   {
      this.userConfig = userConfig;
   }

   public String getUsername()
   {
      return username;
   }

   @Option(name = "--username", metaVar = "USER", usage = "Username")
   public void setUsername(String username)
   {
      this.username = username;
   }

   public File getUserConfig()
   {
      return userConfig;
   }

}
