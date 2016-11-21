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
package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.stats.GetStatisticsCommand;
import org.zanata.client.commands.stats.GetStatisticsOptions;

/**
 * Get translation statistics from Zanata.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Mojo(name = "stats", requiresOnline = true, requiresProject = false)
public class GetStatisticsMojo extends
        ConfigurableProjectMojo<GetStatisticsOptions> implements
        GetStatisticsOptions {

    /**
     * Include detailed statistics.
     */
    @Parameter(property = "zanata.details", defaultValue = "false")
    private boolean includeDetails;

    /**
     * Include word-level stats. By default, only message-level statistics will
     * be fetched.
     */
    @Parameter(property = "zanata.word", defaultValue = "false")
    private boolean includeWordLevelStats;

    /**
     * Output format for the statistics. Valid options are: csv - For a csv
     * format (via the console). console (default) - For regular console
     * printing.
     */
    @Parameter(property = "zanata.format", defaultValue = "console")
    private String format;

    /**
     * Document id to fetch statistics for.
     */
    @Parameter(property = "zanata.docId")
    private String documentId;

    @Override
    public ConfigurableCommand<GetStatisticsOptions> initCommand() {
        return new GetStatisticsCommand(this);
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
    public String getCommandName() {
        return "stats";
    }

    @Override
    public boolean isAuthRequired() {
        return false;
    }
}
