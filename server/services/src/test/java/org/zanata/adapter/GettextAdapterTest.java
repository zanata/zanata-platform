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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.zanata.adapter.FileFormatAdapter.ParserOptions;
import org.zanata.adapter.FileFormatAdapter.WriterOptions;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.dto.TranslatedDoc;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author spathare <a href="mailto:spathare@redhat.com">spathare@redhat.com</a>
 *  @// TODO: 8/02/17 test ids
 */

public class GettextAdapterTest extends AbstractAdapterTest<GettextAdapter> {

    @Before
    public void setup() {
        adapter = new GettextAdapter();
    }

    @Test
    public void parsePOT() {
        Resource resource = parseTestFile("test-gettext.pot");
        assertThat(resource.getTextFlows()).hasSize(3);

        assertThat(resource.getTextFlows().get(0).getContents()).containsExactly("Line One");
        assertThat(resource.getTextFlows().get(1).getContents()).containsExactly("Line Two");
        assertThat(resource.getTextFlows().get(2).getContents()).containsExactly("Line Three");
    }

    @Test
    public void testGettextWithComment() {
        String testComment1 = " translator-comments";
        Resource resource = parseTestFile("test-gettext-comments.po");
        assertThat(resource.getTextFlows()).hasSize(3);

        PoHeader poHeader = (PoHeader) resource.getExtensions().iterator().next();
        assertThat(poHeader.getComment()).isEqualTo(testComment1);
    }

    @Test
    public void testGettextPlurals() {
        Resource resource = parseTestFile("test-gettext-plurals.po");
        assertThat(resource.getTextFlows()).hasSize(1);
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("%n file", "%n files");
    }

    @Test
    public void testGettextFuzzyFlag() {
        Resource resource = parseTestFile("test-gettext-flags.pot");
        assertThat(resource.getTextFlows()).hasSize(2);

        PotEntryHeader potEntryHeader = (PotEntryHeader) resource.getTextFlows()
                .get(0).getExtensions().iterator().next();
        assertThat(potEntryHeader.getFlags().get(0)).isEqualTo("fuzzy");
    }

    @Test
    public void testGettextReference() {
        Resource resource = parseTestFile("test-gettext-reference.pot");
        assertThat(resource.getTextFlows()).hasSize(2);

        PotEntryHeader potEntryHeader = (PotEntryHeader) resource.getTextFlows()
                .get(0).getExtensions().iterator().next();
        assertThat(potEntryHeader.getReferences().get(0)).isEqualTo("reference");
    }

    @Test
    public void testTranslatedGettext() {
        testTranslatedGettext(false);
    }

    @Test
    public void testTranslatedGettextApprovedOnly() {
        testTranslatedGettext(true);
    }

    private void testTranslatedGettext(boolean approvedOnly) {
        Resource resource = parseTestFile("test-gettext-untranslated.pot");

        String firstSourceId = resource.getTextFlows().get(0).getId();
        String secondSourceId = resource.getTextFlows().get(1).getId();
        String thirdSourceId = resource.getTextFlows().get(2).getId();

        TranslationsResource transResource = new TranslationsResource();
        addTranslation(transResource, firstSourceId, "Carpeta padre", ContentState.Approved);
        addTranslation(transResource, secondSourceId, "Asunto:", ContentState.Translated);
        addTranslation(transResource, thirdSourceId, "Conectar", ContentState.NeedReview);

        OutputStream outputStream = new ByteArrayOutputStream();
        ParserOptions sourceOptions = new ParserOptions(null, LocaleId.EN, "");
        TranslatedDoc translatedDoc = new TranslatedDoc(resource, transResource, LocaleId.ES);
        adapter.writeTranslatedFile(outputStream,
                new WriterOptions(sourceOptions, translatedDoc),
                approvedOnly);
        assertThat(outputStream.toString()).contains(
                "\n\nmsgctxt \"0293301ed6a54b7e4503e74bba17bf11\"\nmsgid \"Parent Folder\"\n" +
                        "msgstr \"Carpeta padre\"");
        if (approvedOnly) {
            assertThat(outputStream.toString()).contains(
                    "\n\n#, fuzzy\nmsgctxt \"47a0be8d1015d526a1fbaa56c3102135\"\nmsgid \"Subject:\"\n" +
                            "msgstr \"Asunto:\"");
        } else {
            assertThat(outputStream.toString()).contains(
                    "\n\nmsgctxt \"47a0be8d1015d526a1fbaa56c3102135\"\nmsgid \"Subject:\"\n" +
                            "msgstr \"Asunto:\"");
        }
        assertThat(outputStream.toString()).contains(
                "\n\n#, fuzzy\nmsgctxt \"49ab28040dfa07f53544970c6d147e1e\"\nmsgid \"Connect\"\n" +
                        "msgstr \"Conectar\"");
    }

}
