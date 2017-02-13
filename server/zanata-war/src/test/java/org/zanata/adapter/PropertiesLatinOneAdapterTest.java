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

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class PropertiesLatinOneAdapterTest extends PropertiesAbstractTest {

    @Before
    public void setup() {
        adapter = new PropertiesLatinOneAdapter();
    }

    @Test
    public void parseLatinOneProperties() {
        Resource resource = parseTestFile("test-properties-latin1.properties");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(
                "line1");
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of("Line One"));
    }

    /*
     * Properties files change path, not name
     */
    @Test
    public void testGeneratedFilename() throws Exception {
        HDocument document = new HDocument("/test/basicprop.properties",
                "basicprop.properties", "test/", ContentType.PO,
                new HLocale(new org.zanata.common.LocaleId("en")));
        HRawDocument hRawDocument = new HRawDocument();
        hRawDocument.setType(DocumentType.PROPERTIES);
        document.setRawDocument(hRawDocument);

        assertThat(adapter.generateTranslationFilename(document, "fr"))
                .isEqualTo("basicprop.properties");
    }

    @Test
    public void testTranslatedPropertiesDocument() {
        TranslationsResource tResource = new TranslationsResource();
        addTranslation(tResource, "line1", "Founde metalkcta", ContentState.Approved);
        addTranslation(tResource, "line2", "Tbade metalkcta", ContentState.Translated);
        addTranslation(tResource, "line3", "Kbade metalkcta", ContentState.NeedReview);

        Resource resource = parseTestFile("test-properties-latin1.properties");
        File originalFile = new File(resourcePath.concat("test-properties-latin1.properties"));
        OutputStream outputStream = new ByteArrayOutputStream();

        adapter.writeTranslatedFile(outputStream,
                originalFile.toURI(),
                resource,
                tResource,
                "ru",
                Optional.absent());

        assertThat(outputStream.toString()).isEqualTo(
                "line1=Founde metalkcta\nline2=Tbade metalkcta\nline3=\n");
    }

    @Test
    public void testLatin1encoding() throws Exception {
        File latin1EncodedFile = createTempFile(StandardCharsets.ISO_8859_1);
        Resource resource =
                adapter.parseDocumentFile(latin1EncodedFile.toURI(), LocaleId.EN,
                        Optional.absent());
        assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(
                "line1");
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("Line One");
    }

    @Test
    public void testUTFOnLatin1encoding() throws Exception {
        File latin1EncodedFile = createTempFile(StandardCharsets.UTF_8);
        Resource resource =
                adapter.parseDocumentFile(latin1EncodedFile.toURI(), LocaleId.EN,
                        Optional.absent());
        assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(
                "line1");
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("Â¥Line One");
    }

}
