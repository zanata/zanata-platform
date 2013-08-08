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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
import org.zanata.config.DatabaseBackedConfig;
import org.zanata.config.JndiBackedConfig;
import org.zanata.log4j.ZanataHTMLLayout;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.security.AuthenticationType;
import com.google.common.base.Objects;

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

   private DatabaseBackedConfig databaseBackedConfig;
   private JndiBackedConfig jndiBackedConfig;

   private static final ZanataSMTPAppender smtpAppenderInstance = new ZanataSMTPAppender();

   private boolean debug;
   private int authenticatedSessionTimeoutMinutes = 0;
   private String version;
   private String buildTimestamp;
   private boolean enableCopyTrans = true;
   private Map<AuthenticationType, String> loginModuleNames = new HashMap<AuthenticationType, String>();
   private Set<String> adminUsers = new HashSet<String>();

   // set by component.xml
   private String webAssetsVersion = "";

   @Observer( { EVENT_CONFIGURATION_CHANGED })
   @Create
   public void load()
   {
      log.info("Reloading configuration");
      databaseBackedConfig = (DatabaseBackedConfig)Component.getInstance(DatabaseBackedConfig.class);
      jndiBackedConfig = (JndiBackedConfig)Component.getInstance(JndiBackedConfig.class);

      this.loadLoginModuleNames();
      this.validateConfiguration();
      this.applyLoggingConfiguration();
   }

   /**
    * Loads the accepted login module (JAAS) names from the underlying configuration
    */
   private void loadLoginModuleNames()
   {
      for( String policyName : jndiBackedConfig.getEnabledAuthenticationPolicies() )
      {
         AuthenticationType authType = AuthenticationType.valueOf( policyName.toUpperCase() );
         loginModuleNames.put(authType, jndiBackedConfig.getAuthPolicyName( policyName ));
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
         throw new RuntimeException("Multiple invalid authentication types present in Zanata configuration.");
      }
      else if( loginModuleNames.size() == 2 )
      {
         // Internal and Open id are the only allowed combined authentication types
         if( !(loginModuleNames.containsKey(AuthenticationType.OPENID) && loginModuleNames.containsKey(AuthenticationType.INTERNAL) ) )
         {
            throw new RuntimeException("Multiple invalid authentication types present in Zanata configuration.");
         }
      }
      else if( loginModuleNames.size() < 1)
      {
         throw new RuntimeException("At least one authentication type must be configured in Zanata configuration.");
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
         smtpAppenderInstance.setTo(databaseBackedConfig.getLogEventsDestinationEmailAddress());
         // TODO use hostname, not URL
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
      return databaseBackedConfig.getRegistrationUrl();
   }

   public String getServerPath()
   {
      String configuredValue = databaseBackedConfig.getServerHost();
      // Try to determine a server path if one is not configured
      if( configuredValue == null )
      {
         HttpServletRequest request = ServletContexts.instance().getRequest();
         if( request != null )
         {
            configuredValue =
                  request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
         }
      }
      return configuredValue;
   }

   public String getDocumentFileStorageLocation()
   {
      return jndiBackedConfig.getDocumentFileStorageLocation();
   }

   public String getDomainName()
   {
      return databaseBackedConfig.getDomain();
   }

   public List<String> getAdminEmail()
   {
      String s = databaseBackedConfig.getAdminEmailAddress();
      if (s == null || s.trim().length() == 0)
      {
         return new ArrayList<String>();
      }
      String[] ss = s.trim().split("\\s*,\\s*");
      return new ArrayList<String>(Arrays.asList(ss));
   }

   public String getFromEmailAddr()
   {
      String emailAddr = null;

      // Look in the database first
      emailAddr = databaseBackedConfig.getFromEmailAddress();

      // Look in the properties file next
      if (emailAddr == null && jndiBackedConfig.getDefaultFromEmailAddress() != null)
      {
         emailAddr = jndiBackedConfig.getDefaultFromEmailAddress();
      }

      // Finally, just throw an Exception
      if( emailAddr == null )
      {
         throw new RuntimeException("'From' email address has not been defined in either zanata.properties or Zanata setup");
      }
      return emailAddr;
   }

   public String getHomeContent()
   {
      return databaseBackedConfig.getHomeContent();
   }
  
   public String getHelpContent()
   {
      return databaseBackedConfig.getHelpContent();
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
      String strVal = databaseBackedConfig.getShouldLogEvents();

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
      String s = databaseBackedConfig.getLogEventsDestinationEmailAddress();
      if (s == null || s.trim().length() == 0)
      {
         return new ArrayList<String>();
      }
      String[] ss = s.trim().split("\\s*,\\s*");
      return new ArrayList<String>(Arrays.asList(ss));
   }

   public String getEmailLogLevel()
   {
      return databaseBackedConfig.getEmailLogLevel();
   }

   public String getPiwikUrl()
   {
      return databaseBackedConfig.getPiwikUrl();
   }

   public String getPiwikIdSite()
   {
      return databaseBackedConfig.getPiwikSiteId();
   }

   public String getEmailServerHost()
   {
      String host = jndiBackedConfig.getSmtpHostName();

      // Default to localhost
      if( host == null )
      {
         host = "localhost";
      }
      return host;
   }

   public int getEmailServerPort()
   {
      String port = jndiBackedConfig.getSmtpPort();

      // Default to 25
      if( port == null )
      {
         port = "25";
      }
      return Integer.parseInt(port);
   }

   public String getEmailServerUsername()
   {
      return jndiBackedConfig.getSmtpUsername();
   }

   public String getEmailServerPassword()
   {
      return jndiBackedConfig.getSmtpPassword();
   }

   public boolean useEmailServerTls()
   {
      return jndiBackedConfig.getSmtpUsesTls() != null ? Boolean.parseBoolean(jndiBackedConfig.getSmtpUsesTls()) : false;
   }

   public boolean useEmailServerSsl()
   {
      return jndiBackedConfig.getStmpUsesSsl() != null ? Boolean.parseBoolean(jndiBackedConfig.getStmpUsesSsl()) : false;
   }

   public String getWebAssetsUrl()
   {
      return String.format("%s/%s/assets/css/style.css",
            Objects.firstNonNull(jndiBackedConfig.getWebAssetsUrlBase(), "//assets-zanata.rhcloud.com"), webAssetsVersion);
   }
}
