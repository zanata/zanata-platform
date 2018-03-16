package org.zanata.adapter.glossary;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;

import org.zanata.rest.dto.GlossaryTerm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;

/**
 * Class to write glossary entries into CSV file format.
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryCSVWriter extends AbstractGlossaryPullWriter {
    private static final Logger log =
        LoggerFactory.getLogger(GlossaryCSVWriter.class);

    private static final String NEW_LINE_SEPARATOR = "\n";

    public GlossaryCSVWriter() {
    }

    /**
     * @see {@link #write(Writer, List, LocaleId, List)}
     */
    public void write(@Nonnull OutputStream stream,
            @Nonnull final List<GlossaryEntry> entries,
            @Nonnull final LocaleId srcLocale,
            @Nonnull final List<LocaleId> transLocales) throws IOException {
        OutputStreamWriter osWriter =
                new OutputStreamWriter(stream, Charsets.UTF_8);
        write(osWriter, entries, srcLocale, transLocales);
    }

    /**
     * This output a csv files of given <code>transLocales</code>.
     * First column - source string of <code>srcLocale</code> from {@link GlossaryEntry#glossaryTerms}
     * Second last column (part of speech)- {@link GlossaryEntry#pos}
     * Last column (description)-  {@link GlossaryEntry#description}
     * Between first and second last column - translations of <code>transLocales</code> from {@link GlossaryEntry#glossaryTerms}
     */
    public void write(@Nonnull final Writer fileWriter,
            @Nonnull final List<GlossaryEntry> entries,
            @Nonnull final LocaleId srcLocale,
            @Nonnull final List<LocaleId> transLocales) throws IOException {
        if (fileWriter == null) {
            log.warn("Missing fileWriter.");
            return;
        }
        if (entries == null) {
            log.warn("No glossary entries to process.");
            return;
        }
        if (srcLocale == null || transLocales == null) {
            log.warn("Missing source locale and translation locales.");
            return;
        }
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
                CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR));
        try {
            List<String> header = generateHeader(srcLocale, transLocales);
            log.debug("Writing header-{}", header);
            csvPrinter.printRecord(header);

            for (GlossaryEntry entry : entries) {
                GlossaryTerm srcTerm =
                    getGlossaryTerm(entry.getGlossaryTerms(), srcLocale);

                List<String> row = Lists.newArrayList();
                row.add(srcTerm != null ? srcTerm.getContent() : "");

                for (LocaleId transLocale : transLocales) {
                    GlossaryTerm transTerm =
                        getGlossaryTerm(entry.getGlossaryTerms(), transLocale);
                    row.add(transTerm != null ? transTerm.getContent() : "");
                }

                int totalSize = header.size();
                row.add(getPosColumn(totalSize), entry.getPos());
                row.add(getDescColumn(totalSize), entry.getDescription());

                log.debug("Writing row-{}", row);
                csvPrinter.printRecord(row);
            }
        } finally {
            csvPrinter.flush();
            csvPrinter.close();
        }
    }

    private List<String> generateHeader(LocaleId srcLocale,
        List<LocaleId> transLocales) {
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
        result.add(getPosColumn(size), pos);
        result.add(getDescColumn(size), desc);

        return result;
    }

    private int getPosColumn(int totalSize) {
        return totalSize - 2;
    }

    private int getDescColumn(int totalSize) {
        return totalSize - 1;
    }
}
