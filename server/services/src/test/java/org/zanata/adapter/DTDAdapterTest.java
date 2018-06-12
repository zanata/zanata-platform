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

import com.google.common.base.Optional;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

import java.io.File;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class DTDAdapterTest extends AbstractAdapterTest<DTDAdapter> {

    @Before
    public void setup() {
        adapter = new DTDAdapter();
    }

    @Test
    public void parseBasicDTDFile() {
        Resource resource = parseTestFile("basicdtd.dtd");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents()).containsExactly("Line One");
    }

    @Test
    public void testTranslatedDTDDocument() {
        testTranslatedDTDDocument(false);
    }

    @Test
    public void testTranslatedDTDDocumentApprovedOnly() {
        testTranslatedDTDDocument(true);
    }

    private void testTranslatedDTDDocument(boolean approvedOnly) {
        File testFile = getTestFile("basicdtd.dtd");
        Resource resource = parseTestFile("basicdtd.dtd");

        TranslationsResource translationsResource = new TranslationsResource();
        addTranslation(translationsResource,
                resource.getTextFlows().get(0).getId(),
                "Dakta Amna",
                ContentState.Approved);
        addTranslation(translationsResource,
                resource.getTextFlows().get(1).getId(),
                "Dakta Tba",
                ContentState.Translated);
        addTranslation(translationsResource,
                resource.getTextFlows().get(2).getId(),
                "Dakta Kba",
                ContentState.NeedReview);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        adapter.writeTranslatedFile(output, testFile.toURI(),
                resource, translationsResource, "dv-DL", Optional.absent(),
                approvedOnly);

        String trans1 = "Dakta Amna";
        // use the English source if approvedOnly
        String trans2 = approvedOnly ? "Line Two" : "Dakta Tba";
        // just the English source
        String trans3 = "Line Three";
        assertThat(output.toString(UTF_8)).isEqualTo(
                "<!ENTITY firstField \"" + trans1 + "\">\n" +
                        "<!ENTITY secondField \"" + trans2 + "\">\n" +
                        "<!ENTITY thirdField \"" + trans3 + "\">\n");
    }

}
