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
import static org.zanata.rest.client.ClientUtil.calculateFileMD5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.DocumentType;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.FileUploadResponse;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.StubbingServerRule;

import com.sun.jersey.api.client.ClientResponse;

public class TranslatedFileResourceClientTest {
    private static final Logger log =
            LoggerFactory.getLogger(TranslatedFileResourceClientTest.class);
    private TranslatedFileResourceClient client;

    @ClassRule
    public static StubbingServerRule
            stubbingServerRule = new StubbingServerRule();

    @Before
    public void setUp() throws URISyntaxException {
        RestClientFactory restClientFactory = MockServerTestUtil
                .createClientFactory(stubbingServerRule.getServerBaseUri());
//        client = resteasyClient.target(uri).proxyBuilder(TranslatedFileResource.class);
        client = new TranslatedFileResourceClient(restClientFactory);
    }

    private static File loadFileFromClasspath(String file) {
        return new File(Thread.currentThread().getContextClassLoader()
                .getResource(file).getFile());
    }

    @Test
    public void testTranslationFileUpload() throws Exception {
        DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
        File source = loadFileFromClasspath("zh-CN/test-odt.odt");
        FileInputStream fileInputStream = new FileInputStream(source);

        uploadForm.setFileStream(fileInputStream);
//        uploadForm.setFileType("odt");
        FileUploadResponse uploadResponse = client
                .uploadTranslatedFile("about-fedora", "master",
                        "zh",
                        "test.odt", "auto",
                        ProjectType.File,
                        fileInputStream);
        log.info("response: {}", uploadResponse);
        assertThat(uploadResponse.getSuccessMessage(), Matchers.notNullValue());
    }

    @Test
    public void testDownloadTranslationFile() {
        ClientResponse response =
                client.downloadTranslatedFile("about-fedora", "master", "es",
                        "po", "About-Fedora", ProjectType.File);
        assertEquals(200, response.getStatus());
        InputStream inputStream =
                response.getEntity(InputStream.class);
        PoReader2 reader = new PoReader2();
        TranslationsResource translationsResource =
                reader.extractTarget(new InputSource(inputStream));
        assertThat(translationsResource.getTextFlowTargets(),
                Matchers.hasSize(1));
    }

}


