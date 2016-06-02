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

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import org.apache.commons.csv.CSVPrinter;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Outputs statistics in CSV format to the console.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CsvStatisticsOutput implements ContainerStatisticsCommandOutput {
    private static final String NEW_LINE_SEPARATOR = "\n";

    @Override
    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public void write(ContainerTranslationStatistics statistics) {
        try {
            OutputStreamWriter streamWriter = new OutputStreamWriter(System.out);
            try {
                CSVPrinter csvPrinter = new CSVPrinter(streamWriter,
                    CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR));

                try {
                    writeToCsv(statistics, csvPrinter);
                    csvPrinter.flush();
                } finally {
                    csvPrinter.close();
                }
            } finally {
                streamWriter.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToCsv(ContainerTranslationStatistics statistics,
            CSVPrinter writer) throws IOException {

        writer.printRecord(Lists.newArrayList());

        // Display headers
        Link sourceRef = statistics.getRefs().findLinkByRel("statSource");
        if (sourceRef.getType().equals("PROJ_ITER")) {
            writer.printRecord(Lists.newArrayList("Project Version: ",
                    statistics.getId()));
        } else if (sourceRef.getType().equals("DOC")) {
            writer.printRecord(
                    Lists.newArrayList("Document: ", statistics.getId()));
        }

        // Write headers
        writer.printRecord(Lists.newArrayList("Locale", "Unit", "Total",
                "Translated", "Need Review", "Untranslated", "Last Translated"));

        // Write stats
        if (statistics.getStats() != null) {
            for (TranslationStatistics transStats : statistics.getStats()) {
                writer.printRecord(Lists.newArrayList(transStats.getLocale(),
                        transStats.getUnit().toString(),
                        Long.toString(transStats.getTotal()),
                        Long.toString(transStats.getTranslatedAndApproved()),
                        Long.toString(transStats.getDraft()),
                        Long.toString(transStats.getUntranslated()),
                        transStats.getLastTranslated()));
            }
        }

        writer.printRecord(Lists.newArrayList());

        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Detailed stats
        if (statistics.getDetailedStats() != null) {
            for (ContainerTranslationStatistics detailedStats : statistics
                    .getDetailedStats()) {
                writeToCsv(detailedStats, writer);
            }
        }
    }
}
