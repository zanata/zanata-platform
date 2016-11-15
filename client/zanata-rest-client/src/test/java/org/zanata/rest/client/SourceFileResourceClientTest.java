/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.FileUploadResponse;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.StubbingServerRule;

import javax.ws.rs.core.Response;

public class SourceFileResourceClientTest {
    private static final Logger log =
            LoggerFactory.getLogger(SourceFileResourceClientTest.class);
    private SourceFileResourceClient client;

    @ClassRule
    public static StubbingServerRule
            stubbingServerRule = new StubbingServerRule();

    @Before
    public void setUp() throws URISyntaxException {
        RestClientFactory restClientFactory = MockServerTestUtil
                .createClientFactory(stubbingServerRule.getServerBaseUri());
        client = new SourceFileResourceClient(restClientFactory);
    }

    private static File loadFileFromClasspath(String file) {
        return new File(Thread.currentThread().getContextClassLoader()
                .getResource(file).getFile());
    }

    @Test
    public void testSourceFileUpload() throws Exception {
        File odtFile = loadFileFromClasspath("xliff/StringResource_en_US.xml");
        FileInputStream fileInputStream = new FileInputStream(odtFile);

        FileUploadResponse uploadResponse = client
                .uploadSourceFile("about-fedora", "master",
                        "test.odt", ProjectType.File,
                        fileInputStream, odtFile.length());
        log.info("response: {}", uploadResponse);
        assertThat(uploadResponse.getSuccessMessage(), Matchers.notNullValue());
    }

    @Test
    public void testDownloadSourceFile() throws IOException {
        Response response =
                client.downloadSourceFile("about-fedora", "master",
                        "About-Fedora", ProjectType.File);
        assertEquals(200, response.getStatus());
        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            PoReader2 reader = new PoReader2();
            Resource resource = reader.extractTemplate(new InputSource(inputStream),
                    LocaleId.EN_US, "About-Fedora");
            assertThat(resource.getTextFlows(), Matchers.hasSize(1));
        }
    }

}


