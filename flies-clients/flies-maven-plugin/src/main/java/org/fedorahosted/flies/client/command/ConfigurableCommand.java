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
   private File userConfig;

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
      // TODO use a ConfigurationFactory
      DataConfiguration dataConfig = new DataConfiguration(new HierarchicalINIConfiguration(userConfig));
      applyConfig(dataConfig);
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

   public void setDebug(boolean debug)
   {
      this.debugSet = true;
      this.debug = debug;
   }

   public boolean getErrors()
   {
      return errors;
   }

   public void setErrors(boolean errors)
   {
      this.errorsSet = true;
      this.errors = errors;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   public URL getUrl()
   {
      return url;
   }

   public void setUrl(URL url)
   {
      this.url = url;
   }

   public void setUserConfig(File userConfig)
   {
      this.userConfig = userConfig;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public abstract void run() throws Exception;
}
