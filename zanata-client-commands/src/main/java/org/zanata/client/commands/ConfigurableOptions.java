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
    public String getKey();

    public void setKey(String key);

    /**
     * Base URL for the server. Defaults to the value in zanata.xml.
     */
    public URL getUrl();

    public void setUrl(URL url);

    /**
     * Client configuration file.
     */
    public File getUserConfig();

    public void setUserConfig(File userConfig);

    /**
     * Username for accessing the REST API. Defaults to the value in zanata.ini.
     */
    public String getUsername();

    public void setUsername(String username);

    /**
     * Enable HTTP message logging.
     */
    public boolean getLogHttp();

    public void setLogHttp(boolean traceLogging);

    /**
     * Disable SSL certificate verification when connecting to Zanata host by
     * https.
     */
    boolean isDisableSSLCert();

    public
            void setDisableSSLCert(boolean disableSSLCert);
}
