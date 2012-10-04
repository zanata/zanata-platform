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
package org.zanata;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.ServletContexts;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.log4j.ZanataHTMLLayout;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.security.AuthenticationType;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

@Name("applicationConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
@BypassInterceptors
public class ApplicationConfiguration implements Serializable
{

   private static final Log log = Logging.getLog(ApplicationConfiguration.class);
   private static final long serialVersionUID = -4970657841198107092L;

   private static final String EMAIL_APPENDER_NAME = "zanata.log.appender.email";
   public static final String EVENT_CONFIGURATION_CHANGED = "zanata.configuration.changed";

   private static final String KEY_AUTH_POLICY = "zanata.security.auth.policy";
   private static final String KEY_ADMIN_USERS = "zanata.security.admin.users";

   private static final String[] allConfigKeys = new String[]
      {
         HApplicationConfiguration.KEY_ADMIN_EMAIL,
         HApplicationConfiguration.KEY_DOMAIN,
         HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS,
         HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS,
         HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL,
         HApplicationConfiguration.KEY_HELP_CONTENT,
         HApplicationConfiguration.KEY_HOME_CONTENT,
         HApplicationConfiguration.KEY_HOST,
         HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL,
         HApplicationConfiguration.KEY_REGISTER
      };

   // Resource Bundles
   private static ResourceBundle externalConfig;
   private static ResourceBundle defaultConfig;

   private static final ZanataSMTPAppender smtpAppenderInstance = new ZanataSMTPAppender();

   private Map<String, String> configValues = new HashMap<String, String>();
   
   private boolean debug;
   private int authenticatedSessionTimeoutMinutes = 0;
   private String version;
   private String buildTimestamp;
   private boolean enableCopyTrans = true;
   private Map<AuthenticationType, String> loginModuleNames = new HashMap<AuthenticationType, String>();
   private Set<String> adminUsers = new HashSet<String>();

   @Observer( { EVENT_CONFIGURATION_CHANGED })
   @Create
   public void load()
   {
      log.info("Reloading configuration");
      Map<String, String> configValues = new HashMap<String, String>();
      setDefaults(configValues);

      ApplicationConfigurationDAO applicationConfigurationDAO = (ApplicationConfigurationDAO) Component.getInstance(ApplicationConfigurationDAO.class, ScopeType.STATELESS);
      List<HApplicationConfiguration> storedConfigValues = applicationConfigurationDAO.findAll();
      for (HApplicationConfiguration value : storedConfigValues)
      {
         configValues.put(value.getKey(), value.getValue());
         log.debug("Setting value {0} to {1}", value.getKey(), value.getValue());
      }
      this.configValues = configValues;

      this.loadExternalConfig();
      this.validateConfiguration();
      this.applyLoggingConfiguration();
   }

   private void loadExternalConfig()
   {
      ResourceBundle config = getExternalConfig();

      // Authentication policies
      for( AuthenticationType authType : AuthenticationType.values() )
      {
         String key = KEY_AUTH_POLICY + "." + authType.name().toLowerCase();
         if( config.containsKey( key ) )
         {
            loginModuleNames.put( authType, config.getString(key) );
         }
      }

      // Admin users
      if( config.containsKey( KEY_ADMIN_USERS ) )
      {
         String userList = config.getString( KEY_ADMIN_USERS );

         for( String userName : userList.split(",") )
         {
            adminUsers.add( userName.trim() );
         }
      }
   }

   /**
    * Validates that there are no invalid values set on the zanata configuration
    */
   private void validateConfiguration()
   {
      // Validate that only internal / openid authentication is enabled at once
      if( loginModuleNames.size() > 2 )
      {
         throw new RuntimeException("Multiple invalid authentication types present in zanata.properties");
      }
      else if( loginModuleNames.size() == 2 )
      {
         // Internal and Open id are the only allowed combined authentication types
         if( !(loginModuleNames.containsKey(AuthenticationType.INTERNAL) && loginModuleNames.containsKey(AuthenticationType.INTERNAL) ) )
         {
            throw new RuntimeException("Multiple invalid authentication types present in zanata.properties");
         }
      }
      else if( loginModuleNames.size() < 1)
      {
         throw new RuntimeException("At least one authentication type must be configured in zanata.properties");
      }
   }

   private static final ResourceBundle getExternalConfig()
   {
      if( externalConfig == null )
      {
         try
         {
            externalConfig = ResourceBundle.getBundle("zanata");
         }
         catch (MissingResourceException e)
         {
            log.error("zanata.properties file not found.");
            throw e;
         }
      }
      return externalConfig;
   }

   private static final ResourceBundle getDefaultConfig()
   {
      if( defaultConfig == null )
      {
         try
         {
            defaultConfig = ResourceBundle.getBundle("zanata-defaultconfig");
         }
         catch (MissingResourceException e)
         {
            defaultConfig = new ListResourceBundle()
            {
               @Override
               protected Object[][] getContents()
               {
                  return new Object[0][];
               }
            };
            log.info("zanata-defaultconfig.properties not found. Default configuration won't be bootstrapped.");
         }
      }
      return defaultConfig;
   }

   private void setDefaults(Map<String, String> map)
   {
      for( String key : allConfigKeys )
      {
         if( getDefaultConfig().containsKey(key) )
         {
            map.put(key, getDefaultConfig().getString(key));
         }
      }
   }

   /**
    * Apply logging configuration.
    */
   public void applyLoggingConfiguration()
   {
      final Logger rootLogger = Logger.getRootLogger();

      if( isEmailLogAppenderEnabled() )
      {
         // NB: This appender uses Seam's email configuration (no need for host or port)
         smtpAppenderInstance.setName(EMAIL_APPENDER_NAME);
         smtpAppenderInstance.setFrom(getFromEmailAddr());
         smtpAppenderInstance.setTo(this.configValues.get(HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL));
         smtpAppenderInstance.setSubject("%p log message from Zanata at " + this.getServerPath());
         smtpAppenderInstance.setLayout(new ZanataHTMLLayout());
         //smtpAppenderInstance.setLayout(new PatternLayout("%-5p [%c] %m%n"));
         smtpAppenderInstance.setThreshold(Level.toLevel(getEmailLogLevel()));
         smtpAppenderInstance.setTimeout(60); // will aggregate identical messages within 60 sec periods
         smtpAppenderInstance.activateOptions();

         // Safe to add more than once
         rootLogger.addAppender(smtpAppenderInstance);
         log.info("Email log appender is enabled [level: " + smtpAppenderInstance.getThreshold().toString() + "]");
      }
      else
      {
         rootLogger.removeAppender(EMAIL_APPENDER_NAME);
         log.info("Email log appender is disabled.");
      }
   }

   public String getRegisterPath()
   {
      return configValues.get(HApplicationConfiguration.KEY_REGISTER);
   }

   public String getServerPath()
   {
      String configuredValue = configValues.get(HApplicationConfiguration.KEY_HOST);
      // Try to determine a server path if one is not configured
      if( configuredValue == null )
      {
         HttpServletRequest request = ServletContexts.instance().getRequest();
         configuredValue = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
      }
      return configuredValue;
   }

   public String getByKey(String key)
   {
      return configValues.get(key);
   }

   public String getDomainName()
   {
      return configValues.get(HApplicationConfiguration.KEY_DOMAIN);
   }

   public List<String> getAdminEmail()
   {
      String s = configValues.get(HApplicationConfiguration.KEY_ADMIN_EMAIL);
      if (s == null || s.trim().length() == 0)
      {
         return new ArrayList<String>();
      }
      String[] ss = s.trim().split("\\s*,\\s*");
      return new ArrayList<String>(Arrays.asList(ss));
   }

   public String getFromEmailAddr()
   {
      String emailAddr = configValues.get(HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS);
      if( emailAddr == null )
      {
         try
         {
            emailAddr = "no-reply@" + InetAddress.getLocalHost().getHostName();
         }
         catch (UnknownHostException e)
         {
            emailAddr = "no-reply@zanatainstance.org";
         }
      }
      return emailAddr;
   }

   public String getHomeContent()
   {
      return configValues.get(HApplicationConfiguration.KEY_HOME_CONTENT);
   }

   public String getHelpContent()
   {
      return configValues.get(HApplicationConfiguration.KEY_HELP_CONTENT);
   }
   
   public boolean isInternalAuth()
   {
      return this.loginModuleNames.containsKey( AuthenticationType.INTERNAL );
   }
   
   public boolean isOpenIdAuth()
   {
      return this.loginModuleNames.containsKey( AuthenticationType.OPENID );
   }
   
   public boolean isKerberosAuth()
   {
      return this.loginModuleNames.containsKey( AuthenticationType.KERBEROS );
   }

   public boolean isJaasAuth()
   {
      return this.loginModuleNames.containsKey( AuthenticationType.JAAS );
   }
   
   public String getLoginModuleName( AuthenticationType authType )
   {
      return this.loginModuleNames.get( authType );
   }

   public boolean isDebug()
   {
      return debug;
   }

   public int getAuthenticatedSessionTimeoutMinutes()
   {
      return authenticatedSessionTimeoutMinutes;
   }

   public String getVersion()
   {
      return version;
   }
   
   void setVersion( String version )
   {
      this.version = version;
   }

   public String getBuildTimestamp()
   {
      return buildTimestamp;
   }
   
   void setBuildTimestamp( String buildTimestamp )
   {
      this.buildTimestamp = buildTimestamp;
   }

   public boolean getEnableCopyTrans()
   {
      return enableCopyTrans;
   }

   public Set<String> getAdminUsers()
   {
      return new HashSet<String>( adminUsers );
   }

   public boolean isEmailLogAppenderEnabled()
   {
      String strVal = configValues.get(HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS);

      if(strVal == null)
      {
         return false;
      }
      else
      {
         return Boolean.parseBoolean( strVal );
      }
   }

   public List<String> getLogDestinationEmails()
   {
      String s = configValues.get(HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL);
      if (s == null || s.trim().length() == 0)
      {
         return new ArrayList<String>();
      }
      String[] ss = s.trim().split("\\s*,\\s*");
      return new ArrayList<String>(Arrays.asList(ss));
   }

   public String getEmailLogLevel()
   {
      return configValues.get(HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL);
   }

   public String getPiwikUrl()
   {
      return configValues.get(HApplicationConfiguration.KEY_PIWIK_URL);
   }

   public String getPiwikIdSite()
   {
      return configValues.get(HApplicationConfiguration.KEY_PIWIK_IDSITE);
   }
}
