/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.adapter.glossary;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import com.google.common.collect.Lists;


/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryCSVReader extends AbstractGlossaryPushReader {
    private final int batchSize;
    private final LocaleId srcLang;
    private final static String POS = "POS";
    private final static String DESC = "DESCRIPTION";

    /**
     * This class will close the reader
     *
     * @param batchSize
     */
    public GlossaryCSVReader(LocaleId srcLang, int batchSize) {
        this.srcLang = srcLang;
        this.batchSize = batchSize;
    }

    public List<List<GlossaryEntry>> extractGlossary(Reader reader)
            throws IOException {
        int entryCount = 0;
        try {
            Iterable<CSVRecord> rawRecords = CSVFormat.RFC4180.parse(reader);
            List<CSVRecord> records = Lists.newArrayList(rawRecords);

            validateCSVEntries(records);
            Map<String, Integer> descriptionMap = setupDescMap(records);
            Map<Integer, LocaleId> localeColMap =
                    setupLocalesMap(records, descriptionMap);

            LocaleId srcLocale = localeColMap.get(0);

            if (!srcLang.equals(srcLocale)) {
                throw new RuntimeException("input source language '" + srcLang
                        + "' does not match source language in file '"
                        + srcLocale + "'");
            }

            List<List<GlossaryEntry>> glossaries = Lists.newArrayList();
            List<GlossaryEntry> glossaryEntries = Lists.newArrayList();

            for (int i = 1; i < records.size(); i++) {
                CSVRecord row = records.get(i);
                GlossaryEntry entry = new GlossaryEntry();
                entry.setSrcLang(srcLocale);
                entry.setPos(row.get(descriptionMap.get(POS)));
                entry.setDescription(row.get(descriptionMap.get(DESC)));

                for (int x = 0; x < row.size()
                    && localeColMap.containsKey(x); x++) {
                    LocaleId locale = localeColMap.get(x);
                    String content = row.get(x);

                    GlossaryTerm term = new GlossaryTerm();

                    term.setLocale(locale);
                    term.setContent(content);

                    entry.getGlossaryTerms().add(term);
                }
                glossaryEntries.add(entry);
                entryCount++;

                if (entryCount == batchSize || i == records.size() - 1) {
                    glossaries.add(glossaryEntries);
                    entryCount = 0;
                    glossaryEntries = Lists.newArrayList();
                }
            }
            return glossaries;
        } finally {
            reader.close();
        }
    }

    /**
     * Basic validation of CVS file format - At least 2 rows in the CVS file -
     * Empty content validation - All row must have the same column count
     */
    private void validateCSVEntries(@Nonnull List<CSVRecord> records) {
        if (records.isEmpty()) {
            throw new RuntimeException("Invalid CSV file - empty file");
        }
        if (records.size() < 2) {
            throw new RuntimeException("Invalid CSV file - no entries found");
        }
        for (int i = 1; i < records.size(); i++) {
            CSVRecord record = records.get(i);
            //checking each row size is matching with header size
            if (records.get(0).size() != record.size()) {
                throw new RuntimeException(
                    "Invalid CSV file - inconsistency of columns with header");
            }
        }
    }

    /**
     * Parser reads from all from first row and exclude column from description
     * map. Format of CVS: {source locale},{locale},{locale}...,pos,description
     */
    private Map<Integer, LocaleId> setupLocalesMap(List<CSVRecord> records,
            Map<String, Integer> descriptionMap) {
        Map<Integer, LocaleId> localeColMap = new HashMap<Integer, LocaleId>();
        CSVRecord headerRow = records.get(0);
        for (int row = 0; row <= headerRow.size()
                && !descriptionMap.containsValue(row); row++) {

            LocaleId locale = new LocaleId(StringUtils.trim(headerRow.get(row)));
            localeColMap.put(row, locale);
        }
        return localeColMap;
    }

    /**
     * Read last 2 columns in CSV:
     * {source locale},{locale},{locale}...,pos,description
     *
     * @param records
     */
    private Map<String, Integer> setupDescMap(List<CSVRecord> records) {
        Map<String, Integer> descMap = new HashMap<String, Integer>();
        CSVRecord headerRow = records.get(0);
        descMap.put(POS, headerRow.size() - 2);
        descMap.put(DESC, headerRow.size() - 1);
        return descMap;
    }
}
