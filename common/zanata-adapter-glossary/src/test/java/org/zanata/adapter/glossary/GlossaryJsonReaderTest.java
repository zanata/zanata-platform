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

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.service.GlossaryResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import java.util.List;
import java.util.Map;

public class GlossaryJsonReaderTest {

    @Test
    public void extractGlossary() throws IOException {
        Map<LocaleId, List<GlossaryEntry>> glossaries = getGlossaries("glossary");

        assertThat(glossaries.entrySet()).hasSize(1);

        for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : glossaries.entrySet()) {
            assertThat(entry.getValue().get(0).getDescription()).isEqualTo("testing of hello json");
            assertThat(entry.getValue().get(0).getExternalId()).isEqualTo("hello-verb");
            assertThat(entry.getValue().get(1).getDescription()).isEqualTo("testing of morning json");
            assertThat(entry.getValue().get(1).getExternalId()).isEmpty();
        }
    }

    @Test
    public void emptyGlossary() throws IOException {
        Map<LocaleId, List<GlossaryEntry>> glossaries = getGlossaries("glossaryEmpty");
        assertThat(glossaries.keySet()).hasSize(0);
    }

    @Test
    public void emptyGlossaryEntry() throws IOException {
        Map<LocaleId, List<GlossaryEntry>> glossaries = getGlossaries("glossaryEmptyEntry");
        assertThat(glossaries.keySet()).hasSize(0);
    }

    @Test
    public void glossaryEntryMissingDesc() throws IOException {
        Map<LocaleId, List<GlossaryEntry>> glossaries = getGlossaries("glossaryNoDesc");
        assertThat(glossaries.keySet()).hasSize(1);

        for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : glossaries.entrySet()) {
            assertThat(entry.getValue().get(0).getPos()).isEqualTo("verb");
            assertThat(entry.getValue().get(0).getDescription()).isNull();
        }
    }

    @Test
    public void glossaryEntryMissingPos() throws IOException {
        Map<LocaleId, List<GlossaryEntry>> glossaries = getGlossaries("glossaryNoPos");
        assertThat(glossaries.keySet()).hasSize(1);

        for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : glossaries.entrySet()) {
            assertThat(entry.getValue().get(0).getPos()).isNull();
            assertThat(entry.getValue().get(0).getDescription()).isEqualTo("testing of hello json");
        }
    }

    private Map<LocaleId, List<GlossaryEntry>> getGlossaries(String filename) throws IOException {
        GlossaryJsonReader glossaryJsonReader = new GlossaryJsonReader(LocaleId.EN_US);
        String fullPath = "src/test/resources/glossary/" + filename + ".json";
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(fullPath)), "UTF-8"));

        return glossaryJsonReader.extractGlossary(br, GlossaryResource.GLOBAL_QUALIFIED_NAME);
    }
}
