/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.service.raw;

import org.apache.commons.codec.binary.Hex;
import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.common.DocumentType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.ResourceRequestEnvironment;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.service.FileResource;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.annotation.Annotation;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.rest.service.FileResource.FILETYPE_GETTEXT;
import static org.zanata.rest.service.FileResource.FILETYPE_OFFLINE_PO;
import static org.zanata.rest.service.FileResource.FILETYPE_RAW_SOURCE_DOCUMENT;
import static org.zanata.rest.service.FileResource.FILETYPE_TRANSLATED_APPROVED;
import static org.zanata.test.AssertjKt.describedBy;
import static org.zanata.util.RawRestTestUtils.assertHeaderValue;

public class FileRawRestITCase extends RestTest {

    private static final String DOCTYPE_XLIFF = "XLIFF";
    private static final String DOCTYPE_GETTEXT = "GETTEXT";
    private static final String DOCTYPE_PLAIN_TEXT = DocumentType.PLAIN_TEXT.name();
    // from test-gettext.pot:
    private static final String[] BLANK_TRANSLATIONS = {
            "Parent Folder", "",
            "Subject:", "",
            "Connect", "" };
    // from test-gettext-translated.po:
    private static final String[] FIRST_TRANSLATIONS = {
            "Parent Folder", "Carpeta padre",
            "Subject:", "Asunto:",
            "Connect", "Conectar" };
    private static final String[] SECOND_TRANSLATIONS = {
            "Parent Folder", "Carpeta principal",
            "Subject:", "Tema:",
            "Connect", "Conectar" };
    private static final String[] SECOND_TRANSLATIONS_EXCLUDING_UNCHANGED = {
            "Parent Folder", "Carpeta principal",
            "Subject:", "Tema:",
            "Connect", "" };
    private FileClient fileResource;

    private static final Annotation[] multipartFormAnnotations =
            { new MultipartFormLiteral() };

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void downloadAsGettext1() {
        // uses data from TextFlowTestData.dbunit.xml
        final Response response = getFileResource()
                .downloadTranslationFile("sample-project", "1.0", "en-US", FILETYPE_GETTEXT, "my/path/document.txt", false);

        assertThat(response.getStatus()).isEqualTo(200);
        assertHeaderValue(response, "Content-Disposition",
                "attachment; filename=\"document.txt.po\"");
        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE,
                MediaType.TEXT_PLAIN);

        String entityString = response.readEntity(String.class);

