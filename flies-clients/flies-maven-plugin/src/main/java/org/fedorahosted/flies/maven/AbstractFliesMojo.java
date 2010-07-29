package org.fedorahosted.flies.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.fedorahosted.flies.client.config.FliesConfig;

public abstract class AbstractFliesMojo extends AbstractMojo
{

   private JAXBContext jc = JAXBContext.newInstance(FliesConfig.class);

   private Unmarshaller unmarshaller = jc.createUnmarshaller();

   /**
    * Client configuration file for Flies.
    * 
    * @parameter expression="${flies.client.config}"
    *            default-value="${user.home}/.config/flies.ini"
    */
   protected File userConfig;

   /**
    * Project configuration file for Flies client.
    * 
    * @parameter expression="${flies.project.config}"
    *            default-value="${basedir}/src/main/config/flies.xml"
    */
   protected File projectConfig;

   /**
    * Base URL for the Flies server. Defaults to the value in flies.xml (if
    * present), or else to flies.ini.
    * 
    * @parameter expression="${flies.url}"
    */
   protected URL url;

   /**
    * Username for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    * 
    * @parameter expression="${flies.username}"
    */
   protected String username;

   /**
    * API key for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    * 
    * @parameter expression="${flies.key}"
    */
   protected String key;

   /**
    * Whether to enable debug mode. Defaults to the value in flies.ini.
    * 
    * @parameter expression="${flies.debug}"
    */
   protected boolean debug;
   private boolean debugSet;

   /**
    * Whether to display full information about errors (ie exception stack
    * traces). Defaults to the value in flies.ini.
    * 
    * @parameter expression="${flies.errors}"
    */
   protected boolean errors;
   private boolean errorsSet;

   public AbstractFliesMojo() throws Exception
   {
   }

   public void setDebug(boolean debug)
   {
      this.debugSet = true;
      this.debug = debug;
   }

   public void setErrors(boolean errors)
   {
      this.errorsSet = true;
      this.errors = errors;
   }

   protected void loadConfig() throws Exception
   {
      FliesConfig fliesConfig = (FliesConfig) unmarshaller.unmarshal(projectConfig);
      applyProjectConfig(fliesConfig);

      DataConfiguration dataConfig = new DataConfiguration(new HierarchicalINIConfiguration(userConfig));
      applyConfig(dataConfig);
   }

   private void applyProjectConfig(FliesConfig config)
   {
      if (url == null)
      {
         url = config.getUrl();
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

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      try
      {
         loadConfig();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new MojoExecutionException("error loading Flies user config", e);
      }
      getLog().info(getClass().getSimpleName());
   }
}
