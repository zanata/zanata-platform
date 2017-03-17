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
package org.zanata.config;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.ObjectUtils;
import org.zanata.util.Synchronized;
import org.zanata.ServerConstants;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;

/**
 * Configuration store implementation that is backed by database tables.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("databaseBackedConfig")
@javax.enterprise.context.ApplicationScoped

@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
public class DatabaseBackedConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    private @Nullable String getConfigValue(String key) {
        HApplicationConfiguration configRecord =
                applicationConfigurationDAO.findByKey(key);
        return configRecord != null ? configRecord.getValue() : null;
    }

    /**
     * ========================================================================
     * ===== Specific property accessor methods for configuration values ======
     * ========================================================================
     */
    public String getAdminEmailAddress() {
        return getConfigValue(HApplicationConfiguration.KEY_ADMIN_EMAIL);
    }

    public String getDomain() {
        return getConfigValue(HApplicationConfiguration.KEY_DOMAIN);
    }

    public String getFromEmailAddress() {
        return getConfigValue(HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS);
    }

    public String getShouldLogEvents() {
        return getConfigValue(HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS);
    }

    public String getEmailLogLevel() {
        return getConfigValue(HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL);
    }

    public String getHelpUrl() {
        return getConfigValue(HApplicationConfiguration.KEY_HELP_URL);
    }

    public String getHomeContent() {
        return getConfigValue(HApplicationConfiguration.KEY_HOME_CONTENT);
    }

    public String getServerHost() {
        return getConfigValue(HApplicationConfiguration.KEY_HOST);
    }

    public String getLogEventsDestinationEmailAddress() {
        return getConfigValue(HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL);
    }

    public String getRegistrationUrl() {
        return getConfigValue(HApplicationConfiguration.KEY_REGISTER);
    }

    public String getPiwikUrl() {
        return getConfigValue(HApplicationConfiguration.KEY_PIWIK_URL);
    }

    public String getPiwikSiteId() {
        return getConfigValue(HApplicationConfiguration.KEY_PIWIK_IDSITE);
    }

    public String getTermsOfUseUrl() {
        return getConfigValue(HApplicationConfiguration.KEY_TERMS_CONDITIONS_URL);
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        return getConfigValue(HApplicationConfiguration.KEY_MAX_CONCURRENT_REQ_PER_API_KEY);
    }

    public String getMaxActiveRequestsPerApiKey() {
        return getConfigValue(HApplicationConfiguration.KEY_MAX_ACTIVE_REQ_PER_API_KEY);
    }

    public String getMaxFilesPerUpload() {
        return getConfigValue(HApplicationConfiguration.KEY_MAX_FILES_PER_UPLOAD);
    }

    public boolean isDisplayUserEmail() {
        return Boolean.valueOf(getConfigValue(HApplicationConfiguration.KEY_DISPLAY_USER_EMAIL));
    }

    public String getPermittedEmailDomains() {
        return getConfigValue(HApplicationConfiguration.KEY_PERMITTED_USER_EMAIL_DOMAIN);
    }

    public boolean isAnonymousUserAllowed() {
        String dbValue = getConfigValue(
                HApplicationConfiguration.KEY_ALLOW_ANONYMOUS_USER);
        // If there is no value in the database, by default we allow the access for existing instance.
        // For new instances, we will set the value in database on first boot
        // see EssentialDataCreator
        return dbValue == null || Boolean.valueOf(dbValue);
    }

    public boolean isAutoAcceptTranslators() {
        return ObjectUtils.firstNonNull(Boolean.valueOf(
                getConfigValue(HApplicationConfiguration.KEY_AUTO_ACCEPT_TRANSLATOR)),
                false);
    }
}
