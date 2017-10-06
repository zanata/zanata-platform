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

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.service.GlossaryResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryPoReaderTest {
    // 578 glossary entries
    private final File sourceFile = new File(
            "src/test/resources/glossary/fuel_hi.po");

    private static final int sourceSize1 = 578;

    @Test
    public void extractGlossaryTest() throws IOException {
        LocaleId localeId = new LocaleId("hi");
        GlossaryPoReader reader =
                new GlossaryPoReader(LocaleId.EN_US, localeId);

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        Map<LocaleId, List<GlossaryEntry>> glossaries =
                reader.extractGlossary(br, GlossaryResource.GLOBAL_QUALIFIED_NAME);
        assertThat(glossaries.keySet()).contains(localeId);
        assertThat(glossaries.get(localeId)).hasSize(sourceSize1);
    }

}
