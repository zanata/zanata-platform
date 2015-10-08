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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.zanata.servlet.HttpRequestAndSessionHolder;
import org.zanata.util.Synchronized;
import org.zanata.config.DatabaseBackedConfig;
import org.zanata.config.JaasConfig;
import org.zanata.config.JndiBackedConfig;
import org.zanata.events.ConfigurationChanged;
import org.zanata.events.LogoutEvent;
import org.zanata.events.PostAuthenticateEvent;
import org.zanata.i18n.Messages;
import org.zanata.log4j.ZanataHTMLLayout;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.security.AuthenticationType;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zanata.security.OpenIdLoginModule;

import static java.lang.Math.max;

@Named("applicationConfiguration")
@javax.enterprise.context.ApplicationScoped
/* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */
@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
@Slf4j
public class ApplicationConfiguration implements Serializable {
    private static final long serialVersionUID = -4970657841198107092L;

    private static final String EMAIL_APPENDER_NAME =
            "zanata.log.appender.email";

    @Getter
    private static final int defaultMaxFilesPerUpload = 100;

    @Getter
    private static final int defaultAnonymousSessionTimeoutMinutes = 30;

    @Inject
    private DatabaseBackedConfig databaseBackedConfig;
    @Inject
    private JndiBackedConfig jndiBackedConfig;
    @Inject
    private JaasConfig jaasConfig;
    @Inject
    private Messages msgs;

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

    private Optional<String> openIdProvider; // Cache the OpenId provider

    private String defaultServerPath;

    @PostConstruct
    public void load() {
        log.info("Reloading configuration");
        this.loadLoginModuleNames();
        this.validateConfiguration();
        this.applyLoggingConfiguration();
        this.loadJaasConfig();
    }

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
            return configuredValue;
        } else if (defaultServerPath != null) {
            return defaultServerPath;
        } else {
            createDefaultServerPath();
            return defaultServerPath;
        }
    }


    //@see comment at org.zanata.security.AuthenticationManager.onLoginCompleted()
    public void createDefaultServerPath() {
        java.util.Optional<HttpServletRequest> requestOpt =
                HttpRequestAndSessionHolder.getRequest();
        if (requestOpt.isPresent()) {
            HttpServletRequest request = requestOpt.get();
            defaultServerPath =
                    request.getScheme() + "://" + request.getServerName()
                            + ":" + request.getServerPort()
                            + request.getContextPath();
        }
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

    public String getHelpUrl() {
        return databaseBackedConfig.getHelpUrl();
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

    public String copyrightNotice() {
        return msgs.format("jsf.CopyrightNotice",
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
    }

    public void setAuthenticatedSessionTimeout(
            @Observes PostAuthenticateEvent payload) {
        java.util.Optional<HttpSession> sessionOpt =
                HttpRequestAndSessionHolder.getHttpSession();
        if (sessionOpt.isPresent()) {
            int timeoutInSecs = max(authenticatedSessionTimeoutMinutes * 60,
                    defaultAnonymousSessionTimeoutMinutes * 60);
            sessionOpt.get()
                .setMaxInactiveInterval(timeoutInSecs);
        }
    }

    public void setUnauthenticatedSessionTimeout(@Observes LogoutEvent payload) {
        java.util.Optional<HttpSession> sessionOpt =
                HttpRequestAndSessionHolder.getHttpSession();
        if (sessionOpt.isPresent()) {
            sessionOpt.get()
                .setMaxInactiveInterval(
                        defaultAnonymousSessionTimeoutMinutes * 60);
        }
    }
}
