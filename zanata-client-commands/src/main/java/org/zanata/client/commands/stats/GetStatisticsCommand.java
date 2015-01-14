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
package org.zanata.client.commands.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.util.ConsoleUtils;
import org.zanata.rest.client.StatisticsResourceClient;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class GetStatisticsCommand extends
        ConfigurableCommand<GetStatisticsOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(GetStatisticsCommand.class);
    private final StatisticsResourceClient client;


    public GetStatisticsCommand(GetStatisticsOptions opts) {
        super(opts);
        client = getClientFactory().getStatisticsClient();
    }

    @Override
    public void run() throws Exception {

        String[] localeListArg = null;

        if (getOpts().getLocaleMapList() != null) {
            localeListArg = new String[getOpts().getLocaleMapList().size()];
            // Get the locales from the mappings list
            LocaleList localeMapList = getOpts().getLocaleMapList();
            for (int i = 0; i < localeMapList.size(); i++) {
                LocaleMapping locMapping = localeMapList.get(i);
                localeListArg[i] = locMapping.getLocale();
            }
        }

        ContainerTranslationStatistics containerStats;

        if (getOpts().getProj() == null) {
            log.error("Project slug is required but was not found. Aborting.");
            return;
        }

        if (getOpts().getProj().isEmpty()) {
            log.error("Project slug is empty. Aborting.");
            return;
        }

        if (getOpts().getProjectVersion() == null) {
            log.error("Version slug is required but was not found. Aborting.");
            return;
        }

        if (getOpts().getProjectVersion().isEmpty()) {
            log.error("Version slug is empty. Aborting.");
            return;
        }

        ConsoleUtils.startProgressFeedback();
        // Document Id not specified
        if (getOpts().getDocumentId() == null) {
            containerStats =
                    client.getStatistics(getOpts().getProj(), getOpts()
                            .getProjectVersion(),
                            getOpts().getIncludeDetails(), getOpts()
                                    .getIncludeWordLevelStats(), localeListArg);
        } else {
            // Otherwise, stats for the single document
            containerStats =
                    client
                            .getStatistics(getOpts().getProj(), getOpts()
                                    .getProjectVersion(), getOpts()
                                    .getDocumentId(), getOpts()
                                    .getIncludeWordLevelStats(), localeListArg);
        }
        ConsoleUtils.endProgressFeedback();

        if (getOpts().getFormat() == null) {
            log.warn("Output format not specified; defaulting to Console output.");
        }

        // Select the format (output)
        ContainerStatisticsCommandOutput statsOutput;
        switch (getOpts().getFormat()) {
            case "csv":
                statsOutput = new CsvStatisticsOutput();
                break;
            case "console":
                statsOutput = new ConsoleStatisticsOutput();
                break;
            default:
                log.warn(
                        "Invalid format type '{}', using console format instead.",
                        getOpts().getFormat());
                statsOutput = new ConsoleStatisticsOutput();
        }

        statsOutput.write(containerStats);
    }

}
