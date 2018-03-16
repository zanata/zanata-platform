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

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.spi.LoginModule;

import javax.inject.Named;
import java.io.Serializable;

/**
 * Store for JAAS configuration.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("jaasConfig")

@javax.enterprise.context.ApplicationScoped
public class JaasConfig implements Serializable {

    private static final long serialVersionUID = -8707906261411142759L;

    /**
     * Retrieves all App configuration entries under a given name. In Jboss's
     * standalone*.xml, these are all the {@code <login-module></login-module>}
     * entries under @{code <security-domain>}
     *
     * @param loginModuleName
     *            The login module name as configured. In Jboss' standalone*.xml
     *            this the name attribute at
     *            {@code <security-domain name="NAME">}
     * @return A collection of configuration entries. May be null if there are
     *         no configuration entries under that name.
     */
    public AppConfigurationEntry[] getAppConfigurationEntries(
            String loginModuleName) {
        return Configuration.getConfiguration().getAppConfigurationEntry(
                loginModuleName);
    }

    /**
     * Retrieves a single configuration entry under a given name and using a
     * specific class. In JBoss' standalone*.xml this type is specified under
     * {@code <login-module code="TYPE"></login-module>}. Since there may be
     * more than one configuration entry using the same code, this method
     * returns the first one found.
     *
     * @param loginModuleName
     *            The login module name as configured. In Jboss' standalone*.xml
     *            this the name attribute at
     *            {@code <security-domain name="NAME">}
     * @param loginModuleType
     *            The Login module type used.
     * @return the first found configuration entry under the given name, and
     *         using the given login module class. Null if no such configuration
     *         is found.
     */
    public AppConfigurationEntry
            getAppConfigurationEntry(String loginModuleName,
                    Class<? extends LoginModule> loginModuleType) {
        AppConfigurationEntry[] entries =
                getAppConfigurationEntries(loginModuleName);
        if(entries != null) {
            for (AppConfigurationEntry e : entries) {
                // This is poorly named, the getLoginModuleName method returns
                // the class
                if (e.getLoginModuleName().equals(loginModuleType.getName())) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a configuration property. In Jboss's standalone*.xml, these are
     * configured at {@code
     * <login-module ...>
     *     <module-option name="KEY" value="VALUE"/>
     *     ...
     * </login-module>
     * }
     *
     * @param loginModuleName
     *            The login module name as configured. In Jboss' standalone*.xml
     *            this the name attribute at
     *            {@code <security-domain name="NAME">}
     * @param loginModuleType
     *            The Login module type used.
     * @param key
     *            The option key.
     * @return The configuration value for the property ofr null if the property
     *         cannot be found.
     */
    public String getAppConfigurationProperty(String loginModuleName,
            Class<? extends LoginModule> loginModuleType, String key) {
        AppConfigurationEntry configEntry =
                getAppConfigurationEntry(loginModuleName, loginModuleType);
        if (configEntry == null) {
            return null;
        } else if (configEntry.getOptions().containsKey(key)) {
            return (String) configEntry.getOptions().get(key);
        } else {
            return null;
        }
    }
}
