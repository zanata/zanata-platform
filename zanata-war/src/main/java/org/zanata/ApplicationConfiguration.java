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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.security.AuthenticationType;

@Name("applicationConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
@BypassInterceptors
public class ApplicationConfiguration implements Serializable
{

   private static final String EMAIL_APPENDER_NAME = "zanata.log.appender.email";
   public static final String EVENT_CONFIGURATION_CHANGED = "zanata.configuration.changed";

   private static final Log log = Logging.getLog(ApplicationConfiguration.class);
   private static final long serialVersionUID = -4970657841198107092L;

   private static final ZanataSMTPAppender smtpAppenderInstance = new ZanataSMTPAppender();

   private Map<String, String> configValues = new HashMap<String, String>();
   
   private boolean debug;
   private boolean hibernateStatistics = false;
   private int authenticatedSessionTimeoutMinutes = 0;
   private String version;
   private String buildTimestamp;
   private boolean enableCopyTrans = true;
   private AuthenticationType authType;
   private boolean useDefaultConfig = false;

   public ApplicationConfiguration()
   {
      this(false);
   }

   public ApplicationConfiguration(boolean useDefaultConfig)
   {
      this.useDefaultConfig = useDefaultConfig;
   }

   @Observer( { EVENT_CONFIGURATION_CHANGED })
   @Create
   public void load()
   {
      log.info("Reloading configuration");
      Map<String, String> configValues = new HashMap<String, String>();
      setDefaults(configValues);
      
      if( !this.useDefaultConfig ) 
      {
         ApplicationConfigurationDAO applicationConfigurationDAO = (ApplicationConfigurationDAO) Component.getInstance(ApplicationConfigurationDAO.class, ScopeType.STATELESS);
         List<HApplicationConfiguration> storedConfigValues = applicationConfigurationDAO.findAll();
         for (HApplicationConfiguration value : storedConfigValues)
         {
            configValues.put(value.getKey(), value.getValue());
            log.debug("Setting value {0} to {1}", value.getKey(), value.getValue());
         }
      }
      this.configValues = configValues;

      this.applyLoggingConfiguration();
   }

   private void setDefaults(Map<String, String> map)
   {
      map.put(HApplicationConfiguration.KEY_REGISTER, "/zanata/account/register");
      map.put(HApplicationConfiguration.KEY_HOST, "http://localhost:8080/zanata");
      map.put(HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS, "no-reply@redhat.com");
      map.put(HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL, "WARN");
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
         smtpAppenderInstance.setLayout(new HTMLLayout());
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
      return configValues.get(HApplicationConfiguration.KEY_HOST);
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
      return configValues.get(HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS);
   }

   public String getHomeContent()
   {
      return configValues.get(HApplicationConfiguration.KEY_HOME_CONTENT);
   }

   public String getHelpContent()
   {
      return configValues.get(HApplicationConfiguration.KEY_HELP_CONTENT);
   }
   
   public String getLoginConfigUrl()
   {
      return configValues.get(HApplicationConfiguration.KEY_LOGINCONFIG_URL);
   }
   
   public boolean isInternalAuth()
   {
      return this.authType != null && this.authType == AuthenticationType.INTERNAL;
   }
   
   public boolean isFedoraOpenIdAuth() 
   {
      return this.authType != null && this.authType == AuthenticationType.FEDORA_OPENID;
   }
   
   public boolean isKerberosAuth()
   {
      return this.authType != null && this.authType == AuthenticationType.KERBEROS;
   }
   
   public String getAuthenticationType()
   {
      String authTypeStr = AuthenticationType.JAAS.toString();
      
      if( this.authType != null )
      {
         authTypeStr = this.authType.toString();
      }
      return authTypeStr;
   }

   public boolean isDebug()
   {
      return debug;
   }

   public boolean isHibernateStatistics()
   {
      return hibernateStatistics;
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
