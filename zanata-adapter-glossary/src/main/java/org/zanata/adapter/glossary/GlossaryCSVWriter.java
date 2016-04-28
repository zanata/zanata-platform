package org.zanata.adapter.glossary;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;

import org.zanata.rest.dto.GlossaryTerm;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Class to write glossary entries into CSV file format.
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryCSVWriter extends AbstractGlossaryPullWriter {
    private static final Logger log =
        LoggerFactory.getLogger(GlossaryCSVWriter.class);

    private static final String NEW_LINE_SEPARATOR = "\n";

    public GlossaryCSVWriter() {
    }

    public void write(final Writer fileWriter,
        final List<GlossaryEntry> entries, final LocaleId srcLocale,
        final List<LocaleId> transLocales)
        throws IOException {
        if (entries == null || entries.isEmpty()) {
            log.warn("No glossary entries to process.");
            return;
        }
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
                CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR));
        try {
            int size = transLocales.size() + 3;
            List<String> header = generateHeader(srcLocale, transLocales);
            log.debug("Writing header-" + header);
            csvPrinter.printRecord(header);

            for (GlossaryEntry entry : entries) {
                GlossaryTerm srcTerm =
                    getGlossaryTerm(entry.getGlossaryTerms(), srcLocale);

                List<String> row = Lists.newArrayList();
                row.add(0, srcTerm != null ? srcTerm.getContent() : "");


                int index = 1;
                for (LocaleId transLocale : transLocales) {
                    GlossaryTerm transTerm =
                        getGlossaryTerm(entry.getGlossaryTerms(), transLocale);
                    row.add(index,
                            transTerm != null ? transTerm.getContent() : "");
                    index++;
                }

                row.add(size - 2, entry.getPos());
                row.add(size - 1, entry.getDescription());

                log.debug("Writing row-" + row);
                csvPrinter.printRecord(row);
            }
        } finally {
            csvPrinter.flush();
            csvPrinter.close();
        }
    }

    private List<String> generateHeader(LocaleId srcLocale, List<LocaleId> transLocales) {
        final int size = transLocales.size() + 3;

        List<String> result = Lists.newArrayList();
        result.add(0, srcLocale.toString());

        int index = 1;
        for (LocaleId transLocale: transLocales) {
            result.add(index, transLocale.toString());
            index++;
        }
        String pos = "pos";
        String desc = "description";
        result.add(size - 2, pos);
        result.add(size - 1, desc);

        return result;
    }
}
