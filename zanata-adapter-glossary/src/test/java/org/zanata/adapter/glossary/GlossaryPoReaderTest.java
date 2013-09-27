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

import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.Glossary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = "unit-tests")
public class GlossaryPoReaderTest {
    private final File sourceFile = new File(
            "src/test/resources/glossary/fuel_hi.po"); // 578
                                                       // glossary
                                                       // entries
    private final File sourceFile2 = new File(
            "src/test/resources/glossary/compendium-zh_TW.po"); // 2645
                                                                // glossary
                                                                // entries

    private final int sourceSize1 = 578;
    private final int sourceSize2 = 2645;

    private final int BATCH_SIZE = 50;

    @Test
    public void extractGlossaryTest() throws IOException {
        GlossaryPoReader reader =
                new GlossaryPoReader(LocaleId.EN_US, new LocaleId("hi"), false,
                        BATCH_SIZE);

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        List<Glossary> glossaries = reader.extractGlossary(br);
        assertThat(glossaries.size(),
                equalTo((int) Math.ceil(sourceSize1 * 1F / BATCH_SIZE)));
        assertThat(glossaries.get(0).getGlossaryEntries().size(),
                equalTo(BATCH_SIZE));
        assertThat(glossaries.get(1).getGlossaryEntries().size(),
                equalTo(BATCH_SIZE));

        assertThat(glossaries.get(glossaries.size() - 1).getGlossaryEntries()
                .size(), equalTo(sourceSize1 % BATCH_SIZE));
    }

    @Test
    public void glossaryBatchTest() throws IOException {
        GlossaryPoReader reader =
                new GlossaryPoReader(LocaleId.EN_US, new LocaleId("zh-Hants"),
                        false, BATCH_SIZE);
        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile2), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        List<Glossary> glossaries = reader.extractGlossary(br);
        assertThat(glossaries.size(),
                equalTo((int) Math.ceil(sourceSize2 * 1F / BATCH_SIZE)));
        assertThat(glossaries.get(0).getGlossaryEntries().size(),
                equalTo(BATCH_SIZE));
        assertThat(glossaries.get(1).getGlossaryEntries().size(),
                equalTo(BATCH_SIZE));
        assertThat(glossaries.get(glossaries.size() - 1).getGlossaryEntries()
                .size(), equalTo(sourceSize2 % BATCH_SIZE));
    }
}
