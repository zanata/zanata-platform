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

import org.hamcrest.Matchers;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryCSVReaderTest {

    @Test(expected = RuntimeException.class)
    public void testNotMatchingSource() throws IOException {
        GlossaryCSVReader reader = new GlossaryCSVReader(LocaleId.DE, 300);
        File sourceFile =
            new File("src/test/resources/glossary/translate1.csv");

        Reader inputStreamReader =
            new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        List<List<GlossaryEntry>> glossaries = reader.extractGlossary(br);
    }

    @Test
    public void extractGlossaryTest1() throws IOException {

        GlossaryCSVReader reader = new GlossaryCSVReader(LocaleId.EN_US, 300);

        File sourceFile =
                new File("src/test/resources/glossary/translate1.csv");

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        List<List<GlossaryEntry>> glossaries = reader.extractGlossary(br);
        assertThat(glossaries.size(), Matchers.equalTo(1));

        assertThat(glossaries.get(0).size(), Matchers.equalTo(2));

        for (GlossaryEntry entry : glossaries.get(0)) {
            assertThat(entry.getGlossaryTerms().size(), Matchers.equalTo(3));
        }

    }

    @Test
    public void extractGlossaryTest2() throws IOException {
        GlossaryCSVReader reader = new GlossaryCSVReader(LocaleId.EN_US, 300);

        File sourceFile =
                new File("src/test/resources/glossary/translate2.csv");

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        List<List<GlossaryEntry>> glossaries = reader.extractGlossary(br);
        // System.out.println(glossary);
        assertThat(glossaries.size(), Matchers.equalTo(1));

        assertThat(glossaries.get(0).size(),
                Matchers.equalTo(2));

        for (GlossaryEntry entry : glossaries.get(0)) {
            assertThat(entry.getGlossaryTerms().size(), Matchers.equalTo(3));
        }

    }
}
