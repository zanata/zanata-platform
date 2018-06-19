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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.LocaleId;

import net.sf.okapi.filters.ts.TsFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.DocumentType;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
// TODO test writeTranslatedFile
public class TSAdapterTest extends AbstractAdapterTest<TSAdapter> {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        adapter = new TSAdapter();
    }

    @Test
    public void parseTS() {
        Resource resource = parseTestFile("basicts.ts");
        assertThat(getContext(resource.getTextFlows().get(0))).isEqualTo("Test");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents()).containsExactly("Line One");
        assertThat(resource.getTextFlows().get(1).getContents()).containsExactly("Line Two");
        assertThat(resource.getTextFlows().get(2).getContents()).containsExactly("Line Three");
    }

    /*
     * TS plural sets source textflow to have same content for number of plurals
     */
    @Test
    public void testTSWithPlurals() {
        Resource resource = parseTestFile("test-ts-plurals.ts");
        assertThat(resource.getTextFlows()).hasSize(1);
        assertThat(resource.getTextFlows().get(0).isPlural()).isTrue();
        assertThat(resource.getTextFlows().get(0).getContents().size()).isEqualTo(2);
    }

    @Test
    public void testTSWithMultipleContexts() {
        Resource resource = parseTestFile("test-ts-multiplecontexts.ts");
        assertThat(resource.getTextFlows()).hasSize(2);

        TextFlow first = resource.getTextFlows().get(0);
        assertThat(first.getContents()).containsExactly("First source");
        assertThat(getContext(first)).isEqualTo("testContext1");

        TextFlow second = resource.getTextFlows().get(1);
        assertThat(second.getContents()).containsExactly("Second source");
        assertThat(getContext(second)).isEqualTo("testContext2");
    }

    @Test
    public void testTSWithComments() {
        Resource resource = parseTestFile("test-ts-comment.ts");
        assertThat(resource.getTextFlows()).hasSize(3);

        TextFlow first = resource.getTextFlows().get(0);
        assertThat(first.getContents()).containsExactly("Line One");
        assertThat(getComment(first)).isEqualTo("Extra comment");

        TextFlow second = resource.getTextFlows().get(1);
        assertThat(second.getContents()).containsExactly("Line Two");
        assertThat(getComment(second)).isEqualTo("Second comment");
    }

    @Test
    public void testGeneratedFilename() throws Exception {
        HDocument document = new HDocument("/test/basicts.ts",
                "basicts.ts", "test/", ContentType.PO,
                new HLocale(new org.zanata.common.LocaleId("en")));
        HRawDocument hRawDocument = new HRawDocument();
        hRawDocument.setType(DocumentType.TS);
        document.setRawDocument(hRawDocument);

        assertThat(adapter.generateTranslationFilename(document, "fr"))
                .isEqualTo("basicts_fr.ts");
        assertThat(adapter.generateTranslationFilename(document, "en_AU"))
                .isEqualTo("basicts_en_AU.ts");
    }

    @Test
    public void testGeneratedFilenameMissingExtension() throws Exception {
        HDocument document = new HDocument("/test/basicts",
                "basicts", "test/", ContentType.PO,
                new HLocale(new org.zanata.common.LocaleId("en")));
        HRawDocument hRawDocument = new HRawDocument();
        hRawDocument.setType(DocumentType.TS);
        document.setRawDocument(hRawDocument);

        assertThat(adapter.generateTranslationFilename(document, "fr"))
                .isEqualTo("basicts_fr.ts");
        assertThat(adapter.generateTranslationFilename(document, "en_AU"))
                .isEqualTo("basicts_en_AU.ts");
    }

    @Test
    public void testUploadedTranslationsFile() throws Exception {
        File file = getTestFile("test-ts-translated.ts");
        RawDocument rawDocument = new RawDocument(
                FileUtils.readFileToString(file),
                new LocaleId("en"),
                new LocaleId("dv-LL"));
        TranslationsResource translationsResource =
                getAdapter().parseTranslationFile(rawDocument,
                        "");

        assertThat(translationsResource.getTextFlowTargets().size()).isEqualTo(2);
        assertThat(translationsResource.getTextFlowTargets().get(0).getContents())
                .containsExactly("Foun’dé metalkcta");
        assertThat(translationsResource.getTextFlowTargets().get(1).getContents())
                .containsExactly("Tba’dé metalkcta");
    }

    @Test
    public void testTranslatedTSDocument() throws Exception {
        testTranslatedTSDocument(false);
    }

    @Test
    public void testTranslatedTSDocumentApprovedOnly() {
        testTranslatedTSDocument(true);
    }

    private void testTranslatedTSDocument(boolean approvedOnly) {
        Resource resource = parseTestFile("test-ts-untranslated.ts");
        Map<String, TextFlowTarget> translations = new HashMap<>();
        addTranslation(translations, resource.getTextFlows().get(0).getId(),
                "Foun’dé metalkcta",
                ContentState.Approved);
        addTranslation(translations, resource.getTextFlows().get(1).getId(),
                "Tba’dé metalkcta",
                ContentState.Translated);
        // TODO test NeedReview as well (should be omitted or marked as unfinished)
        File originalFile = getTestFile("test-ts-untranslated.ts");
        LocaleId localeId = new LocaleId("en");
        OutputStream outputStream = new ByteArrayOutputStream();
        try (
                TsFilter tsFilter = new TsFilter();
                IFilterWriter writer = tsFilter.createFilterWriter()) {
            writer.setOptions(localeId, Charsets.UTF_8.name());
            writer.setOutput(outputStream);
            getAdapter()
                    .generateTranslatedFile(originalFile.toURI(), translations,
                            localeId, writer, "", approvedOnly);
        }
        // the second translation (Translated) should only have type=unfinished if approvedOnly is set
        String maybeTypeUnfinished = approvedOnly ? "type=\"unfinished\" " : "";
        assertThat(outputStream.toString()).isEqualTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE TS []>\n" +
                        "<TS version=\"2.1\" sourcelanguage=\"en\" language=\"sv\">\n" +
                        "<message>\n" +
                        "    <source>First source</source>\n" +
                        "<translation variants=\"no\">Foun’dé metalkcta</translation>\n" +
                        "  </message><message>\n" +
                        "      <source>Second source</source>\n" +
                        "<translation " + maybeTypeUnfinished + "variants=\"no\">Tba’dé metalkcta</translation>\n" +
                        "    </message>\n" +
                        "</TS>\n");
    }

    @Test
    public void testFailToParseTSDocument() throws Exception {
        exception.expect(FileFormatAdapterException.class);
        exception.expectMessage("Unable to parse document");
        parseTestFile("test-ts-invalid.ts");
    }

    @Test
    public void testFailToParseTSTranslation() throws Exception {
        File file = getTestFile("test-ts-invalid.ts");
        RawDocument rawDocument = new RawDocument(
                FileUtils.readFileToString(file),
                new LocaleId("en"),
                new LocaleId("ru"));
        exception.expect(FileFormatAdapterException.class);
        exception.expectMessage("Unable to parse translation file");
        getAdapter().parseTranslationFile(rawDocument, "");
    }

    @Test
    public void testFailToParseOriginalFile() throws Exception {
        exception.expect(FileFormatAdapterException.class);
        exception.expectMessage("Unable to generate translated document from original");
        try (
                TsFilter tsFilter = new TsFilter();
                IFilterWriter filterWriter = tsFilter.createFilterWriter()) {
            getAdapter().generateTranslatedFile(
                    getTestFile("test-ts-nonexistent.ts").toURI(),
                    new HashMap<>(),
                    new LocaleId("en"),
                    filterWriter, "", false);
        }
    }

    private String getContext(TextFlow textFlow) {
        PotEntryHeader potEntryHeader = textFlow.getExtensions(true)
                .findByType(PotEntryHeader.class);
        if (potEntryHeader != null) {
            return potEntryHeader.getContext();
        }
        return null;
    }

    private String getComment(TextFlow textFlow) {
        SimpleComment simpleComment = textFlow.getExtensions(true)
                .findByType(SimpleComment.class);
        if (simpleComment != null) {
            return simpleComment.getValue();
        }
        return null;
    }

}
