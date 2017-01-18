/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.config;

import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.AuthenticationType;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This servlet context listener will check the correctness of required system
 * properties.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SystemConfigStartupCheck implements ServletContextListener {
    private static final Logger log =
            LoggerFactory.getLogger(SystemConfigStartupCheck.class);
    @Inject
    private SystemPropertyConfigStore sysPropConfigStore;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ObsoleteJNDIChecker.ensureNoObsoleteJNDIEntries();

        boolean errorFound = sysPropConfigStore.hasMissingRequiredSystemProperties();

        errorFound |= !validateAuthenticationConfig();

        // obsolete properties
        String obsoleteProp = "ehcache.disk.store.dir";
        String ehcacheDir = sysPropConfigStore.get(obsoleteProp);
        if (!isNullOrEmpty(ehcacheDir)) {
            log.error(
                    "{} is no longer needed. Please remove it from your system properties and remove any obsolete cache files from the directory {}.", obsoleteProp, ehcacheDir);
            errorFound = true;
        }

        if (errorFound) {
            throw new RuntimeException(
                    "System properties for Zanata are not configured properly. Check the log for details.");
        }
    }

    /**
     * Validates that there are no invalid values set on the zanata
     * configuration.
     *
     * @return true if auth policy value looks ok otherwise false
     */
    private boolean validateAuthenticationConfig() {
        Map<AuthenticationType, String> loginModuleNames =
                sysPropConfigStore.getLoginModuleNames();
        // Validate that only internal / openid authentication is enabled at
        // once
        if (loginModuleNames.size() > 2) {
            log.error(
                    "Multiple invalid authentication types present in Zanata configuration.");
            return false;
        } else if (loginModuleNames.size() == 2) {
            // Internal and Open id are the only allowed combined authentication
            // types
            if (!(loginModuleNames.containsKey(AuthenticationType.OPENID) &&
                    loginModuleNames
                            .containsKey(AuthenticationType.INTERNAL))) {
                log.error(
                        "Multiple invalid authentication types present in Zanata configuration.");
                return false;
            }
        } else if (loginModuleNames.size() < 1) {
            log.error(
                    "At least one authentication type must be configured in Zanata configuration.");
            return false;
        }
        return true;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
