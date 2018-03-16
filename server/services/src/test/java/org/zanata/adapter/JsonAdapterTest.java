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
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.filters.json.JSONFilter;
import net.sf.okapi.common.LocaleId;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TextFlowTarget;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class JsonAdapterTest extends AbstractAdapterTest<JsonAdapter> {

    private LocaleId localeId = new LocaleId("en");

    @Before
    public void setup() {
        adapter = new JsonAdapter();
    }

    @Test
    public void parseJSON() {
        Resource resource = parseTestFile("basicjson.json");
        assertThat(resource.getTextFlows()).hasSize(5);
        assertThat(getTextFlowContentsAt(resource, 0)).containsExactly("Line One");
    }

    /*
     * JSON parts with duplicated keys are handled separately
     */
    @Test
    public void testStandaloneElementsAreIncluded() {
        Resource resource = parseTestFile("basicjson.json");
        assertThat(getTextFlowContentsAt(resource, 3)).containsExactly("First");
        assertThat(getTextFlowContentsAt(resource, 4)).containsExactly("Second");
    }

    /*
     * JSON parts with duplicated keys are separate
     */
    @Test
    public void testDuplicateKeys() {
        Resource resource = parseTestFile("test-json-duplicateids.json");
        assertThat(getTextFlowContentsAt(resource, 1)).containsExactly("Line Two");
        assertThat(getTextFlowContentsAt(resource, 2)).containsExactly("Line Three");
    }

    /*
     * JSON parts with duplicated key and content can be considered one entity
     */
    @Test
    public void testDuplicateKeyAndContent() {
        Resource resource = parseTestFile("test-json-duplicateentries.json");
        assertThat(resource.getTextFlows()).hasSize(2);
        assertThat(getTextFlowContentsAt(resource, 0)).containsExactly("Dupe");
        assertThat(getTextFlowContentsAt(resource, 1)).containsExactly("Same");
    }

    /*
     * JSON parts with similar content are separate
     */
    @Test
    @Ignore("ZNTA-1731")
    public void testDuplicateContent() {
        Resource resource = parseTestFile("test-json-duplicatecontent.json");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(getTextFlowContentsAt(resource, 1)).containsExactly("Same");
        assertThat(getTextFlowContentsAt(resource, 2)).containsExactly("Same");
    }

    @Test
    public void testTranslatedJSONDocument() throws Exception {
        Resource resource = parseTestFile("test-json-untranslated.json");
        assertThat(resource.getTextFlows().get(1).getContents())
                .containsExactly("First Source");
        assertThat(resource.getTextFlows().get(2).getContents())
                .containsExactly("Second Source");
        String firstSourceId = resource.getTextFlows().get(1).getId();
        String secondSourceId = resource.getTextFlows().get(2).getId();

        Map<String, TextFlowTarget> translations = new HashMap<>();

        TextFlowTarget firstTranslation = new TextFlowTarget();
        firstTranslation.setContents("Foun’dé metalkcta");
        firstTranslation.setState(ContentState.Approved);
        translations.put(firstSourceId, firstTranslation);

        TextFlowTarget secondTranslation = new TextFlowTarget();
        secondTranslation.setContents("Tba’dé metalkcta");
        secondTranslation.setState(ContentState.Translated);
        translations.put(secondSourceId, secondTranslation);

        File originalFile = getTestFile("test-json-untranslated.json");
        OutputStream outputStream = new ByteArrayOutputStream();
        try (IFilterWriter writer = createWriter(outputStream)) {
            adapter.generateTranslatedFile(originalFile.toURI(), translations,
                    this.localeId, writer, Optional.absent());
        }

        assertThat(outputStream.toString()).isEqualTo("{\n"+
            "  \"test\": {\n" +
            "    \"title\": \"Test\",\n" +
            "    \"test1\": {\n" +
            "      \"title\": \"Foun’dé metalkcta\"\n" +
            "    },\n" +
            "    \"test2\": {\n" +
            "      \"title\": \"Tba’dé metalkcta\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n");
    }

    // we do clean up the writer, but in the caller
    @SuppressWarnings("all")
    private IFilterWriter createWriter(OutputStream outputStream) {
        IFilterWriter writer = new JSONFilter().createFilterWriter();
        writer.setOptions(this.localeId, Charsets.UTF_8.name());
        writer.setOutput(outputStream);
        return writer;
    }

    private List<String> getTextFlowContentsAt(Resource resource, int position) {
        return resource.getTextFlows().get(position).getContents();
    }

}
