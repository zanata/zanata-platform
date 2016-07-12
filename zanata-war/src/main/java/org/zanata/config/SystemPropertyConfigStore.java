/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Property Store that delegates to system properties.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("systemPropertyConfigStore")
@javax.enterprise.context.Dependent
public class SystemPropertyConfigStore implements ConfigStore {

    private static final String KEY_AUTH_POLICY =
            "zanata.security.authpolicy.";
    private static final String KEY_ADMIN_USERS =
            "zanata.security.adminusers";
    private static final String KEY_DEFAULT_FROM_ADDRESS =
            "zanata.email.defaultfromaddress";
    public static final String KEY_DOCUMENT_FILE_STORE =
            "zanata.file.directory";

    /**
     * Server-wide switch to enable/disable OAuth support
     */
    private static final String KEY_SUPPORT_OAUTH = "zanata.support.oauth";
    private static final Logger log =
            LoggerFactory.getLogger(SystemPropertyConfigStore.class);

    @Override
    public String get(String propertyName) {
        return System.getProperty(propertyName);
    }

    @Override
    public int get(String propertyName, int defaultValue) {
        return parseAs(propertyName, get(propertyName), defaultValue, Integer::valueOf);
    }

    @Override
    public long getLong(String propertyName, long defaultValue) {
        return parseAs(propertyName, get(propertyName), defaultValue, Long::valueOf);
    }

    private static <N extends Number> N parseAs(String propertyName,
            String value, N defaultValue, Function<String, N> convertFn) {
        try {
            return convertFn.apply(value);
        } catch (NumberFormatException e) {
            log.warn(
                    "Invalid system property value [{}] is given to {}. Fall back to default {}",
                    value, propertyName, defaultValue);
            return defaultValue;
        }
    }

    /**
     * ========================================================================
     * Specific property accessormethods for configuration values
     * ========================================================================
     */
    public Set<String> getEnabledAuthenticationPolicies() {
        Stream<String> authPolicyKeys =
                System.getProperties().stringPropertyNames().stream()
                        .filter(propName -> propName
                                .startsWith(KEY_AUTH_POLICY));
        return authPolicyKeys.map(k -> k.replace(KEY_AUTH_POLICY, ""))
                .collect(toSet());
    }

    public String getAuthPolicyName(String authType) {
        return System.getProperty(KEY_AUTH_POLICY + authType);
    }

    public String getAdminUsersList() {
        return System.getProperty(KEY_ADMIN_USERS);
    }

    public String getDefaultFromEmailAddress() {
        return System.getProperty(KEY_DEFAULT_FROM_ADDRESS);
    }

    public String getDocumentFileStorageLocation() {
        return System.getProperty(KEY_DOCUMENT_FILE_STORE);
    }

    /**
     *
     * @return whether this server instance supports OAuth
     */
    public boolean isOAuthEnabled() {
        return Boolean
                .parseBoolean(System.getProperty(KEY_SUPPORT_OAUTH, "false"));
    }
}