        assertPoFileCorrect(entityString);
        assertPoFileContainsTranslations(entityString, "hello world", "");
    }

    @Test
    @RunAsClient
    public void downloadAsGettext2() {
        // uses data from TextFlowTestData.dbunit.xml
        Response response = getFileResource()
                .downloadTranslationFile("sample-project", "1.0", "en-US", FILETYPE_GETTEXT, "my/path/document-2.txt", false);

        // Ok
        assertThat(response.getStatus()).isEqualTo(200);
        assertHeaderValue(response, "Content-Disposition",
                "attachment; filename=\"document-2.txt.po\"");
        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE,
                MediaType.TEXT_PLAIN);

        String entityString = response.readEntity(String.class);
        assertPoFileCorrect(entityString);
        assertPoFileContainsTranslations(
                entityString, "mssgId1",
                "mssgTrans1", "mssgId2", "mssgTrans2", "mssgId3",
                "mssgTrans3");
    }

    @Test
    @RunAsClient
    public void downloadXliff11Source() throws Exception {
        String filename = "test-xliff.xlf";
        File testFile = getTestFile(filename);
        uploadSourceFile(filename, DOCTYPE_XLIFF);
        String downloadedXliffContent = downloadSourceFile(FILETYPE_RAW_SOURCE_DOCUMENT,
                filename, filename);

        assertThat(downloadedXliffContent).isNotEmpty();
        assertThat(downloadedXliffContent).isXmlEqualToContentOf(testFile);
    }

    @Test
    @RunAsClient
    public void downloadXliff11Translation() throws Exception {
        String filename = "test-xliff.xlf";

        uploadSourceFile(filename, DOCTYPE_XLIFF);

        uploadTranslationFile("test-xliff-es.xlf", "test-xliff.xlf",
                DOCTYPE_XLIFF, "es");

        String downloadedXliffContent = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                filename, true, filename);

        assertThat(downloadedXliffContent).isNotEmpty();

        File translatedFile = getTestFile("test-xliff-es-approvedOnly.xlf");
        assertThat(downloadedXliffContent).isXmlEqualToContentOf(translatedFile);
    }

    @Test
    @RunAsClient
    public void downloadXliff11TranslationApprovedOnly() throws Exception {
        String filename = "test-xliff.xlf";

        uploadSourceFile(filename, DOCTYPE_XLIFF);

        uploadTranslationFile("test-xliff-es.xlf", "test-xliff.xlf",
                DOCTYPE_XLIFF, "es");

        String downloadedXliffContent = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                filename, true, filename);

        assertThat(downloadedXliffContent).isNotEmpty();

        File translatedFile = getTestFile("test-xliff-es-approved.xlf");
        assertThat(downloadedXliffContent).isXmlEqualToContentOf(translatedFile);
    }

    @Test
    @RunAsClient
    public void downloadTextTranslation() throws Exception {
        String filename = "test-text.txt";

        uploadSourceFile(filename, DOCTYPE_PLAIN_TEXT);

        String offlineEmptyPO = downloadAsOfflinePO(filename);

        assertThat(offlineEmptyPO).contains(
                "msgctxt \"764569e58f53ea8b6404f6fa7fc0247f\"\n" +
                        "msgid \"Hello world.\"\n" +
                        "msgstr \"\"\n" +
                        "\n" +
                        "msgctxt \"0101977b4093f837ff6276a762fba7be\"\n" +
                        "msgid \"Goodbye.\"\n" +
                        "msgstr \"\"");

        // TODO sort out problem with uploading offlinepo (source hashes don't match)
//        uploadTranslationFile("test-text-es.txt.po", filename, ".po", "es");
//
//        String downloadedContent = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
//                filename, false, filename);
//
//        assertThat(downloadedContent).isNotEmpty();
//
//        File translatedFile = getTestFile("test-text-es.txt");
//        assertThat(downloadedContent).isEqualTo(contentOf(translatedFile, UTF_8));
    }

    @Test
    @RunAsClient
    public void downloadGettextWithNoTranslations() throws Exception {
        uploadSourceFile("test-gettext.pot", DOCTYPE_GETTEXT);

        String downloadedPoContents = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                "test-gettext.pot", false, "test-gettext.po");

        assertThat(downloadedPoContents).isNotEmpty();
        assertPoFile(downloadedPoContents);
        assertPoFileContainsTranslations(downloadedPoContents,
                BLANK_TRANSLATIONS);
    }

    @Test
    @RunAsClient
    public void downloadGettextWhichIsTranslatedIncludingTranslatedAndApproved() throws Exception {
        uploadSourceFile("test-gettext.pot", DOCTYPE_GETTEXT);

        uploadTranslationFile("test-gettext-translated.po", "test-gettext.pot",
                DOCTYPE_GETTEXT, "es");

        String downloadedPoContents = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                "test-gettext.pot", false, "test-gettext.po");

        assertThat(downloadedPoContents).isNotEmpty();
        assertPoFile(downloadedPoContents);
        assertPoFileContainsTranslations(downloadedPoContents,
                FIRST_TRANSLATIONS);
    }

    @Test
    @RunAsClient
    public void downloadGettextWhichIsTranslatedIncludingApprovedOnly() throws Exception {
        uploadSourceFile("test-gettext.pot", DOCTYPE_GETTEXT);

        uploadTranslationFile("test-gettext-translated.po", "test-gettext.pot",
                DOCTYPE_GETTEXT, "es");

        String downloadedPoContents = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                "test-gettext.pot", true, "test-gettext.po");

        assertThat(downloadedPoContents).isNotEmpty();
        assertPoFile(downloadedPoContents);

        assertPoFileContainsTranslations(downloadedPoContents,
                BLANK_TRANSLATIONS);
    }

    @Test
    @RunAsClient
    public void downloadGettextWhichIsMostlyApprovedIncludingTranslatedAndApproved() throws Exception {
        uploadSourceFile("test-gettext.pot", DOCTYPE_GETTEXT);

        uploadTranslationFile("test-gettext-translated.po", "test-gettext.pot",
                DOCTYPE_GETTEXT, "es");
        // trigger MockTranslationMergeApproved to approve two of the translations:
        uploadTranslationFile("test-gettext-translated-updated.po", "test-gettext.pot",
                DOCTYPE_GETTEXT, "es");

        String downloadedPoContents = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                "test-gettext.pot", false, "test-gettext.po");

        assertThat(downloadedPoContents).isNotEmpty();
        assertPoFile(downloadedPoContents);
        assertPoFileContainsTranslations(downloadedPoContents,
                SECOND_TRANSLATIONS);
    }

    @Test
    @RunAsClient
    public void downloadGettextWhichIsMostlyApprovedIncludingApprovedOnly() throws Exception {
        uploadSourceFile("test-gettext.pot", DOCTYPE_GETTEXT);

        uploadTranslationFile("test-gettext-translated.po", "test-gettext.pot",
                DOCTYPE_GETTEXT, "es");
        // trigger MockTranslationMergeApproved to approve two of the translations:
        uploadTranslationFile("test-gettext-translated-updated.po", "test-gettext.pot",
                DOCTYPE_GETTEXT, "es");

        String downloadedPoContents = downloadTranslationFile("es", FILETYPE_TRANSLATED_APPROVED,
                "test-gettext.pot", true, "test-gettext.po");

        assertThat(downloadedPoContents).isNotEmpty();
        assertPoFile(downloadedPoContents);
        assertPoFileContainsTranslations(downloadedPoContents,
                SECOND_TRANSLATIONS_EXCLUDING_UNCHANGED);
    }

    private String downloadSourceFile(String fileType, String docId, String expectedFileName) {
        Response response = getFileResource()
                .downloadSourceFile("file-project", "1.0", fileType, docId);

        assertThat(response.getStatus()).isEqualTo(200);
        assertHeaderValue(response, "Content-Disposition",
                "attachment; filename=\"" + expectedFileName + "\"");

        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_OCTET_STREAM);

        return getInputStreamAsStringFromResponse(response);
    }

    private String downloadAsOfflinePO(String filename) {
        // uses data from TextFlowTestData.dbunit.xml
        final Response response = getFileResource()
                .downloadTranslationFile("file-project", "1.0", "es", FILETYPE_OFFLINE_PO, filename, false);

        assertThat(response.getStatus()).isEqualTo(200);
        assertHeaderValue(response, "Content-Disposition",
                "attachment; filename=\"" + filename + ".po\"");

        String entityString = response.readEntity(String.class);

        assertPoFileCorrect(entityString);
        return entityString;
    }

    private String downloadTranslationFile(String locale, String fileType, String docId, boolean approvedOnly, String expectedFileName) {
        Response response = getFileResource()
                .downloadTranslationFile("file-project", "1.0", locale, fileType, docId, approvedOnly);

        assertThat(response.getStatus()).isEqualTo(200);
        assertHeaderValue(response, "Content-Disposition",
                "attachment; filename=\"" + expectedFileName + "\"");

        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_OCTET_STREAM);

        return getInputStreamAsStringFromResponse(response);
    }

    private void uploadSourceFile(String fileName, String docType) throws IOException {
        DocumentFileUploadForm fileUploadForm = getDocumentFileUploadForm(fileName, docType);
        try {
            Response response = getFileResource()
                    .uploadSourceFile("file-project", "1.0", fileName,
                            fileUploadForm);
            assertThat(response.getStatus()).isEqualTo(200);

            ChunkUploadResponse chunkUploadResponseFromResponse = getChunkUploadResponseFromResponse(response);

            assertThat(chunkUploadResponseFromResponse.getErrorMessage()).isNullOrEmpty();
        } finally {
            fileUploadForm.getFileStream().close();
        }
    }

    private void uploadTranslationFile(String fileName, String docId, String docType, String locale) throws FileNotFoundException {
        uploadTranslationFile(fileName, docId, docType, locale, "import");
    }

    private void uploadTranslationFile(String fileName, String docId, String docType, String locale, String merge) throws FileNotFoundException {
        DocumentFileUploadForm fileUploadForm = getDocumentFileUploadForm(fileName, docType);

        Response response = getFileResource().uploadTranslationFile("file-project", "1.0", locale, docId, merge, fileUploadForm);

        assertThat(response.getStatus())
                .as(describedBy(() -> getChunkUploadResponseFromResponse(response).getErrorMessage()))
                .isEqualTo(200);

        ChunkUploadResponse chunkUploadResponseFromResponse = getChunkUploadResponseFromResponse(response);

        assertThat(chunkUploadResponseFromResponse.getErrorMessage()).isNullOrEmpty();
        assertThat(chunkUploadResponseFromResponse.getSuccessMessage()).isEqualTo("Translations uploaded successfully");
    }

    private File getTestFile(String name) {
        return new File("src/test/resources/org/zanata/" + name);
    }

    private DocumentFileUploadForm getDocumentFileUploadForm(String fileName, String docType) throws FileNotFoundException {
        // TODO this doesn't close the stream if there's an exception
        File file = getTestFile(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);

        String hash = calculateFileHash(file);

        return generateUniqueUploadForm(docType, hash, file.length(), fileInputStream);
    }

    private String getInputStreamAsStringFromResponse(Response response) {
        InputStream inputStream = response.readEntity(InputStream.class);

        String theString = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));

        return theString;
    }

    private ChunkUploadResponse getChunkUploadResponseFromResponse(Response response) {
        response.bufferEntity();

        return response.readEntity(ChunkUploadResponse.class);
    }

    private String calculateFileHash(File srcFile) {
        try {
//            return Files.asByteSource(srcFile).hash(Hashing.md5()).toString();
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileStream = new FileInputStream(srcFile);
            try {
                fileStream = new DigestInputStream(fileStream, md);
                byte[] buffer = new byte[256];
                //noinspection StatementWithEmptyBody
                while (fileStream.read(buffer) > 0) {
                    // just keep digesting the input
                }
            } finally {
                fileStream.close();
            }
            //noinspection UnnecessaryLocalVariable
            String md5hash = new String(Hex.encodeHex(md.digest()));
            return md5hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DocumentFileUploadForm generateUniqueUploadForm(String docType, String md5hash, long streamSize,
                                                            InputStream fileStream) {
        DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
        uploadForm.setFirst(true);
        uploadForm.setLast(true);
        uploadForm.setFileType(docType);
        uploadForm.setHash(md5hash);
        uploadForm.setSize(streamSize);
        uploadForm.setFileStream(fileStream);
        return uploadForm;
    }

    private static void assertPoFile(String poFileContents) {
        new MessageStreamParser(new StringReader(poFileContents));
    }

    private static void assertPoFileCorrect(String poFileContents) {
        MessageStreamParser messageParser =
                new MessageStreamParser(new StringReader(poFileContents));

        while (messageParser.hasNext()) {
            Message message = messageParser.next();

            if (message.isHeader()) {
                // assert that expected headers are present (with values if
                // needed)
                assertThat(message.getMsgstr())
                        .contains("MIME-Version:", "Content-Type:",
                                "Content-Transfer-Encoding:",
                                "Last-Translator:", "PO-Revision-Date:",
                                "X-Generator: Zanata", "Plural-Forms:");
            }
        }
    }

    /**
     * Validates that the po files contains the appropriate translations.
     *
     * @param poFileContents
     *            The contents of the PO file as a string
     * @param translations
     *            The translations in (msgid, msgstr) pairs. E.g. msgid1,
     *            trans1, msgid2, trans2, ... etc.
     */
    private static void assertPoFileContainsTranslations(String poFileContents,
                                                         String... translations) {
        if (translations.length % 2 != 0) {
            throw new AssertionError(
                    "Translation parameters should be given in pairs.");
        }

        MessageStreamParser messageParser =
                new MessageStreamParser(new StringReader(poFileContents));

        List<String> found = new ArrayList<String>(translations.length);

        // Assert that all the given translations are present
        while (messageParser.hasNext()) {
            Message message = messageParser.next();

            if (!message.isHeader()) {
                // Find the message id in the array given to check
                int foundAt = 0;
                while (foundAt < translations.length) {
                    // Message Id found
                    if (message.getMsgid().equals(translations[foundAt])) {
                        found.add(message.getMsgid());
                        // Translation does not match
                        if (!message.getMsgstr().equals(
                                translations[foundAt + 1])) {
                            throw new AssertionError(
                                    "Expected translation for msgid '"
                                            + message.getMsgid() + "' "
                                            + "is: '"
                                            + translations[foundAt + 1] + "'. "
                                            + "Instead got '"
                                            + message.getMsgstr() + "'");
                        }
                    }

                    foundAt += 2;
                }
            }
        }

        // If there are some messages not found
        if (found.size() < translations.length / 2) {
            StringBuilder assertionError =
                    new StringBuilder(
                            "The following msgids were expected yet not found: ");
            for (int i = 0; i < translations.length; i += 2) {
                if (!found.contains(translations[i])) {
                    assertionError.append(translations[i] + " | ");
                }
            }

            throw new AssertionError(assertionError.toString());
        }
    }

    public FileClient getFileResource() {
        if (fileResource == null) {
            fileResource = new FileClient();
        }
        return fileResource;
    }

    public class FileClient implements FileResource {
        @Override
        public Response acceptedFileTypes() {
            return new GetResourceRequest(
                    getRestEndpointUrl(FileResource.ACCEPTED_TYPES_RESOURCE), getAuthorizedEnvironment()) {
            }.runWithResult();
        }

        @Override
        public Response acceptedFileTypeList() {
            return new GetResourceRequest(
                    getRestEndpointUrl(FileResource.ACCEPTED_TYPE_LIST_RESOURCE), getAuthorizedEnvironment()) {
            }.runWithResult();
        }

        @Override
        public Response fileTypeInfoList() {
            return new GetResourceRequest(
                    getRestEndpointUrl(FileResource.FILE_TYPE_INFO_RESOURCE), getAuthorizedEnvironment()) {
            }.runWithResult();
        }

        @Override
        public Response uploadSourceFile(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm) {
            return new UploadResourceRequest(
                    getRestEndpointUrl("/file/source/" + projectSlug + "/" + iterationSlug + "/"),
                    "POST", getAuthorizedEnvironment(), uploadForm) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("docId", docId)
                            .request(MediaType.APPLICATION_XML_TYPE);
                }
            }.runWithResult();
        }

        @Override
        public Response uploadTranslationFile(String projectSlug, String iterationSlug, String localeId, String docId, String merge, DocumentFileUploadForm uploadForm) {
            return new UploadResourceRequest(
                    getRestEndpointUrl("/file/translation/" + projectSlug + "/" + iterationSlug + "/" + localeId),
                    "POST", getAuthorizedEnvironment(), uploadForm) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("docId", docId)
                            .queryParam("merge", merge)
                            .request(MediaType.APPLICATION_XML_TYPE);
                }
            }.runWithResult();
        }

        @Override
        public Response downloadSourceFile(String projectSlug, String iterationSlug, String fileType, String docId) {
            return new DownloadResourceRequest(
                    getRestEndpointUrl("/file/source/" + projectSlug + "/" + iterationSlug + "/" + fileType),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget.queryParam("docId", docId).request(MediaType.APPLICATION_OCTET_STREAM_TYPE);
                }
            }.runWithResult();
        }

        @Override
        public Response downloadTranslationFile(String projectSlug, String iterationSlug, String locale, String fileType, String docId, boolean approvedOnly) {
            return new DownloadResourceRequest(
                    getRestEndpointUrl("/file/translation/" + projectSlug + "/" + iterationSlug + "/" + locale + "/" + fileType),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("docId", docId)
                            .queryParam("approvedOnly", approvedOnly)
                            .request(MediaType.APPLICATION_OCTET_STREAM_TYPE);
                }
            }.runWithResult();
        }

        @Override
        public Response download(String downloadId) {
            return new GetResourceRequest(
                    getRestEndpointUrl("/download/" + downloadId),
                     getAuthorizedEnvironment()) {
            }.runWithResult();
        }
    }

    private static abstract class DownloadResourceRequest extends ResourceRequest {
        public DownloadResourceRequest(String resourceUrl, String method) {
            super(resourceUrl, method);
        }

        protected DownloadResourceRequest(String resourceUrl, String method,
                                        ResourceRequestEnvironment environment) {
            super(resourceUrl, method, environment);
        }

        @Override
        public Response invokeWithResponse(
                Invocation.Builder builder) {
            return builder.get();
        }

        @Override
        protected void onResponse(Response response) {
            // No response processing needed when downloading a file
        }
    }

    private static abstract class UploadResourceRequest extends ResourceRequest {
        private final DocumentFileUploadForm uploadForm;

        public UploadResourceRequest(String resourceUrl, String method, DocumentFileUploadForm uploadForm) {
            super(resourceUrl, method);

            this.uploadForm = uploadForm;
        }

        protected UploadResourceRequest(String resourceUrl, String method,
                                        ResourceRequestEnvironment environment, DocumentFileUploadForm uploadForm) {
            super(resourceUrl, method, environment);

            this.uploadForm = uploadForm;
        }

        @Override
        public Response invokeWithResponse(
                Invocation.Builder builder) {
            Entity<DocumentFileUploadForm> entity = Entity.entity(uploadForm, MediaType.MULTIPART_FORM_DATA_TYPE, multipartFormAnnotations);

            return builder.buildPost(entity).invoke();
        }

        @Override
        protected void onResponse(Response response) {
            // No response processing needed when uploading a file
        }
    }

    private static abstract class GetResourceRequest extends ResourceRequest {
        public GetResourceRequest(String resourceUrl) {
            super(resourceUrl, "GET");
        }

        protected GetResourceRequest(String resourceUrl, ResourceRequestEnvironment environment) {
            super(resourceUrl, "GET", environment);
        }

        @Override
        protected Invocation.Builder prepareRequest(
                ResteasyWebTarget webTarget) {
            return webTarget
                    .request().header(HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_XML_TYPE);
        }

        @Override
        protected void onResponse(Response response) {
            // No response processing needed when getting the resource
        }
    }

    @SuppressWarnings("all")
    private static class MultipartFormLiteral implements MultipartForm {

        @Override
        public java.lang.Class<? extends Annotation> annotationType() {
            return MultipartForm.class;
        }
    }
}
