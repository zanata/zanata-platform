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
public abstract class ConfigurableOptionsImpl extends BasicOptionsImpl implements ConfigurableOptions
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

   public ConfigurableOptionsImpl()
   {
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)")
   public void setKey(String key)
   {
      this.key = key;
   }

   @Override
   public URL getUrl()
   {
      return url;
   }

   @Override
   @Option(name = "--url", metaVar = "URL", usage = "Flies base URL, eg http://flies.example.com/flies/")
   public void setUrl(URL url)
   {
      this.url = url;
   }

   @Override
   @Option(name = "--user-config", metaVar = "FILE", usage = "Flies user configuration, eg /home/user/.config/flies.ini")
   public void setUserConfig(File userConfig)
   {
      this.userConfig = userConfig;
   }

   @Override
   public String getUsername()
   {
      return username;
   }

   @Override
   @Option(name = "--username", metaVar = "USER", usage = "Flies user name")
   public void setUsername(String username)
   {
      this.username = username;
   }

   @Override
   public File getUserConfig()
   {
      return userConfig;
   }

}
