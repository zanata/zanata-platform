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
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class PropertiesUTF8AdapterTest extends AbstractAdapterTest<PropertiesUTF8Adapter> {

    @Before
    public void setup() {
        adapter = new PropertiesUTF8Adapter();
    }

    @Test
    public void parseUTF8Properties() {
        Resource resource = parseTestFile("test-properties-utf8.properties");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(
                "line1");
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("¥Line One");
    }

    @Test
    public void testTranslatedPropertiesDocument() {
        TranslationsResource tResource = new TranslationsResource();
        addTranslation(tResource, "line1", "¥Foun’dé metalkcta", ContentState.Approved);
        addTranslation(tResource, "line2", "¥Tba’dé metalkcta", ContentState.Translated);
        addTranslation(tResource, "line3", "¥Kba’dé metalkcta", ContentState.NeedReview);

        Resource resource = parseTestFile("test-properties-utf8.properties");
        File originalFile = new File(resourcePath.concat("test-properties-utf8.properties"));
        OutputStream outputStream = new ByteArrayOutputStream();

        adapter.writeTranslatedFile(outputStream,
                originalFile.toURI(),
                resource,
                tResource,
                "ru",
                Optional.absent());

        assertThat(outputStream.toString()).isEqualTo(
                "line1=¥Foun’dé metalkcta\n" +
                "line2=¥Tba’dé metalkcta\n" +
                "line3=\n");
    }

}
