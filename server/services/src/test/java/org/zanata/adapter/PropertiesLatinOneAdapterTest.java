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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;

import org.junit.Before;
import org.junit.Test;
import org.zanata.adapter.FileFormatAdapter.WriterOptions;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.common.dto.TranslatedDoc;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class PropertiesLatinOneAdapterTest extends AbstractAdapterTest<PropertiesLatinOneAdapter> {

    @Before
    public void setup() {
        adapter = new PropertiesLatinOneAdapter();
    }

    @Test
    public void parseLatinOneProperties() throws Exception {
        File latin1EncodedFile = createTempPropertiesFile(ISO_8859_1);
        Resource resource =
                adapter.parseDocumentFile(new FileFormatAdapter.ParserOptions(
                        latin1EncodedFile.toURI(), LocaleId.EN,
                        ""));
        assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(
                "line1");
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("ÀLine One");
    }

    /*
     * Properties files change path, not name, so name should be identical
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
                .isEqualTo("basicprop_fr.properties");
    }

    @Test
    public void testTranslatedPropertiesDocument() throws Exception {
        TranslationsResource tResource = new TranslationsResource();

        addTranslation(tResource, "line1", "ÀFounde metalkcta", ContentState.Approved);
        addTranslation(tResource, "line2", "ÀTbade metalkcta", ContentState.Translated);
        addTranslation(tResource, "line3", "ÀKbade metalkcta", ContentState.NeedReview);

        File latin1EncodedFile = createTempPropertiesFile(ISO_8859_1);
        Resource resource =
                adapter.parseDocumentFile(new FileFormatAdapter.ParserOptions(
                        latin1EncodedFile.toURI(), LocaleId.EN,
                        ""));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        FileFormatAdapter.ParserOptions
                sourceOptions = new FileFormatAdapter.ParserOptions(null, LocaleId.EN, "");
        TranslatedDoc translatedDoc = new TranslatedDoc(resource, tResource, new LocaleId("ru"));
        adapter.writeTranslatedFile(output,
                new WriterOptions(sourceOptions, translatedDoc),
                false);

        // \u00C0 is the escaped unicode form of À
        assertThat(output.toString(ISO_8859_1)).isEqualTo(
                "line1=\\u00C0Founde metalkcta\n" +
                "line2=\\u00C0Tbade metalkcta\n" +
                "line3=\n");
    }

}
