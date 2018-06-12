/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.file;

import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.zanata.adapter.FileFormatAdapter.ParserOptions;
import org.zanata.common.DocumentType;
import org.zanata.exception.DocumentUploadException;
import org.zanata.model.HDocument;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.VirusScanner;
import org.zanata.security.ZanataCredentials;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@RunWith(CdiUnitRunner.class)
public class SourceDocumentUploadTest extends DocumentUploadTest {
    @Produces @Mock
    DocumentUploadUtil documentUploadUtil;

    @Produces @Mock
    private TranslationFileService translationFileService;
    @Produces @Mock
    private DocumentService documentService;
    @Produces @Mock
    private FilePersistService filePersistService;
    @Produces @Mock
    WindowContext windowContext;
    @Produces @Mock
    UrlUtil urlUtil;
    @Produces @Mock
    VirusScanner virusScanner;

    @Produces @Mock Session session;
    @Produces @SessionId String sessionId = "";
    @Produces @ServerPath String serverPath = "";
    @Produces @ContextPath String contextPath = "";

    @Captor
    private ArgumentCaptor<ParserOptions> parserOptions;
    @Captor
    private ArgumentCaptor<HRawDocument> persistedRawDocument;

    @Inject
    private SourceDocumentUpload sourceUpload;

    @After
    public void clearResponse() {
        response = null;
        conf = null;
    }

    @SuppressWarnings("unchecked")
    private void mockRequiredServices() throws IOException {
        mockProjectAndVersionStatus();
        mockHasUploadPermission();
        mockHasPlainTextAdapter();

        ZanataCredentials creds = new ZanataCredentials();
        creds.setUsername("johnsmith");
        when(identity.getCredentials()).thenReturn(creds);

        File someFile = File.createTempFile("tests", "something");
        when(documentUploadUtil.persistTempFileFromUpload(conf.uploadForm))
                .thenReturn(someFile);
        doNothing().when(virusScanner).scan(someFile, "myproject:myversion:mydoc");
        when(
                documentDAO.getAdapterParams(conf.projectSlug,
                        conf.versionSlug, conf.docId)).thenReturn(
                conf.storedParams);
//        conf.storedParams);
        when(
                documentDAO.addRawDocument(ArgumentMatchers.any(HDocument.class),
                        persistedRawDocument.capture())).thenReturn(
                new HRawDocument());
        when(
                documentDAO.getByProjectIterationAndDocId(conf.projectSlug,
                        conf.versionSlug, conf.docId)).thenReturn(
                conf.existingDocument);
        Resource document = new Resource();
        when(
                translationFileService.parseUpdatedAdapterDocumentFile(
                        eq(conf.docId),
                        eq(conf.fileType), parserOptions.capture(),
                        ArgumentMatchers.any(Optional.class))).thenReturn(
            document);
        when(
                documentService.saveDocument(eq(conf.projectSlug),
                        eq(conf.versionSlug), ArgumentMatchers.any(Resource.class),
                        ArgumentMatchers.anySet(), ArgumentMatchers.anyBoolean()))
                .thenReturn(new HDocument());
    }

    private void mockHasPlainTextAdapter() {
        when(translationFileService.hasAdapterFor(DocumentType.PLAIN_TEXT))
                .thenReturn(conf.plaintextAdapterAvailable);
    }

    private void mockHasUploadPermission() {
        when(identity.hasPermissionWithAnyTargets("import-template",
                projectIteration))
                .thenReturn(conf.hasImportTemplatePermission);
    }

    @Test
    public void checksValidityAndFailsIfNotValid() {
        conf = defaultUpload().build();
        doThrow(new DocumentUploadException(NOT_ACCEPTABLE, "Test message")).when(
                documentUploadUtil).failIfUploadNotValid(conf.id,
                conf.uploadForm);
        response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertResponseHasStatus(NOT_ACCEPTABLE);
        assertResponseHasErrorMessage("Test message");
        assertUploadTerminated();
    }

