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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
// TODO test writeTranslatedFile
public class XliffAdapterTest extends AbstractAdapterTest<XliffAdapter> {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        adapter = new XliffAdapter();
    }

    @Test
    public void parseXLIFF() {
        Resource resource = parseTestFile("test-xliff.xlf");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("Line One");
    }

    @Test
    public void parseXliffWithHtmlTags() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Sorry, Zanata does not " +
                "support elements inside source: g");
        parseTestFile("test-xliff-tagged.xlf");

    }

    @Test
    public void parseTranslationsFile() throws Exception {
        File translationFile = getTestFile("test-xliff-translated.xlf");
        File tempFile = File.createTempFile("test-xliff-translated", ".xlf");
        // Xliff implementation deletes the file
        FileUtils.copyFile(translationFile, tempFile);
        assertThat(tempFile.exists());
        LocaleId sourceLocale = LocaleId.fromJavaName("en");

        TranslationsResource translationsResource =
                adapter.parseTranslationFile(tempFile.toURI(), sourceLocale,
                        "fr", Optional.absent());
        assertThat(translationsResource.getTextFlowTargets().get(0).getContents())
                .containsExactly("Imanétaba Amna");
        assertThat(translationsResource.getTextFlowTargets().get(1).getContents())
                .containsExactly("Imanétaba Tba");
        assertThat(translationsResource.getTextFlowTargets().get(2).getContents())
                .containsExactly("Imanétaba Kba");
    }

    @Test
    public void testTranslatedXliffDocument() throws Exception {
        Resource resource = parseTestFile("test-xliff.xlf");
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

        File outputFile = File.createTempFile("test-xliff-translated", ".xlf");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        adapter.writeTranslatedFile(output, null,
                resource, translationsResource, "dv-DL", Optional.absent());
        output.writeTo(new FileOutputStream(outputFile));

        assertThat(output.toString()).contains(
                "        <source>Line One</source>\n" +
                "        <target>Dakta Amna</target>");
        assertThat(output.toString()).contains(
                "        <source>Line Two</source>\n" +
                "        <target>Dakta Tba</target>");
        // Assert fuzzy is not copied anywhere to output
        assertThat(output.toString()).doesNotContain("<target>Dakta Kba</target>");
    }
}
