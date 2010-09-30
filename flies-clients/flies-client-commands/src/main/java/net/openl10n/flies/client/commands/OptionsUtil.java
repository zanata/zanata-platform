package net.openl10n.flies.client.commands;

import java.io.File;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.openl10n.flies.client.config.FliesConfig;
import net.openl10n.flies.client.config.LocaleList;
import net.openl10n.flies.client.exceptions.ConfigException;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            JAXBContext jc = JAXBContext.newInstance(FliesConfig.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            String projectConfig = projOpts.getProjectConfig();
            File projectConfigFile = null;
            String userDir = System.getProperty("user.dir");
            File projectDir = new File(userDir);
            while (projectDir != null && !(projectConfigFile = new File(projectDir, projectConfig)).exists())
            {
               projectDir = projectDir.getParentFile();
            }

            if (projectConfigFile.exists())
            {
               log.info("Loading flies project config from {}", projectConfigFile);
               FliesConfig fliesConfig = (FliesConfig) unmarshaller.unmarshal(projectConfigFile);
               // local project config is supposed to override user's flies.ini,
               // so we apply it first
               applyProjectConfig(projOpts, fliesConfig);
            }
            else
            {
               log.warn("Flies project config file '{}' not found in '{}' or parent directories; ignoring.", projectConfig, userDir);
            }
         }
      }
      if (opts.getUserConfig() != null)
      {
         if (opts.getUserConfig().exists())
         {
            log.info("Loading flies user config from {}", opts.getUserConfig());
            DataConfiguration dataConfig = new DataConfiguration(new HierarchicalINIConfiguration(opts.getUserConfig()));
            applyUserConfig(opts, dataConfig);
         }
         else
         {
            System.err.printf("Flies user config file '%s' not found; ignoring.\n", opts.getUserConfig());
         }
      }
   }

   /**
    * Applies values from the project configuration unless they have been set
    * directly via parameters.
    * 
    * @param config
    */
   private static void applyProjectConfig(ConfigurableProjectOptions opts, FliesConfig config)
   {
      if (opts.getProject() == null)
      {
         opts.setProject(config.getProject());
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
   private static void applyUserConfig(ConfigurableOptions opts, DataConfiguration config)
   {
      if (!opts.isDebugSet())
      {
         Boolean debug = config.getBoolean("flies.debug", null);
         if (debug != null)
            opts.setDebug(debug);
      }

      if (!opts.isErrorsSet())
      {
         Boolean errors = config.getBoolean("flies.errors", null);
         if (errors != null)
            opts.setErrors(errors);
      }

      if (!opts.isQuietSet())
      {
         Boolean quiet = config.getBoolean("flies.quiet", null);
         if (quiet != null)
            opts.setQuiet(quiet);
      }

      if (opts.getUrl() == null)
         opts.setUrl(config.getURL("flies.url", null));
      if (opts.getUsername() == null)
         opts.setUsername(config.getString("flies.username", null));
      if (opts.getKey() == null)
         opts.setKey(config.getString("flies.key", null));
   }

   public static FliesClientRequestFactory createRequestFactory(ConfigurableOptions opts)
   {
      try
      {
         if (opts.getUrl() == null)
            throw new ConfigException("Flies URL must be specified");
         if (opts.getUsername() == null)
            throw new ConfigException("Flies username must be specified");
         if (opts.getKey() == null)
            throw new ConfigException("Flies key must be specified");
         return new FliesClientRequestFactory(opts.getUrl().toURI(), opts.getUsername(), opts.getKey());
      }
      catch (URISyntaxException e)
      {
         throw new ConfigException(e);
      }
   }

}
