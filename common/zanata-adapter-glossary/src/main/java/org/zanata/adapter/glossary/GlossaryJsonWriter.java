/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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

import com.google.common.base.Charsets;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class GlossaryJsonWriter extends AbstractGlossaryPullWriter {

    public GlossaryJsonWriter() {
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
     * This outputs a json file of given <code>transLocales</code>.
     */
    public void write(@Nonnull final Writer fileWriter,
                      @Nonnull final List<GlossaryEntry> entries,
                      @Nonnull final LocaleId srcLocale,
                      @Nonnull final List<LocaleId> transLocales) throws IOException {

        JSONObject root = new JSONObject();
        try {

            JSONArray entriesOut = new JSONArray();

            for (GlossaryEntry entry : entries) {
                GlossaryTerm srcTerm =
                        getGlossaryTerm(entry.getGlossaryTerms(), srcLocale);

                JSONObject newEntry = new JSONObject();
                newEntry.put("id", entry.getExternalId());
                newEntry.put("term", srcTerm.getContent());
                newEntry.put("description", entry.getDescription());
                newEntry.put("pos", entry.getPos());

                JSONObject translations = new JSONObject();
                for (LocaleId transLocale : transLocales) {
                    GlossaryTerm transTerm =
                            getGlossaryTerm(entry.getGlossaryTerms(), transLocale);
                    if (transTerm != null) {
                        translations.put(transTerm.getLocale().toJavaName(), transTerm.getContent());
                    }
                }
                newEntry.put("translations", translations);
                entriesOut.put(newEntry);
            }
            root.put("terms", entriesOut);
        } finally {
            fileWriter.write(root.toString(2));
            fileWriter.close();
        }
    }

}
