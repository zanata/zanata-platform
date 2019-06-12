package org.zanata.maven;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConfigurableOptions;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.CommandHook;

import com.pyx4j.log4j.MavenLogAppender;

/**
 * Base class for mojos which support configuration by the user's zanata.ini
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public abstract class ConfigurableMojo<O extends ConfigurableOptions> extends
        AbstractMojo implements ConfigurableOptions {
    private static final String BUG_URL = "https://zanata.atlassian.net/";

    /**
     * Client configuration file for Zanata.
     */
    @Parameter(property = "zanata.userConfig", defaultValue = "${user.home}/.config/zanata.ini")
    private File userConfig;

    /**
     * Base URL for the server. Defaults to the value in zanata.xml (if
     * present).
     */
    @Parameter(property = "zanata.url")
    private URL url;

    /**
     * Username for accessing the REST API. Defaults to the value in zanata.ini.
     */
    @Parameter(property = "zanata.username")
    private String username;

    /**
     * API key for accessing the REST API. Defaults to the value in zanata.ini.
     */
    @Parameter(property = "zanata.key")
    private String key;

    private List<CommandHook> commandHooks = new ArrayList<CommandHook>();

    /**
     * Interactive mode is enabled by default, but can be disabled using Maven's
     * -B/--batch-mode option.
     */
    @Parameter(defaultValue = "${settings.interactiveMode}")
    private boolean interactiveMode = true;

    private boolean interactiveModeSet;

    /**
     * Enable HTTP message logging.
     */
    @Parameter(property = "zanata.logHttp", defaultValue = "false")
    private boolean logHttp = false;

    /**
     * Disable SSL certificate verification when connecting to Zanata host by
     * https.
     */
    @Parameter(property = "zanata.disableSSLCert", defaultValue = "false")
    private boolean disableSSLCert = false;

    public ConfigurableMojo() {
        // Hack for Java 11 class loading via modules - load all package-info classes here
        try {
            Class.forName("org.zanata.client.config.package-info");
            Class.forName("org.zanata.rest.dto.package-info");
            Class.forName("org.zanata.rest.dto.resource.package-info");
        } catch (ClassNotFoundException e) {
            // ignore class loading failure here
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // @formatter:off
      /*
       * Configure the MavenLogAppender to use this Mojo's Maven logger. NB
       * maven-plugin-log4j.jar includes a log4j.xml to activate the
       * MavenLogAppender. See
       * http://pyx4j.com/snapshot/pyx4j/pyx4j-maven-plugins/maven-plugin-log4j/index.html
       * In case it needs to be overridden, it looks like this:

<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE log4j:configuration PUBLIC "-//log4j//DTD//EN" "http://logging.apache.org/log4j/docs/api/org/apache/log4j/xml/log4j.dtd">
<log4j:configuration>

    <appender name="MavenLogAppender" class="com.pyx4j.log4j.MavenLogAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%m" />
        </layout>
    </appender>

    <root>
        <level value="debug" />
        <appender-ref ref="MavenLogAppender" />
    </root>

</log4j:configuration>

       */
      // @formatter:on

        MavenLogAppender.startPluginLog(this);
        try {
            getLog().info("Please report Zanata bugs here: " + BUG_URL);
            OptionsUtil.applyConfigFiles(this);

            runCommand();
        } catch (Exception e) {
            throw new MojoExecutionException("Zanata mojo exception", e);
        } finally {
            MavenLogAppender.endPluginLog(this);
        }
    }

    protected void runCommand() throws Exception {
        ZanataCommand command = initCommand();
        String name = command.getName();
        getLog().info("Zanata command: " + name);
        if (command.isDeprecated()) {
            String msg = command.getDeprecationMessage();
            if (msg != null) {
                getLog().warn(
                        "Command \"" + name + "\" has been deprecated: " + msg);
            } else {
                getLog().warn("Command \"" + name + "\" has been deprecated");
            }
        }
        command.runWithActions();
    }

    public abstract ConfigurableCommand<O> initCommand();

    // These options don't apply to Mojos (since they duplicate Maven's built-in
    // mechanisms)

    @Override
    public boolean getDebug() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDebug(boolean debug) {
        getLog().info("ignoring setDebug: use mvn -X to control debug logging");
    }

    @Override
    public boolean getErrors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setErrors(boolean errors) {
        getLog().info(
                "ignoring setErrors: use mvn -e to control exception logging");
    }

    @Override
    public boolean getHelp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHelp(boolean help) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getQuiet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setQuiet(boolean quiet) {
        getLog().info("ignoring setQuiet: use mvn -q to set quiet logging mode");
    }

    // maven controls logging, so there's no point in changing these values
    @Override
    public boolean isDebugSet() {
        return getLog().isDebugEnabled();
    }

    @Override
    public boolean isErrorsSet() {
        return getLog().isErrorEnabled();
    }

    @Override
    public boolean isQuietSet() {
        return !getLog().isInfoEnabled();
    }

    @Override
    public boolean isInteractiveModeSet() {
        return interactiveModeSet;
    }

    @Override
    public boolean isInteractiveMode() {
        return interactiveMode;
    }

    @Override
    public void setInteractiveMode(boolean interactiveMode) {
        interactiveModeSet = true;
        this.interactiveMode = interactiveMode;
    }

    // these options only apply to the command line:
    @Override
    public String getCommandDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public void setUserConfig(File userConfig) {
        this.userConfig = userConfig;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public File getUserConfig() {
        return userConfig;
    }

    @Override
    public void setCommandHooks(List<CommandHook> commandHooks) {
        this.commandHooks = commandHooks;
    }

    @Override
    public List<CommandHook> getCommandHooks() {
        return commandHooks;
    }

    @Override
    public boolean getLogHttp() {
        return logHttp;
    }

    @Override
    public void setLogHttp(boolean logHttp) {
        this.logHttp = logHttp;
    }

    @Override
    public boolean isDisableSSLCert() {
        return disableSSLCert;
    }

    @Override
    public void setDisableSSLCert(boolean disableSSLCert) {
        this.disableSSLCert = disableSSLCert;
    }

    @Override
    public boolean isAuthRequired() {
        // auth should be required for most commands
        return true;
    }
}
