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
package org.zanata.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class IdmlAdapterTest extends AbstractAdapterTest<IDMLAdapter> {

    @Before
    public void setup() {
        adapter = new IDMLAdapter();
    }

    @Test
    public void parseIDML() {
        Resource resource = parseTestFile("test-idml.idml");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents()).containsExactly("Line One");
    }

    @Test
    public void testTranslatedIDMLDocument() throws Exception {
        TranslationsResource translationsResource = new TranslationsResource();
        File originalFile = getTestFile("test-idml.idml");
        Resource resource = parseTestFile("test-idml.idml");

        addTranslation(translationsResource,
                resource.getTextFlows().get(0).getId(),
                "Dakta Amna", ContentState.Approved);
        addTranslation(translationsResource,
                resource.getTextFlows().get(1).getId(),
                "Dakta Tba",
                ContentState.Translated);
        addTranslation(translationsResource,
                resource.getTextFlows().get(2).getId(),
                "Dakta Kba",
                ContentState.NeedReview);

        File outputFile = File.createTempFile("test-idml-translated", ".idml");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        adapter.writeTranslatedFile(output, originalFile.toURI(),
                resource, translationsResource, "dv-DL", "",
                false);
        output.writeTo(new FileOutputStream(outputFile));

        Resource translatedResource = adapter.parseDocumentFile(new FileFormatAdapter.ParserOptions(
                outputFile.toURI(), new LocaleId("en"), ""));

        assertThat(translatedResource.getTextFlows().get(0).getContents())
                .containsExactly("Dakta Amna");
        assertThat(translatedResource.getTextFlows().get(1).getContents())
                .containsExactly("Dakta Tba");
        // translation is fuzzy, so use the English source
        assertThat(translatedResource.getTextFlows().get(2).getContents())
                .containsExactly("Line Three");
    }


}
