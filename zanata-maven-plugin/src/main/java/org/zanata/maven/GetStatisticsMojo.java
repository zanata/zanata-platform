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

import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.stats.GetStatisticsCommand;
import org.zanata.client.commands.stats.GetStatisticsOptions;

/**
 * Get translation statistics from Zanata.
 *
 * @goal stats
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class GetStatisticsMojo extends
        ConfigurableProjectMojo<GetStatisticsOptions> implements
        GetStatisticsOptions {
    /**
     * Include detailed statistics.
     *
     * @parameter expression="${zanata.details}" default-value="false"
     */
    private boolean includeDetails;

    /**
     * Include word-level stats. By default, only message-level statistics will
     * be fetched.
     *
     * @parameter expression="${zanata.word}" default-value="false"
     */
    private boolean includeWordLevelStats;

    /**
     * Output format for the statistics. Valid options are: csv - For a csv
     * format (via the console). console (default) - For regular console
     * printing.
     *
     * @parameter expression="${zanata.format}" default-value="console"
     */
    private String format;

    /**
     * Document id to fetch statistics for.
     *
     * @parameter expression="${zanata.docId}"
     */
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
