/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.json.JSONObject;
import org.json.JSONArray;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.QualifiedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class GlossaryJsonReader {
    private final LocaleId srcLang;

    private final static String TERM = "term";
    private final static String TRANSLATIONS = "translations";
    private final static String[] POSSYNONYMS =
            {"pos", "partofspeech", "part of speech"};
    private final static String[] DESCSYNONYMS =
            {"desc", "description", "definition"};
    private final static String[] EXTERNALID = {"id", "externalid", "external id"};

    public GlossaryJsonReader(LocaleId srcLang) {
        this.srcLang = srcLang;
    }

    /**
     * Extract a glossary from a representative json file.
     * The format of the glossary should be:
     * {"terms": [
     *   {
     *     "term": "hello",
     *     "id": "hello-verb"
     *     "desc": "testing of hello json",
     *     "pos": "verb",
     *     "translations": { "es": "Hola", "zh": "您好" }
     *     "synonyms": "Hi",
     *     ...
     *   },
     *   term2...
     * ]}
     * @param reader input source for the json content
     * @param qualifiedName name for the glossary, e.g. global, projectname
     * @return a map of glossary entries
     * @throws IOException if the file is not available
     */
    public Map<LocaleId, List<GlossaryEntry>> extractGlossary(Reader reader,
            String qualifiedName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String content = bufferedReader.lines().collect(Collectors.joining());
        reader.close();
        Map<LocaleId, List<GlossaryEntry>> results = Maps.newHashMap();

        try {
            JSONObject jsonObj = new JSONObject(content);
            JSONArray termsArray = jsonObj.getJSONArray("terms");
            List<GlossaryEntry> empty = Lists.newArrayList();
            // Iterate through the terms
            for (int current = 0; current < termsArray.length(); ++current) {
                Object obj = termsArray.get(current);
                if (!(obj instanceof JSONObject)) {
                    continue;
                }
                JSONObject entry = ((JSONObject) obj);
                if (!entry.has(TERM)) {
                    continue;
                }
                String srcTerm = entry.getString(TERM);
                GlossaryEntry glossaryEntry = new GlossaryEntry();
                String description = getValueOf(DESCSYNONYMS, entry);
                if (!isBlank(description)) {
                    glossaryEntry.setDescription(getValueOf(DESCSYNONYMS, entry));
                }
                String pos = getValueOf(POSSYNONYMS, entry);
                if (!isBlank(pos)) {
                    glossaryEntry.setPos(pos);
                }
                glossaryEntry.setQualifiedName(new QualifiedName(qualifiedName));
                glossaryEntry.setSrcLang(srcLang);
                glossaryEntry.setExternalId(getValueOf(EXTERNALID, entry));
                GlossaryTerm glossaryTerm = new GlossaryTerm();
                glossaryTerm.setLocale(srcLang);
                glossaryTerm.setContent(srcTerm);
                glossaryEntry.getGlossaryTerms().add(glossaryTerm);
                // Iterate through the translations
                if (entry.has(TRANSLATIONS) &&
                        entry.get(TRANSLATIONS) instanceof JSONObject) {
                    JSONObject translations = (JSONObject) entry.get(TRANSLATIONS);
                    Iterator<?> transKeys = translations.keys();

                    while (transKeys.hasNext()) {
                        String locale = (String) transKeys.next();
                        if (translations.getString(locale) != null) {
                            LocaleId transLocaleId = new LocaleId(locale);
                            String transContent = translations.getString(locale);

                            GlossaryTerm transTerm = new GlossaryTerm();
                            transTerm.setLocale(transLocaleId);
                            transTerm.setContent(transContent);
                            glossaryEntry.getGlossaryTerms().add(transTerm);
                        }
                    }
                }
                List<GlossaryEntry> srcEntries = firstNonNull(
                        results.get(srcLang), empty);
                srcEntries.add(glossaryEntry);
                results.put(srcLang, srcEntries);
            }
        } catch (ClassCastException | JSONException exception) {
            throw new RuntimeException("Invalid JSON glossary file: "
                    .concat(exception.getMessage()));
        }
        return results;
    }

    /*
     * Attempt to return a value from the json data based on a key synonym
     */
    private String getValueOf(String[] synonyms, JSONObject data) {
        for (String option : synonyms) {
            if (data.has(option)) {
                return data.getString(option);
            } else if (data.has(option.toUpperCase())) {
                return data.getString(option.toUpperCase());
            }
        }
        return StringUtils.EMPTY;
    }
}