    @Test
    public void usefulMessageWhenSourceUploadNotAllowed() throws IOException {
        conf = defaultUpload().hasImportTemplatePermission(false).build();
        mockRequiredServices();
        response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertResponseHasStatus(FORBIDDEN);
        assertResponseHasErrorMessage("You do not have permission to upload source documents to "
                + "project-version \"myproject:myversion\".");
    }

    @Test
    public void usefulMessageWhenFileTypeInvalid() throws IOException {
        // Note: could pass non-valid type rather than hacking it at the back
        conf = defaultUpload().plaintextAdapterAvailable(false).build();
        mockRequiredServices();
        response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertResponseHasStatus(BAD_REQUEST);
        assertResponseHasErrorMessage("The type \"PLAIN_TEXT\" specified in form parameter 'type' is not "
                + "valid for a source file on this server.");
    }

    @Test
    public void failsIfPersistFails() throws IOException {
        conf = defaultUpload().build();

        mockProjectAndVersionStatus();
        mockHasUploadPermission();
        mockHasPlainTextAdapter();

        doThrow(new DocumentUploadException(NOT_ACCEPTABLE, "Test message")).when(
                documentUploadUtil).persistTempFileFromUpload(conf.uploadForm);

        response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertResponseHasErrorMessage("Test message");
        assertResponseHasStatus(NOT_ACCEPTABLE);
    }

    @Test
    @InRequestScope
    public void canUploadNewDocument() throws IOException {
        conf = defaultUpload().build();
        mockRequiredServices();
        when(documentUploadUtil.isNewDocument(conf.id)).thenReturn(true);

        response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertThat(responseEntity().getSuccessMessage()).isEqualTo(
                "Upload of new source document successful.");
        assertResponseHasStatus(CREATED);
        assertThat(responseEntity().getAcceptedChunks()).isEqualTo(1);
        assertThat(responseEntity().isExpectingMore()).isFalse();
        assertThat(responseEntity().getErrorMessage()).isNull();
    }

    @Test
    @InRequestScope
    public void canUploadExistingDocument() throws IOException {
        conf = defaultUpload().existingDocument(new HDocument()).build();
        mockRequiredServices();
        when(documentUploadUtil.isNewDocument(conf.id)).thenReturn(false);

        response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertResponseHasStatus(OK);
        assertThat(responseEntity().getAcceptedChunks()).isEqualTo(1);
        assertThat(responseEntity().isExpectingMore()).isFalse();
        assertThat(responseEntity().getSuccessMessage()).isEqualTo(
                "Upload of new version of source document successful.");
        assertThat(responseEntity().getErrorMessage()).isNull();
    }

    @Test
    @InRequestScope
    public void usesGivenParameters() throws IOException {
        conf = defaultUpload().params("my params").build();
        mockRequiredServices();
        sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertThat(parserOptions.getValue().getParams()).isEqualTo("my params");
    }

    @Test
    @InRequestScope
    public void uploadParametersAreStored() throws IOException {
        conf = defaultUpload().params("my params").build();
        mockRequiredServices();
        sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        // TODO move this assertion into usesGivenParameters
        assertThat(persistedRawDocument.getValue().getAdapterParameters())
                .isEqualTo("my params");
    }

    @Test
    @InRequestScope
    public void fallsBackOnStoredParameters() throws IOException {
        conf = defaultUpload().params(null).build();
        mockRequiredServices();
        sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertThat(parserOptions.getValue().getParams()).isEqualTo(conf.storedParams);
    }

    @Test
    @InRequestScope
    public void storedParametersNotOverwrittenWithEmpty() throws IOException {
        conf = defaultUpload().storedParams("stored params").params("").build();
        mockRequiredServices();
        sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
        assertThat(persistedRawDocument.getValue().getAdapterParameters())
                .isEqualTo("stored params");
    }
}
