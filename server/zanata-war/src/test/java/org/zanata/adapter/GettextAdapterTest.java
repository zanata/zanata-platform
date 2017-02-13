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
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author spathare <a href="mailto:spathare@redhat.com">spathare@redhat.com</a>
 *  @// TODO: 8/02/17 test ids
 */

public class GettextAdapterTest {

    private GettextAdapter adapter;
    private File testFile;
    private String filePath = "src/test/resources/org/zanata/adapter/";

    @Before
    public void setup() {
        adapter = new GettextAdapter();
    }

    private Resource parseTestFile(String filename) {
        testFile = new File(filePath.concat(filename));
        assert testFile.exists();
        return adapter.parseDocumentFile(testFile.toURI(),
                LocaleId.EN, Optional.absent());
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
        assertThat(resource.getTextFlows().get(0).getContents()).containsExactly("%n file", "%n files");
    }

    @Test
    public void testGettextFuzzyFlag() {
        Resource resource = parseTestFile("test-gettext-flags.pot");
        assertThat(resource.getTextFlows()).hasSize(2);

        PotEntryHeader potEntryHeader = (PotEntryHeader) resource.getTextFlows().get(0).getExtensions().iterator().next();
        assertThat(potEntryHeader.getFlags().get(0)).isEqualTo("fuzzy");

    }


    @Test
    public void testGettextReference() {
        Resource resource = parseTestFile("test-gettext-reference.pot");
        assertThat(resource.getTextFlows()).hasSize(2);

        PotEntryHeader potEntryHeader = (PotEntryHeader) resource.getTextFlows().get(0).getExtensions().iterator().next();
        assertThat(potEntryHeader.getReferences().get(0)).isEqualTo("reference");
    }

    @Test
    public void testTranslatedGettext() {

        TranslationsResource transResource = new TranslationsResource();
        addTranslation(transResource, "0293301ed6a54b7e4503e74bba17bf11", "Carpeta padre", ContentState.Approved);
        addTranslation(transResource, "47a0be8d1015d526a1fbaa56c3102135", "Asunto:", ContentState.Translated);
        addTranslation(transResource, "49ab28040dfa07f53544970c6d147e1e", "Conectar", ContentState.NeedReview);

        Resource resource = parseTestFile("test-gettext-translated.po");
        File originalFile = new File(filePath.concat("test-gettext-translated.po"));
        OutputStream outputStream = new ByteArrayOutputStream();

        adapter.writeTranslatedFile(outputStream, originalFile.toURI(), resource, transResource, "es", Optional.absent());

        assertThat(outputStream.toString()).isEqualTo(
                "#, fuzzy\n" +
                        "msgid \"\"\n" +
                        "msgstr \"\"\n" +
                        "\"Content-Type: text/plain; charset=UTF-8\\n\"\n" +
                        "\"Content-Transfer-Encoding: 8bit\\n\"\n" +
                        "\"MIME-Version: 1.0\\n\"\n\n" +
                        "msgctxt \"0293301ed6a54b7e4503e74bba17bf11\"\n" +
                        "msgid \"Parent Folder\"\n" +
                        "msgstr \"Carpeta padre\"\n\n" +
                        "msgctxt \"47a0be8d1015d526a1fbaa56c3102135\"\n" +
                        "msgid \"Subject:\"\n" +
                        "msgstr \"Asunto:\"\n\n" +
                        "#, fuzzy\n" +
                        "msgctxt \"49ab28040dfa07f53544970c6d147e1e\"\n" +
                        "msgid \"Connect\"\n" +
                        "msgstr \"Conectar\"\n\n");

    }

    private TranslationsResource addTranslation(TranslationsResource resource,
                                        String id, String content, ContentState state) {
        TextFlowTarget textFlowTarget = new TextFlowTarget();
        textFlowTarget.setResId(id);
        textFlowTarget.setContents(content);
        textFlowTarget.setState(state);
        resource.getTextFlowTargets().add(textFlowTarget);
        return resource;
    }

}
