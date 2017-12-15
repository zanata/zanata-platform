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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.QualifiedName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryCSVReader extends AbstractGlossaryPushReader {

    private final LocaleId srcLang;
    private final static String POS = "POS";
    private final static String[] POSSYNONYMS =
            {"POS", "PARTOFSPEECH", "PART OF SPEECH"};
    private final static String DESC = "DESCRIPTION";
    private final static String[] DESCSYNONYMS =
            {"DESCRIPTION", "DESC", "DEFINITION"};
    private final static int TERM = 0;

    /**
     * This class will close the reader
     *
     */
    public GlossaryCSVReader(LocaleId srcLang) {
        this.srcLang = srcLang;
    }

    public Map<LocaleId, List<GlossaryEntry>> extractGlossary(Reader reader,
            String qualifiedName) throws IOException {
        try {
            Iterable<CSVRecord> rawRecords = CSVFormat.RFC4180.parse(reader);
            List<CSVRecord> records = Lists.newArrayList(rawRecords);

            validateCSVEntries(records);
            Map<String, Integer> descriptionMap = setupDescMap(records);
            Map<Integer, LocaleId> localeColMap =
                    setupLocalesMap(records, descriptionMap);

            LocaleId srcLocale = localeColMap.get(TERM);

            if (!srcLang.equals(srcLocale)) {
                throw new RuntimeException("input source language '" + srcLang
                        + "' does not match source language in file '"
                        + srcLocale + "'");
            }
            Map<LocaleId, List<GlossaryEntry>> results = Maps.newHashMap();

            for (CSVRecord row : records.subList(1, records.size())) {
                GlossaryEntry entry = new GlossaryEntry();
                entry.setSrcLang(srcLocale);
                if (descriptionMap.containsKey(POS)) {
                    entry.setPos(row.get(descriptionMap.get(POS)));
                }
                if (descriptionMap.containsKey(DESC)) {
                    entry.setDescription(row.get(descriptionMap.get(DESC)));
                }
                entry.setQualifiedName(new QualifiedName(qualifiedName));

                for (int x = 1; x < row.size()
                    && localeColMap.containsKey(x); x++) {

                    GlossaryTerm srcTerm = new GlossaryTerm();
                    srcTerm.setLocale(srcLocale);
                    srcTerm.setContent(row.get(TERM));

                    entry.getGlossaryTerms().add(srcTerm);

                    LocaleId transLocaleId = localeColMap.get(x);
                    String transContent = row.get(x);

                    GlossaryTerm transTerm = new GlossaryTerm();
                    transTerm.setLocale(transLocaleId);
                    transTerm.setContent(transContent);
                    entry.getGlossaryTerms().add(transTerm);

                    List<GlossaryEntry> list = results.get(transLocaleId);
                    if (list == null) {
                        list = Lists.newArrayList();
                    }
                    list.add(entry);
                    results.put(transLocaleId, list);
                }
            }
            return results;
        } finally {
            reader.close();
        }
    }

    /**
     * Basic validation of CSV file format - At least 2 rows in the CSV file -
     * Empty content validation - All rows must have the same column count.
     * @param records list of records representing a csv file
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
     * Parser reads from all from first row and up to first recognised
     * non-locale column mapping.
     * Format of CSV: {source locale},{locale},{locale}...,pos,description
     * @param records list of records representing a csv file
     * @param descriptionMap header locations of non-locale columns
     */
    private Map<Integer, LocaleId> setupLocalesMap(List<CSVRecord> records,
            Map<String, Integer> descriptionMap) {
        Map<Integer, LocaleId> localeColMap = new HashMap<>();
        CSVRecord headerRow = records.get(0);
        for (int column = 0; column < headerRow.size()
                && !descriptionMap.containsValue(column); column++) {
            LocaleId locale =
                    new LocaleId(StringUtils.trim(headerRow.get(column)));
            localeColMap.put(column, locale);
        }
        return localeColMap;
    }

    /**
     * Locate trailing data columns in CSV:
     * {source locale},{locale},{locale}...,pos,description,...
     *
     * @param records list of records representing a csv file
     */
    private Map<String, Integer> setupDescMap(List<CSVRecord> records) {
        Map<String, Integer> descMap = new HashMap<>();
        CSVRecord headerRow = records.get(0);
        for (int position = 1; position < headerRow.size(); position++) {
            String headerItem = headerRow.get(position);
            if (headerItem.trim().length() == 0) {
                continue;
            }
            if (Arrays.asList(POSSYNONYMS).contains(headerItem.toUpperCase())) {
                descMap.put(POS, position);
            } else if (Arrays.asList(DESCSYNONYMS)
                    .contains(headerItem.toUpperCase())) {
                descMap.put(DESC, position);
            }
        }
        return descMap;
    }
}
