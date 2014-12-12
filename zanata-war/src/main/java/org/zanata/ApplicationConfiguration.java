/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Level;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.web.ServletContexts;
import org.zanata.config.DatabaseBackedConfig;
import org.zanata.config.JaasConfig;
import org.zanata.config.JndiBackedConfig;
import org.zanata.events.ConfigurationChanged;
import org.zanata.log4j.ZanataHTMLLayout;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.security.AuthenticationType;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zanata.security.OpenIdLoginModule;

@Name("applicationConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
@Slf4j
public class ApplicationConfiguration implements Serializable {
    private static final long serialVersionUID = -4970657841198107092L;

    private static final String EMAIL_APPENDER_NAME =
            "zanata.log.appender.email";

    private static final String STYLESHEET_LOCAL_PATH = "/assets/css/style.min.css";

    @Getter
    private static final int defaultMaxFilesPerUpload = 100;

    @In
    private DatabaseBackedConfig databaseBackedConfig;
    @In
    private JndiBackedConfig jndiBackedConfig;
    @In
    private JaasConfig jaasConfig;

    private static final ZanataSMTPAppender smtpAppenderInstance =
            new ZanataSMTPAppender();

    @Getter
    private boolean debug;

    @Getter
    private int authenticatedSessionTimeoutMinutes = 0;

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private String buildTimestamp;

    @Getter
    @Setter
    private String scmDescribe;

    @Getter
    private boolean copyTransEnabled = true;

    private Map<AuthenticationType, String> loginModuleNames = Maps
            .newHashMap();

    private Set<String> adminUsers;

    private String webAssetsUrl;
    private String webAssetsStyleUrl;

    // set by component.xml
    private String webAssetsVersion = "";

    private Optional<String> openIdProvider; // Cache the OpenId provider

    private String serverPath;

    @Create
    public void load() {
        log.info("Reloading configuration");
        this.loadLoginModuleNames();
        this.validateConfiguration();
        this.applyLoggingConfiguration();
        this.loadJaasConfig();
    }

    @Observer(ConfigurationChanged.EVENT_NAME)
    public void resetConfigValue(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            ConfigurationChanged configChange) {
        String configName = configChange.getConfigKey();
        // Remove the value from all stores
        databaseBackedConfig.reset(configName);
        jndiBackedConfig.reset(configName);
    }

    /**
     * Loads the accepted login module (JAAS) names from the underlying
     * configuration
     */
    private void loadLoginModuleNames() {
        for (String policyName : jndiBackedConfig
                .getEnabledAuthenticationPolicies()) {
            AuthenticationType authType =
                    AuthenticationType.valueOf(policyName.toUpperCase());
            loginModuleNames.put(authType,
                    jndiBackedConfig.getAuthPolicyName(policyName));
        }
    }

    /**
     * Validates that there are no invalid values set on the zanata
     * configuration
     */
    private void validateConfiguration() {
        // Validate that only internal / openid authentication is enabled at
        // once
        if (loginModuleNames.size() > 2) {
            throw new RuntimeException(
                    "Multiple invalid authentication types present in Zanata configuration.");
        } else if (loginModuleNames.size() == 2) {
            // Internal and Open id are the only allowed combined authentication
            // types
            if (!(loginModuleNames.containsKey(AuthenticationType.OPENID) && loginModuleNames
                    .containsKey(AuthenticationType.INTERNAL))) {
                throw new RuntimeException(
                        "Multiple invalid authentication types present in Zanata configuration.");
            }
        } else if (loginModuleNames.size() < 1) {
            throw new RuntimeException(
                    "At least one authentication type must be configured in Zanata configuration.");
        }
    }

    /**
     * Apply logging configuration.
     */
    public void applyLoggingConfiguration() {
        // TODO is this still working with jboss logging?
        final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

        if (isEmailLogAppenderEnabled()) {
            // NB: This appender uses Seam's email configuration (no need for
            // host or port)
            smtpAppenderInstance.setName(EMAIL_APPENDER_NAME);
            smtpAppenderInstance.setFrom(getFromEmailAddr());
            smtpAppenderInstance.setTo(databaseBackedConfig
                    .getLogEventsDestinationEmailAddress());
            // TODO use hostname, not URL
            smtpAppenderInstance.setSubject("%p log message from Zanata at "
                    + this.getServerPath());
            smtpAppenderInstance.setLayout(new ZanataHTMLLayout());
            // smtpAppenderInstance.setLayout(new
            // PatternLayout("%-5p [%c] %m%n"));
            smtpAppenderInstance
                    .setThreshold(Level.toLevel(getEmailLogLevel()));
            smtpAppenderInstance.setTimeout(60); // will aggregate identical
                                                 // messages within 60 sec
                                                 // periods
            smtpAppenderInstance.activateOptions();

            // Safe to add more than once
            rootLogger.addAppender(smtpAppenderInstance);
            log.info("Email log appender is enabled [level: "
                    + smtpAppenderInstance.getThreshold().toString() + "]");
        } else {
            rootLogger.removeAppender(EMAIL_APPENDER_NAME);
            log.info("Email log appender is disabled.");
        }
    }

    /**
     * Load configuration pertaining to JAAS.
     */
    private void loadJaasConfig() {
        if (loginModuleNames.containsKey(AuthenticationType.OPENID)) {
            openIdProvider =
                    Optional.fromNullable(jaasConfig
                            .getAppConfigurationProperty(
                                    loginModuleNames
                                            .get(AuthenticationType.OPENID),
                                    OpenIdLoginModule.class,
                                    OpenIdLoginModule.OPEN_ID_PROVIDER_KEY));
        } else {
            openIdProvider = Optional.absent();
        }
    }

    public String getRegisterPath() {
        return databaseBackedConfig.getRegistrationUrl();
    }

    public String getServerPath() {
        String configuredValue = databaseBackedConfig.getServerHost();
        if (configuredValue != null) {
            serverPath = configuredValue;
        }
        // Try to determine a server path if one is not configured
        if (serverPath == null) {
            HttpServletRequest request =
                    ServletContexts.instance().getRequest();
            if (request != null) {
                serverPath =
                        request.getScheme() + "://" + request.getServerName()
                                + ":" + request.getServerPort()
                                + request.getContextPath();
            }
        }
        return serverPath;
    }

    public String getDocumentFileStorageLocation() {
        return jndiBackedConfig.getDocumentFileStorageLocation();
    }

    public String getDomainName() {
        return databaseBackedConfig.getDomain();
    }

    public List<String> getAdminEmail() {
        String s = databaseBackedConfig.getAdminEmailAddress();
        if (s == null || s.trim().length() == 0) {
            return new ArrayList<String>();
        }
        String[] ss = s.trim().split("\\s*,\\s*");
        return new ArrayList<String>(Arrays.asList(ss));
    }

    public String getFromEmailAddr() {
        String emailAddr = null;

        // Look in the database first
        emailAddr = databaseBackedConfig.getFromEmailAddress();

        // Look in the properties file next
        if (emailAddr == null
                && jndiBackedConfig.getDefaultFromEmailAddress() != null) {
            emailAddr = jndiBackedConfig.getDefaultFromEmailAddress();
        }

        // Finally, just throw an Exception
        if (emailAddr == null) {
            throw new RuntimeException(
                    "'From' email address has not been defined in either zanata.properties or Zanata setup");
        }
        return emailAddr;
    }

    public String getHomeContent() {
        return databaseBackedConfig.getHomeContent();
    }

    public String getHelpContent() {
        return databaseBackedConfig.getHelpContent();
    }

    public boolean isInternalAuth() {
        return this.loginModuleNames.containsKey(AuthenticationType.INTERNAL);
    }

    public boolean isOpenIdAuth() {
        return this.loginModuleNames.containsKey(AuthenticationType.OPENID);
    }

    public boolean isSingleOpenIdProvider() {
        return openIdProvider.isPresent();
    }

    public String getOpenIdProviderUrl() {
        return openIdProvider.orNull();
    }

    public boolean isKerberosAuth() {
        return this.loginModuleNames.containsKey(AuthenticationType.KERBEROS);
    }

    public boolean isJaasAuth() {
        return this.loginModuleNames.containsKey(AuthenticationType.JAAS);
    }

    public boolean isMultiAuth() {
        return loginModuleNames.size() > 1;
    }

    public String getLoginModuleName(AuthenticationType authType) {
        return this.loginModuleNames.get(authType);
    }

    public Set<String> getAdminUsers() {
        String configValue =
                Strings.nullToEmpty(jndiBackedConfig.getAdminUsersList());
        if (adminUsers == null) {
            adminUsers =
                    Sets.newHashSet(Splitter.on(",").omitEmptyStrings()
                            .trimResults().split(configValue));
        }
        return adminUsers;
    }

    public boolean isEmailLogAppenderEnabled() {
        String strVal = databaseBackedConfig.getShouldLogEvents();

        if (strVal == null) {
            return false;
        } else {
            return Boolean.parseBoolean(strVal);
        }
    }

    public List<String> getLogDestinationEmails() {
        String s = databaseBackedConfig.getLogEventsDestinationEmailAddress();
        if (s == null || s.trim().length() == 0) {
            return new ArrayList<String>();
        }
        String[] ss = s.trim().split("\\s*,\\s*");
        return new ArrayList<String>(Arrays.asList(ss));
    }

    public String getEmailLogLevel() {
        return databaseBackedConfig.getEmailLogLevel();
    }

    public String getPiwikUrl() {
        return databaseBackedConfig.getPiwikUrl();
    }

    public String getPiwikIdSite() {
        return databaseBackedConfig.getPiwikSiteId();
    }

    public String getTermsOfUseUrl() {
        return databaseBackedConfig.getTermsOfUseUrl();
    }

    public String getEmailServerHost() {
        String host = jndiBackedConfig.getSmtpHostName();

        // Default to localhost
        if (host == null) {
            host = "localhost";
        }
        return host;
    }

    public int getEmailServerPort() {
        String port = jndiBackedConfig.getSmtpPort();

        // Default to 25
        if (port == null) {
            port = "25";
        }
        return Integer.parseInt(port);
    }

    public String getEmailServerUsername() {
        return jndiBackedConfig.getSmtpUsername();
    }

    public String getEmailServerPassword() {
        return jndiBackedConfig.getSmtpPassword();
    }

    public boolean useEmailServerTls() {
        return jndiBackedConfig.getSmtpUsesTls() != null ? Boolean
                .parseBoolean(jndiBackedConfig.getSmtpUsesTls()) : false;
    }

    public boolean useEmailServerSsl() {
        return jndiBackedConfig.getStmpUsesSsl() != null ? Boolean
                .parseBoolean(jndiBackedConfig.getStmpUsesSsl()) : false;
    }

    public String getWebAssetsStyleUrl() {
        if (isEmpty(webAssetsStyleUrl)) {
            webAssetsStyleUrl = getWebAssetsUrl() + STYLESHEET_LOCAL_PATH;
        }
        return webAssetsStyleUrl;
    }

    public String getWebAssetsUrl() {
        if (isEmpty(webAssetsUrl)) {
            webAssetsUrl =
                    String.format("%s/%s", getBaseWebAssetsUrl(),
                            webAssetsVersion);
        }
        return webAssetsUrl;
    }

    private String getBaseWebAssetsUrl() {
        return Objects.firstNonNull(jndiBackedConfig.getWebAssetsUrlBase(),
                "//assets-zanata.rhcloud.com");
    }

    public int getMaxConcurrentRequestsPerApiKey() {
        return parseIntegerOrDefault(databaseBackedConfig.getMaxConcurrentRequestsPerApiKey(), 6);
    }

    public int getMaxActiveRequestsPerApiKey() {
        return parseIntegerOrDefault(databaseBackedConfig.getMaxActiveRequestsPerApiKey(), 2);
    }

    public int getMaxFilesPerUpload() {
        return parseIntegerOrDefault(databaseBackedConfig.getMaxFilesPerUpload(), defaultMaxFilesPerUpload);
    }

    private int parseIntegerOrDefault(String value, int defaultValue) {
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
