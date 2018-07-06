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
package org.zanata.client.commands;

import java.io.File;
import java.net.URL;

import org.kohsuke.args4j.Option;

/**
 * Base options for commands which support configuration by the user's
 * zanata.ini
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public abstract class ConfigurableOptionsImpl extends BasicOptionsImpl
        implements ConfigurableOptions {
    /**
     * Client configuration file.
     */
    private File userConfig = new File(System.getProperty("user.home"),
            ".config/zanata.ini");

    /**
     * Base URL for the server. Defaults to the value in zanata.xml.
     */
    private URL url;

    /**
     * Username for accessing the REST API. Defaults to the value in zanata.ini.
     */
    private String username;

    /**
     * API key for accessing the REST API. Defaults to the value in zanata.ini.
     */
    private String key;

    /**
     * Enable HTTP message logging.
     */
    private boolean logHttp;
    private boolean disableSSLCert;

    public ConfigurableOptionsImpl() {
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    @Option(name = "--key", metaVar = "KEY",
            usage = "API key (from user's profile page)")
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    @Option(name = "--url", metaVar = "URL",
            usage = "Base URL, eg http://zanata.example.com/zanata/")
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    @Option(name = "--user-config", metaVar = "FILE",
            usage = "User configuration, eg /home/user/.config/zanata.ini")
    public void setUserConfig(File userConfig) {
        this.userConfig = userConfig;
    }

    @Override
    public File getUserConfig() {
        return userConfig;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @Option(name = "--username", metaVar = "USER", usage = "Username")
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean getLogHttp() {
        return logHttp;
    }

    @Override
    @Option(name = "--log-http",
            usage = "Enable HTTP message logging. WARNING: Uses a lot of memory if sending/receiving large amounts of data (eg large documents).")
    public void setLogHttp(boolean logHttp) {
        this.logHttp = logHttp;
    }

    @Override
    public boolean isDisableSSLCert() {
        return disableSSLCert;
    }

    @Override
    @Option(
            name = "--disable-ssl-cert",
            usage = "Whether verification of SSL certificates should be disabled")
    public
            void setDisableSSLCert(boolean disableSSLCert) {
        this.disableSSLCert = disableSSLCert;
    }

    @Override
    public boolean isAuthRequired() {
        // auth should be required for most commands
        return true;
    }
}
