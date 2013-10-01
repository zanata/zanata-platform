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
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;

/**
 * Configuration store implementation that is backed by database tables.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("databaseBackedConfig")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class DatabaseBackedConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @In
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    private Map<String, String> configurationValues;

    /**
     * Resets the store by clearing out all values. This means that values will
     * need to be reloaded as they are requested.
     */
    @Create
    public void reset() {
        configurationValues = new HashMap<String, String>();
    }

    /**
     * Resets a single value of the configuration. This value will be reloaded
     * from the configuration store the next time it's requested.
     *
     * @param key
     *            Configuration key to reset.
     */
    public void reset(String key) {
        configurationValues.remove(key);
    }

    private String getConfigValue(String key) {
        if (!configurationValues.containsKey(key)) {
            HApplicationConfiguration configRecord =
                    applicationConfigurationDAO.findByKey(key);
            String storedVal = null;
            if (configRecord != null) {
                storedVal = configRecord.getValue();
            }
            configurationValues.put(key, storedVal);
        }
        return configurationValues.get(key);
    }

    private boolean containsKey(String key) {
        // Preemptively load the key
        return getConfigValue(key) != null;
    }

    /**
     * ========================================================================
     * ========================================== Specific property accessor
     * methods for configuration values
     * ==========================================
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

    public String getHelpContent() {
        return getConfigValue(HApplicationConfiguration.KEY_HELP_CONTENT);
    }

    // invalidate key will force reload of that value from db
    public void invalidateHomeContent() {
        configurationValues.remove(HApplicationConfiguration.KEY_HOME_CONTENT);
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

}
