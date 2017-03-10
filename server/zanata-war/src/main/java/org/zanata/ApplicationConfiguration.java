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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import com.google.common.base.Optional;
import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.log4j.Level;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import org.zanata.config.AllowAnonymousAccess;
import org.zanata.config.AllowPublicRegistration;
import org.zanata.config.OAuthTokenExpiryInSeconds;
import org.zanata.config.SupportOAuth;
import org.zanata.config.SystemPropertyConfigStore;
import org.zanata.model.validator.AcceptedEmailDomainsForNewAccount;
import org.zanata.servlet.HttpRequestAndSessionHolder;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.util.DefaultLocale;
import org.zanata.util.Synchronized;
import org.zanata.config.DatabaseBackedConfig;
import org.zanata.config.JaasConfig;
import org.zanata.events.LogoutEvent;
import org.zanata.events.PostAuthenticateEvent;
import org.zanata.i18n.Messages;
import org.zanata.log4j.ZanataHTMLLayout;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.security.AuthenticationType;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zanata.security.OpenIdLoginModule;
import static java.lang.Math.max;

@Named("applicationConfiguration")
@ApplicationScoped
@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
public class ApplicationConfiguration implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ApplicationConfiguration.class);

    private static final long serialVersionUID = -4970657841198107092L;
    private static final String EMAIL_APPENDER_NAME =
            "zanata.log.appender.email";
    private static final int defaultMaxFilesPerUpload = 100;
    private static final int defaultAnonymousSessionTimeoutMinutes = 30;
    public static final String ACCESS_TOKEN_EXPIRES_IN_SECONDS =
            "accessTokenExpiresInSeconds";
    @Inject
    private DatabaseBackedConfig databaseBackedConfig;
    @Inject
    private JaasConfig jaasConfig;
    @Inject
    @DefaultLocale
    private Messages msgs;
    @Inject
    private SystemPropertyConfigStore sysPropConfigStore;
    private static final ZanataSMTPAppender smtpAppenderInstance =
            new ZanataSMTPAppender();
    private int authenticatedSessionTimeoutMinutes;
    private String version;
    private String buildTimestamp;
    private String scmDescribe;
    private boolean copyTransEnabled = true;
    @Inject
    @DeltaSpike
    private HttpSession session;

    /**
     * To be used with single sign-up module with openId. Default is false
     *
     * When set to true:
     *
     * This is to enforce username to match with username returned from openId
     * server when new user register.
     *
     * Usage: server administrator can enable this in system property
     * zanata.enforce.matchingusernames. In standalone.xml:
     *
     * <pre>
     * {@code <property name="zanata.enforce.matchingusernames" value="true" />}
     * </pre>
     */
    private boolean enforceMatchingUsernames;
    private Map<AuthenticationType, String> loginModuleNames =
            Maps.newHashMap();
    private Set<String> adminUsers;
    private Optional<String> openIdProvider; // Cache the OpenId provider
    private long tokenExpiresInSeconds;

    @PostConstruct
    public void load() {
        log.info("Reloading configuration");
        this.loadLoginModuleNames();
        this.loadJaasConfig();
        authenticatedSessionTimeoutMinutes = sysPropConfigStore
                .get("authenticatedSessionTimeoutMinutes", 180);
        enforceMatchingUsernames = Boolean.parseBoolean(
                sysPropConfigStore.get("zanata.enforce.matchingusernames"));
        tokenExpiresInSeconds = sysPropConfigStore
                .getLong(ACCESS_TOKEN_EXPIRES_IN_SECONDS, 3600);
    }

    /**
     * Loads the accepted login module (JAAS) names from the underlying
     * configuration
     */
    private void loadLoginModuleNames() {
        loginModuleNames = sysPropConfigStore.getLoginModuleNames();
    }

    /**
     * Apply logging configuration.
     */
    public void applyLoggingConfiguration() {
        // TODO is this still working with jboss logging?
        final org.apache.log4j.Logger rootLogger =
                org.apache.log4j.Logger.getRootLogger();
        if (isEmailLogAppenderEnabled()) {
            // NB: This appender uses JBoss's email configuration (no need for
            // host or port)
            smtpAppenderInstance.setName(EMAIL_APPENDER_NAME);
            smtpAppenderInstance.setFrom(getFromEmailAddr());
            smtpAppenderInstance.setTo(
                    databaseBackedConfig.getLogEventsDestinationEmailAddress());
            // TODO use hostname, not URL
            smtpAppenderInstance.setSubject(
                    "%p log message from Zanata at " + this.getServerPath());
            String buildInfo = getVersion() + ", " + getBuildTimestamp() + "["
                + getScmDescribe() + "]";
            smtpAppenderInstance.setLayout(new ZanataHTMLLayout(buildInfo));
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
            openIdProvider = Optional
                    .fromNullable(jaasConfig.getAppConfigurationProperty(
                            loginModuleNames.get(AuthenticationType.OPENID),
                            OpenIdLoginModule.class,
                            OpenIdLoginModule.OPEN_ID_PROVIDER_KEY));
        } else {
            openIdProvider = Optional.absent();
        }
    }

    public String getRegisterPath() {
        return databaseBackedConfig.getRegistrationUrl();
    }

    @Produces
    @ServerPath
    public String getServerPath() {
        String configuredValue = databaseBackedConfig.getServerHost();
        if (configuredValue != null) {
            return configuredValue;
        } else {
            return HttpRequestAndSessionHolder.getDefaultServerPath();
        }
    }

    public String getDocumentFileStorageLocation() {
        return sysPropConfigStore.getDocumentFileStorageLocation();
    }

    public String getHibernateSearchIndexBase() {
        return sysPropConfigStore.getHibernateSearchIndexBase();
    }

    public String getDomainName() {
        return databaseBackedConfig.getDomain();
    }

    public List<String> getAdminEmail() {
        String s = databaseBackedConfig.getAdminEmailAddress();
        if (s == null || s.trim().length() == 0) {
            return new ArrayList<>();
        }
        String[] ss = s.trim().split("\\s*,\\s*");
        return new ArrayList<>(Arrays.asList(ss));
    }

    public String getFromEmailAddr() {
        String emailAddr = null;
        // Look in the database first
        emailAddr = databaseBackedConfig.getFromEmailAddress();
        // Look in the properties file next
        if (emailAddr == null
                && sysPropConfigStore.getDefaultFromEmailAddress() != null) {
            emailAddr = sysPropConfigStore.getDefaultFromEmailAddress();
        }
        // Finally, just throw an Exception
        if (emailAddr == null) {
            throw new RuntimeException(
                    "\'From\' email address has not been defined in either zanata.properties or Zanata setup");
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

    @Produces
    @Dependent
    protected AuthenticationType authenticationType() {
        if (isInternalAuth()) {
            return AuthenticationType.INTERNAL;
        } else if (isJaasAuth()) {
            return AuthenticationType.JAAS;
        } else if (isKerberosAuth()) {
            return AuthenticationType.KERBEROS;
        }
        throw new RuntimeException(
                "only supports internal, jaas, or kerberos authentication");
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
                Strings.nullToEmpty(sysPropConfigStore.getAdminUsersList());
        if (adminUsers == null) {
            adminUsers = Sets.newHashSet(Splitter.on(",").omitEmptyStrings()
                    .trimResults().split(configValue));
        }
        return adminUsers;
    }

    public boolean isEmailLogAppenderEnabled() {
        String strVal = databaseBackedConfig.getShouldLogEvents();
        return strVal != null && Boolean.parseBoolean(strVal);
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
        return parseIntegerOrDefault(
                databaseBackedConfig.getMaxConcurrentRequestsPerApiKey(), 6);
    }

    public int getMaxActiveRequestsPerApiKey() {
        return parseIntegerOrDefault(
                databaseBackedConfig.getMaxActiveRequestsPerApiKey(), 2);
    }

    public int getMaxFilesPerUpload() {
        return parseIntegerOrDefault(
                databaseBackedConfig.getMaxFilesPerUpload(),
                defaultMaxFilesPerUpload);
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
        if (session != null) {
            int timeoutInSecs = max(authenticatedSessionTimeoutMinutes * 60,
                    defaultAnonymousSessionTimeoutMinutes * 60);
            session.setMaxInactiveInterval(timeoutInSecs);
        }
    }

    public void
            setUnauthenticatedSessionTimeout(@Observes LogoutEvent payload) {
        // if we use ServiceLocator and get deltaspike session, upon invoking
        // setMaxInactiveInterval method, it will throw Request Scope not active
        // exception
        java.util.Optional<HttpSession> httpSession =
                HttpRequestAndSessionHolder.getHttpSession(false);
        if (httpSession.isPresent()) {
            httpSession.get().setMaxInactiveInterval(
                    defaultAnonymousSessionTimeoutMinutes * 60);
        }
    }

    public boolean isDisplayUserEmail() {
        return databaseBackedConfig.isDisplayUserEmail();
    }

    @Produces
    @OAuthTokenExpiryInSeconds
    protected long getTokenExpiresInSeconds() {
        return tokenExpiresInSeconds;
    }

    @Produces
    @SupportOAuth
    protected boolean isOAuthSupported() {
        return sysPropConfigStore.isOAuthEnabled();
    }

    @Produces
    @AcceptedEmailDomainsForNewAccount
    protected Set<String> permittedEmailDomains() {
        String domains = databaseBackedConfig.getPermittedEmailDomains();
        if (Strings.isNullOrEmpty(domains)) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(Splitter.on(",").trimResults()
                .omitEmptyStrings().split(domains));
    }

    @Produces
    @AllowAnonymousAccess
    protected boolean isAnonymousUserAllowed() {
        return databaseBackedConfig.isAnonymousUserAllowed();
    }

    @Named("allowPublicRegistration")
    @Produces
    @AllowPublicRegistration
    protected boolean isPublicRegistrationAllowed(
            @AllowAnonymousAccess boolean allowAnonymous) {
        return allowAnonymous;
    }

    public static int getDefaultMaxFilesPerUpload() {
        return ApplicationConfiguration.defaultMaxFilesPerUpload;
    }

    public static int getDefaultAnonymousSessionTimeoutMinutes() {
        return ApplicationConfiguration.defaultAnonymousSessionTimeoutMinutes;
    }

    public int getAuthenticatedSessionTimeoutMinutes() {
        return this.authenticatedSessionTimeoutMinutes;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getBuildTimestamp() {
        return this.buildTimestamp;
    }

    public void setBuildTimestamp(final String buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public String getScmDescribe() {
        return this.scmDescribe;
    }

    public void setScmDescribe(final String scmDescribe) {
        this.scmDescribe = scmDescribe;
    }

    public boolean isCopyTransEnabled() {
        return this.copyTransEnabled;
    }

    /**
     * To be used with single sign-up module with openId. Default is false
     *
     * When set to true:
     *
     * This is to enforce username to match with username returned from openId
     * server when new user register.
     *
     * Usage: server administrator can enable this in system property
     * zanata.enforce.matchingusernames. In standalone.xml:
     *
     * <pre>
     * {@code <property name="zanata.enforce.matchingusernames" value="true" />}
     * </pre>
     */
    public boolean isEnforceMatchingUsernames() {
        return this.enforceMatchingUsernames;
    }

    public boolean isAutoAcceptRequests() {
        return databaseBackedConfig.isAutoAcceptTranslators();
    }
}
