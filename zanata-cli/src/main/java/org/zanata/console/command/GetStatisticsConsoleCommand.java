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
package org.zanata.console.command;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.stats.GetStatisticsCommand;
import org.zanata.client.commands.stats.GetStatisticsOptions;
import org.zanata.client.config.LocaleList;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@CommandDefinition(name = "stats", description = "Gets Statistics")
public class GetStatisticsConsoleCommand implements Command,
        GetStatisticsOptions {
    @Option
    private boolean includeDetails;
    @Option
    private boolean includeWordLevelStats;
    @Option
    private String format;
    @Option
    private String documentId;
    @Option
    private String project;
    @Option
    private File projectConfig;
    @Option
    private String projectVersion;
    @Option
    private String projectType;
    @Option
    private LocaleList localeMapList;
    @Option
    private String key;
    @Option(/* converter = URLConverter.class */)
    private URL url;
    @Option
    private File userConfig;
    @Option
    private String username;
    @Option
    private boolean logHttp;
    @Option
    private boolean disableSSLCert;
    @Option
    private Boolean debug = null;
    @Option(name = "errors", shortName = 'X')
    private Boolean errors = null;
    @Option
    private boolean help;
    @Option
    private Boolean quiet = null;
    @Option
    private boolean interactiveMode;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation)
            throws IOException {
        try {
            new GetStatisticsCommand(this).run();
        } catch (Exception e) {
            e.printStackTrace(); // To change body of catch statement use File |
                                 // Settings | File Templates.
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

    @Override
    public boolean getIncludeDetails() {
        return includeDetails;
    }

    @Override
    public void setIncludeDetails(boolean includeDetails) {
        this.includeDetails = includeDetails;
    }

    @Override
    public boolean getIncludeWordLevelStats() {
        return includeWordLevelStats;
    }

    @Override
    public void setIncludeWordLevelStats(boolean includeWordLevelStats) {
        this.includeWordLevelStats = includeWordLevelStats;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public String getProj() {
        return project;
    }

    @Override
    public void setProj(String projectSlug) {
        this.project = projectSlug;
    }

    @Override
    public void setProjectConfig(File projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    public String getProjectVersion() {
        return projectVersion;
    }

    @Override
    public void setProjectVersion(String versionSlug) {
        this.projectVersion = versionSlug;
    }

    @Override
    public String getProjectType() {
        return projectType;
    }

    @Override
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    @Override
    public File getProjectConfig() {
        return projectConfig;
    }

    @Override
    public LocaleList getLocaleMapList() {
        return localeMapList;
    }

    @Override
    public void setLocaleMapList(LocaleList locales) {
        this.localeMapList = locales;
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
    public File getUserConfig() {
        return userConfig;
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
    public boolean getLogHttp() {
        return logHttp;
    }

    @Override
    public void setLogHttp(boolean traceLogging) {
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
    public ZanataCommand initCommand() {
        return null; // To change body of implemented methods use File |
                     // Settings | File Templates.
    }

    @Override
    public boolean getDebug() {
        return debug;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean isDebugSet() {
        return debug != null;
    }

    @Override
    public boolean getErrors() {
        return errors;
    }

    @Override
    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    @Override
    public boolean isErrorsSet() {
        return errors != null;
    }

    @Override
    public boolean getHelp() {
        return help;
    }

    @Override
    public void setHelp(boolean help) {
        this.help = help;
    }

    @Override
    public boolean getQuiet() {
        return quiet;
    }

    @Override
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public boolean isQuietSet() {
        return this.quiet != null;
    }

    @Override
    public boolean isInteractiveMode() {
        return interactiveMode;
    }

    @Override
    public void setInteractiveMode(boolean interactiveMode) {
        this.interactiveMode = interactiveMode;
    }

    @Override
    public String getCommandName() {
        return "Statistics";
    }

    @Override
    public String getCommandDescription() {
        return "Get Stats desc";
    }
}
