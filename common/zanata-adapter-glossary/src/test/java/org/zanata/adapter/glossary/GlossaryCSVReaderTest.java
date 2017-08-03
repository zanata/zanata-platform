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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.service.GlossaryResource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryCSVReaderTest {

    @Test(expected = RuntimeException.class)
    public void testNotMatchingSource() throws IOException {
        GlossaryCSVReader reader = new GlossaryCSVReader(LocaleId.DE);
        File sourceFile =
            new File("src/test/resources/glossary/translate1.csv");

        Reader inputStreamReader =
            new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        reader.extractGlossary(br, GlossaryResource.GLOBAL_QUALIFIED_NAME);
    }

    @Test
    public void extractGlossaryTest1() throws IOException {

        GlossaryCSVReader reader = new GlossaryCSVReader(LocaleId.EN_US);
        int entryPerLocale = 2;

        File sourceFile =
                new File("src/test/resources/glossary/translate1.csv");

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        Map<LocaleId, List<GlossaryEntry>> glossaries =
                reader.extractGlossary(br,
                        GlossaryResource.GLOBAL_QUALIFIED_NAME);

        assertThat(glossaries.keySet().size(), equalTo(2));
        assertThat(glossaries.keySet(),
                contains(LocaleId.ES, new LocaleId("zh")));

        for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : glossaries
                .entrySet()) {
            assertThat(entry.getValue().size(),
                    Matchers.equalTo(entryPerLocale));
        }
    }

    @Test
    public void extractGlossaryTest2() throws IOException {
        GlossaryCSVReader reader = new GlossaryCSVReader(LocaleId.EN_US);
        int entryPerLocale = 2;

        File sourceFile =
                new File("src/test/resources/glossary/translate2.csv");

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        Map<LocaleId, List<GlossaryEntry>> glossaries =
                reader.extractGlossary(br, GlossaryResource.GLOBAL_QUALIFIED_NAME);

        assertThat(glossaries.keySet().size(), equalTo(2));
        assertThat(glossaries.keySet(),
                contains(LocaleId.ES, new LocaleId("zh")));
        for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : glossaries
            .entrySet()) {
            assertThat(entry.getValue().size(),
                Matchers.equalTo(entryPerLocale));
        }
    }
}
