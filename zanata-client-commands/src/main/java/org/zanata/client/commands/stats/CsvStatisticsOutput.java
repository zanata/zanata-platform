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

import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Outputs statistics in CSV format to the console.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CsvStatisticsOutput implements ContainerStatisticsCommandOutput {
    @Override
    public void write(ContainerTranslationStatistics statistics) {
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(System.out));
        writeToCsv(statistics, csvWriter);
        try {
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToCsv(ContainerTranslationStatistics statistics,
            CSVWriter writer) {
        writer.writeNext(new String[] {});

        // Display headers
        Link sourceRef = statistics.getRefs().findLinkByRel("statSource");
        if (sourceRef.getType().equals("PROJ_ITER")) {
            writer.writeNext(new String[] { "Project Version: ",
                    statistics.getId() });
        } else if (sourceRef.getType().equals("DOC")) {
            writer.writeNext(new String[] { "Document: ", statistics.getId() });
        }

        // Write headers
        writer.writeNext(new String[] { "Locale", "Unit", "Total",
                "Translated", "Need Review", "Untranslated", "Last Translated" });

        // Write stats
        if (statistics.getStats() != null) {
            for (TranslationStatistics transStats : statistics.getStats()) {
                writer.writeNext(new String[] { transStats.getLocale(),
                        transStats.getUnit().toString(),
                        Long.toString(transStats.getTotal()),
                        Long.toString(transStats.getTranslated()),
                        Long.toString(transStats.getNeedReview()),
                        Long.toString(transStats.getUntranslated()),
                        transStats.getLastTranslated() });
            }
        }

        writer.writeNext(new String[] {});

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
