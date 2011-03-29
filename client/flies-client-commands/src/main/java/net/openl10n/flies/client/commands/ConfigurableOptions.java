package net.openl10n.flies.client.commands;

import java.io.File;
import java.net.URL;

import org.kohsuke.args4j.Option;

/**
 * Base options for commands which supports configuration by the user's
 * zanata.ini
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public interface ConfigurableOptions extends BasicOptions
{

   /**
    * API key for accessing the REST API. Defaults to the value in
    * zanata.ini.
    */
   public String getKey();

   @Option(name = "--key", metaVar = "KEY", usage = "API key (from user's profile page)")
   public void setKey(String key);

   /**
    * Base URL for the server. Defaults to the value in zanata.xml.
    */
   public URL getUrl();

   @Option(name = "--url", metaVar = "URL", usage = "Base URL, eg http://zanata.example.com/zanata/")
   public void setUrl(URL url);

   /**
    * Client configuration file.
    */
   public File getUserConfig();

   @Option(name = "--user-config", metaVar = "FILE", usage = "User configuration, eg /home/user/.config/zanata.ini")
   public void setUserConfig(File userConfig);

   /**
    * Username for accessing the REST API. Defaults to the value in
    * zanata.ini.
    */
   public String getUsername();

   @Option(name = "--username", metaVar = "USER", usage = "Username")
   public void setUsername(String username);


}