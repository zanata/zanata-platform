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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.StubbingServerRule;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.*;

public class FileResourceClientTest {
    private static final Logger log =
            LoggerFactory.getLogger(FileResourceClientTest.class);
    private FileResourceClient client;

    @ClassRule
    public static StubbingServerRule
            stubbingServerRule = new StubbingServerRule();

    @Before
    public void setUp() throws URISyntaxException {
        RestClientFactory restClientFactory = MockServerTestUtil
                .createClientFactory(stubbingServerRule.getServerBaseUri());
        client = new FileResourceClient(restClientFactory);
    }

    @Test
    public void testServerAcceptedType() {
        List<DocumentType> serverAcceptedTypes = client
                .acceptedFileTypes();

        Set<String> allExtension = new HashSet<String>();
        for (DocumentType docType : serverAcceptedTypes) {
            allExtension.addAll(docType.getSourceExtensions());
        }
        assertThat(allExtension, Matchers.containsInAnyOrder("dtd", "pot",
                "txt", "idml", "html", "htm", "odt", "odp", "odg", "ods",
                "srt", "sbt", "sub", "vtt", "properties", "xlf", "ts", "json"));
    }

    @Test
    public void testFileTypeInfoList() {
        List<FileTypeInfo> serverAcceptedTypes = client
                .fileTypeInfoList();

        Set<String> allExtension = new HashSet<String>();
        for (FileTypeInfo docType : serverAcceptedTypes) {
            allExtension.addAll(docType.getSourceExtensions());
        }
        assertThat(allExtension, Matchers.containsInAnyOrder("dtd", "pot",
                "txt", "idml", "html", "htm", "odt", "odp", "odg", "ods",
                "srt", "sbt", "sub", "vtt", "properties", "xlf", "ts", "json"));
    }

    @Test
    public
            void testSourceFileUpload() throws Exception {
//        client = clientTalkingToRealServer();
        DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
        String resource = "test-odt.odt";
        InputStream fileInputStream = loadFromClasspath(resource);

        // TODO avoid direct file access
        // use/copy commons-io CountingInputStream and DigestInputStream
        uploadForm.setFileStream(fileInputStream);
        uploadForm.setFileType("odt");
        Pair<String, Long> fileHashAndSize =
                calculateFileHashAndSize(loadFromClasspath(resource));
        uploadForm.setHash(fileHashAndSize.getLeft());
        uploadForm.setFirst(true);
        uploadForm.setLast(true);
        uploadForm.setSize(fileHashAndSize.getRight());
        ChunkUploadResponse uploadResponse = client
                .uploadSourceFile("about-fedora", "master",
                        "test.odt",
                        uploadForm);
        log.info("response: {}", uploadResponse);
        assertThat(uploadResponse.getAcceptedChunks(), Matchers.equalTo(1));
    }

    private static InputStream loadFromClasspath(String resource) {
        InputStream resourceStream =
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(resource);
        return resourceStream;
    }

    @Test
    public void testTranslationFileUpload() throws Exception {
//        client = clientTalkingToRealServer();
        DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
        String resource = "zh-CN/test-odt.odt";
        InputStream fileInputStream = loadFromClasspath(resource);

        uploadForm.setFileStream(fileInputStream);
        uploadForm.setFileType("odt");
        Pair<String, Long> fileHashAndSize =
                calculateFileHashAndSize(loadFromClasspath(resource));
        uploadForm.setHash(fileHashAndSize.getLeft());
        uploadForm.setFirst(true);
        uploadForm.setLast(true);
        uploadForm.setSize(fileHashAndSize.getRight());
        ChunkUploadResponse uploadResponse = client
                .uploadTranslationFile("about-fedora", "master",
                        "zh",
                        "test.odt", "auto",
                        uploadForm);
        log.info("response: {}", uploadResponse);
        assertThat(uploadResponse.getAcceptedChunks(), Matchers.equalTo(1));
    }

    private Pair<String, Long> calculateFileHashAndSize(InputStream in) {
        CountingInputStream countingStream = new CountingInputStream(in);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try {
                in = new DigestInputStream(countingStream, md);
                byte[] buffer = new byte[256];
                while (in.read(buffer) > 0) {
                    // continue
                }
            } finally {
                in.close();
            }
            String hash = new String(Hex.encodeHex(md.digest()));
            return new ImmutablePair<>(hash, countingStream.getByteCount());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDownloadSourceFile() throws IOException {
        InputStream inputStream =
                client.downloadSourceFile("about-fedora", "master", "pot",
                        "About-Fedora").readEntity(InputStream.class);
        PoReader2 reader = new PoReader2();
        Resource resource =
                reader.extractTemplate(new InputSource(inputStream),
                        LocaleId.EN_US, "About-Fedora");
        assertThat(resource.getTextFlows(), Matchers.hasSize(1));
    }

    @Test
    public void testDownloadTranslationFile() {
        InputStream inputStream =
                client.downloadTranslationFile("about-fedora", "master", "es",
                        "po", "About-Fedora").readEntity(InputStream.class);
        PoReader2 reader = new PoReader2();
        TranslationsResource translationsResource =
                reader.extractTarget(new InputSource(inputStream));
        assertThat(translationsResource.getTextFlowTargets(),
                Matchers.hasSize(1));
    }

}


