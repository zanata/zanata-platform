package org.zanata.client.commands;

import java.io.File;
import java.net.URL;


import org.kohsuke.args4j.Option;

/**
 * Base options for commands which supports configuration by the user's
 * zanata.ini
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public interface ConfigurableOptions extends BasicOptions {

    /**
     * API key for accessing the REST API. Defaults to the value in zanata.ini.
     */
    String getKey();

    void setKey(String key);

    /**
     * Base URL for the server. Defaults to the value in zanata.xml.
     */
    URL getUrl();

    void setUrl(URL url);

    /**
     * Client configuration file.
     */
    File getUserConfig();

    void setUserConfig(File userConfig);

    /**
     * Username for accessing the REST API. Defaults to the value in zanata.ini.
     */
    String getUsername();

    void setUsername(String username);

    /**
     * Enable HTTP message logging.
     */
    boolean getLogHttp();

    void setLogHttp(boolean traceLogging);

    /**
     * Disable SSL certificate verification when connecting to Zanata host by
     * https.
     */
    boolean isDisableSSLCert();

    void setDisableSSLCert(boolean disableSSLCert);

    /**
     * Use to disable check for presence of username and API key before running command.
     *
     * @return true if this command should fail when username or API key is absent.
     */
    boolean isAuthRequired();
}
