/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.fedorahosted.flies.client.command;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.fedorahosted.flies.client.config.FliesConfig;
import org.kohsuke.args4j.Option;

/**
 * Base class for Flies commands which supports configuration by the user's
 * flies.ini
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableCommand
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

   /**
    * Whether to enable debug mode. Defaults to the value in flies.ini.
    */
   private boolean debug;
   private boolean debugSet;

   /**
    * Whether to display full information about errors (ie exception stack
    * traces). Defaults to the value in flies.ini.
    */
   private boolean errors;
   private boolean errorsSet;

   private boolean help;

   public ConfigurableCommand()
   {
   }

   /**
    * Loads the config files (controlled by the property userConfig) to supply
    * any values which haven't already been set. (May be overridden by subclass
    * to load other configuration.)
    * 
    * @throws Exception
    */
   public void initConfig() throws Exception
   {
      if (userConfig != null)
      {
         if (userConfig.exists())
         {
            // TODO use a ConfigurationFactory
            DataConfiguration dataConfig = new DataConfiguration(new HierarchicalINIConfiguration(userConfig));
            applyConfig(dataConfig);
         }
         else
         {
            System.err.printf("Flies user config file '%s' not found; ignoring.", userConfig);
         }
      }
   }

   private void applyConfig(DataConfiguration config)
   {
      if (!debugSet)
         debug = config.getBoolean("debug", false);
      if (!errorsSet)
         errors = config.getBoolean("errors", false);
      if (key == null)
         key = config.getString("key", null);
      if (url == null)
         url = config.getURL("url", null);
      if (username == null)
         username = config.getString("username", null);
   }

   public boolean getDebug()
   {
      return debug;
   }

   @Option(name = "--debug", aliases = { "-x" }, usage = "Enable debug mode")
   public void setDebug(boolean debug)
   {
      this.debugSet = true;
      this.debug = debug;
   }

   public boolean getErrors()
   {
      return errors;
   }

   @Option(name = "--errors", aliases = { "-e" }, usage = "Output full execution error messages")
   public void setErrors(boolean errors)
   {
      this.errorsSet = true;
      this.errors = errors;
   }

   public boolean getHelp()
   {
      return this.help;
   }

   @Option(name = "--help", aliases = { "-h", "-help" }, usage = "Display this help and exit")
   public void setHelp(boolean help)
   {
      this.help = help;
   }

   public String getKey()
   {
      return key;
   }

   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)", required = true)
   public void setKey(String key)
   {
      this.key = key;
   }

   public URL getUrl()
   {
      return url;
   }

   @Option(name = "--url", metaVar = "URL", usage = "Flies base URL, eg http://flies.example.com/flies/", required = true)
   public void setUrl(URL url)
   {
      this.url = url;
   }

   @Option(name = "--user-config", metaVar = "FILE", usage = "Flies user configuration, eg /home/user/.config/flies.ini", required = false)
   public void setUserConfig(File userConfig)
   {
      this.userConfig = userConfig;
   }

   public String getUsername()
   {
      return username;
   }

   @Option(name = "--user", metaVar = "USER", usage = "Flies user name", required = true)
   public void setUsername(String username)
   {
      this.username = username;
   }

   public abstract void run() throws Exception;
}
