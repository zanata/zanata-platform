package org.zanata.client.commands;

import java.io.File;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.VersionUtility;
import org.zanata.client.config.ConfigUtil;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.ZanataConfig;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.rest.client.ZanataProxyFactory;

public class OptionsUtil
{
   private static final Logger log = LoggerFactory.getLogger(OptionsUtil.class);

   /**
    * Loads the config files (controlled by the property userConfig) to supply
    * any values which haven't already been set.
    * 
    * @throws Exception
    */
   public static void applyConfigFiles(ConfigurableOptions opts) throws ConfigurationException, JAXBException
   {
      if (opts instanceof ConfigurableProjectOptions)
      {
         ConfigurableProjectOptions projOpts = (ConfigurableProjectOptions) opts;
         if (projOpts.getProjectConfig() != null)
         {
            JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            String projectConfigName = projOpts.getProjectConfig();
            File projectConfigFile = new File(projectConfigName);
            if (!projectConfigFile.isAbsolute())
            {
	           String userDir = System.getProperty("user.dir");
	           File projectDir = new File(userDir);
	           while (projectDir != null && !(projectConfigFile = new File(projectDir, projectConfigName)).exists())
	           {
	              projectDir = projectDir.getParentFile();
	           }
            }

            if (projectConfigFile.exists())
            {
               log.info("Loading project config from {}", projectConfigFile);
               ZanataConfig projectConfig = (ZanataConfig) unmarshaller.unmarshal(projectConfigFile);
               // local project config is supposed to override user's zanata.ini,
               // so we apply it first
               applyProjectConfig(projOpts, projectConfig);
            }
            else
            {
               log.warn("Project config file '{}' not found; ignoring.", projectConfigName);
            }
         }
      }
      if (opts.getUserConfig() != null)
      {
         if (opts.getUserConfig().exists())
         {
            log.info("Loading user config from {}", opts.getUserConfig());
            HierarchicalINIConfiguration dataConfig = new HierarchicalINIConfiguration(opts.getUserConfig());
            applyUserConfig(opts, dataConfig);
         }
         else
         {
            System.err.printf("User config file '%s' not found; ignoring.\n", opts.getUserConfig());
         }
      }
   }

   /**
    * Applies values from the project configuration unless they have been set
    * directly via parameters.
    * 
    * @param config
    */
   private static void applyProjectConfig(ConfigurableProjectOptions opts, ZanataConfig config)
   {
      if (opts.getProj() == null)
      {
         opts.setProj(config.getProject());
      }
      if (opts.getUrl() == null)
      {
         opts.setUrl(config.getUrl());
      }
      if (opts.getProjectVersion() == null)
      {
         opts.setProjectVersion(config.getProjectVersion());
      }
      LocaleList locales = config.getLocales();
      opts.setLocales(locales);
   }

   /**
    * Applies values from the user's personal configuration unless they have
    * been set directly (by parameters or by project configuration).
    * 
    * @param config
    */
   private static void applyUserConfig(ConfigurableOptions opts, HierarchicalINIConfiguration config)
   {
      if (!opts.isDebugSet())
      {
         Boolean debug = config.getBoolean("defaults.debug", null);
         if (debug != null)
            opts.setDebug(debug);
      }

      if (!opts.isErrorsSet())
      {
         Boolean errors = config.getBoolean("defaults.errors", null);
         if (errors != null)
            opts.setErrors(errors);
      }

      if (!opts.isQuietSet())
      {
         Boolean quiet = config.getBoolean("defaults.quiet", null);
         if (quiet != null)
            opts.setQuiet(quiet);
      }
      if ((opts.getUsername() == null || opts.getKey() == null) && opts.getUrl() != null)
      {
         SubnodeConfiguration servers = config.getSection("servers");
         String prefix = ConfigUtil.findPrefix(servers, opts.getUrl());
         if (prefix != null)
         {
            if (opts.getUsername() == null)
            {
               opts.setUsername(servers.getString(prefix + ".username", null));
            }
            if (opts.getKey() == null)
            {
               opts.setKey(servers.getString(prefix + ".key", null));
            }
         }
      }
   }

   public static ZanataProxyFactory createRequestFactory(ConfigurableOptions opts)
   {
      try
      {
         if (opts.getUrl() == null)
            throw new ConfigException("Server URL must be specified");
         if (opts.getUsername() == null)
            throw new ConfigException("Username must be specified");
         if (opts.getKey() == null)
            throw new ConfigException("API key must be specified");
         return new ZanataProxyFactory(opts.getUrl().toURI(), opts.getUsername(), opts.getKey(), VersionUtility.getAPIVersionInfo());
      }
      catch (URISyntaxException e)
      {
         throw new ConfigException(e);
      }
   }

}
