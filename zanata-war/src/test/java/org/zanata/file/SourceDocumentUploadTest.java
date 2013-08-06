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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.DocumentType;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.HDocument;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.security.ZanataCredentials;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;

@Test(groups = { "unit-tests" })
public class SourceDocumentUploadTest extends DocumentUploadTest
{
   @Mock DocumentUploadUtil documentUploadUtil;

   @Mock private TranslationFileService translationFileService;
   @Mock private DocumentService documentService;
   @Mock private FilePersistService filePersistService;

   @Captor private ArgumentCaptor<Optional<String>> paramCaptor;
   @Captor private ArgumentCaptor<HRawDocument> persistedRawDocument;

   private SourceDocumentUpload sourceUpload;

   @BeforeMethod
   public void beforeTest()
   {
      MockitoAnnotations.initMocks(this);
      seam.reset();
      seam.ignoreNonResolvable()
            .use("documentUploadUtil", documentUploadUtil)
            .use("identity", identity)
            .use("projectIterationDAO", projectIterationDAO)
            .use("translationFileServiceImpl", translationFileService)
            .use("documentServiceImpl", documentService)
            .use("documentDAO", documentDAO)
            .use("filePersistService", filePersistService)
            .allowCycles();

      sourceUpload = seam.autowire(SourceDocumentUpload.class);
   }

   @AfterMethod
   public void clearResponse()
   {
      response = null;
      conf = null;
   }

   private void mockRequiredServices() throws IOException
   {
      mockProjectAndVersionStatus();
      mockHasUploadPermission();
      mockHasPlainTextAdapter();

      ZanataCredentials creds = new ZanataCredentials();
      creds.setUsername("johnsmith");
      when(identity.getCredentials()).thenReturn(creds);

      File someFile = File.createTempFile("tests", "something");
      when(documentUploadUtil.persistTempFileFromUpload(conf.uploadForm)).thenReturn(someFile);

      when(documentDAO.getAdapterParams(conf.projectSlug, conf.versionSlug, conf.docId))
            .thenReturn(Optional.fromNullable(conf.storedParams));
      when(documentDAO.addRawDocument(Matchers.<HDocument> any(), persistedRawDocument.capture()))
            .thenReturn(new HRawDocument());
      when(documentDAO.getByProjectIterationAndDocId(conf.projectSlug, conf.versionSlug,
            conf.docId)).thenReturn(conf.existingDocument);
      Resource document = new Resource();
      when(translationFileService.parseUpdatedAdapterDocumentFile(
            Matchers.<URI> any(), eq(conf.docId), eq(conf.fileType), paramCaptor.capture()))
            .thenReturn(document);
      when(documentService.saveDocument(eq(conf.projectSlug), eq(conf.versionSlug), Matchers.<Resource> any(),
            Matchers.anySetOf(String.class), Matchers.anyBoolean()))
            .thenReturn(new HDocument());
   }

   private void mockHasPlainTextAdapter()
   {
      when(translationFileService.hasAdapterFor(DocumentType.PLAIN_TEXT)).thenReturn(conf.plaintextAdapterAvailable);
   }

   private void mockHasUploadPermission()
   {
      when(identity.hasPermission("import-template", projectIteration)).thenReturn(conf.hasImportTemplatePermission);
   }

   public void checksValidityAndFailsIfNotValid()
   {
      conf = defaultUpload().build();
      doThrow(new ChunkUploadException(NOT_ACCEPTABLE, "Test message"))
            .when(documentUploadUtil).failIfUploadNotValid(conf.id, conf.uploadForm);
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(NOT_ACCEPTABLE);
      assertResponseHasErrorMessage("Test message");
      assertUploadTerminated();
   }

   public void usefulMessageWhenSourceUploadNotAllowed() throws IOException
   {
      conf = defaultUpload().hasImportTemplatePermission(false).build();
      mockRequiredServices();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(FORBIDDEN);
      assertResponseHasErrorMessage("You do not have permission to upload source documents to " +
            "project-version \"myproject:myversion\".");
   }

   public void usefulMessageWhenFileTypeInvalid() throws IOException
   {
      // Note: could pass non-valid type rather than hacking it at the back
      conf = defaultUpload().plaintextAdapterAvailable(false).build();
      mockRequiredServices();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(BAD_REQUEST);
      assertResponseHasErrorMessage("The type \"txt\" specified in form parameter 'type' is not " +
            "valid for a source file on this server.");
   }

   public void failsIfPersistFails() throws IOException
   {
      conf = defaultUpload().build();

      mockProjectAndVersionStatus();
      mockHasUploadPermission();
      mockHasPlainTextAdapter();

      doThrow(new ChunkUploadException(NOT_ACCEPTABLE, "Test message"))
            .when(documentUploadUtil).persistTempFileFromUpload(conf.uploadForm);

      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasErrorMessage("Test message");
      assertResponseHasStatus(NOT_ACCEPTABLE);
   }

   public void canUploadNewDocument() throws IOException
   {
      conf = defaultUpload().build();
      mockRequiredServices();
      when(documentUploadUtil.isNewDocument(conf.id)).thenReturn(true);

      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertThat(responseEntity().getSuccessMessage(),
            is("Upload of new source document successful."));
      assertResponseHasStatus(CREATED);
      assertThat(responseEntity().getAcceptedChunks(), is(1));
      assertThat(responseEntity().isExpectingMore(), is(false));
      assertThat(responseEntity().getErrorMessage(), is(nullValue()));
   }

   public void canUploadExistingDocument() throws IOException
   {
      conf = defaultUpload().existingDocument(new HDocument()).build();
      mockRequiredServices();
      when(documentUploadUtil.isNewDocument(conf.id)).thenReturn(false);

      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(OK);
      assertThat(responseEntity().getAcceptedChunks(), is(1));
      assertThat(responseEntity().isExpectingMore(), is(false));
      assertThat(responseEntity().getSuccessMessage(),
            is("Upload of new version of source document successful."));
      assertThat(responseEntity().getErrorMessage(), is(nullValue()));
   }

   public void usesGivenParameters() throws IOException
   {
      conf = defaultUpload().build();
      mockRequiredServices();
      sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertThat(paramCaptor.getValue().get(), is(conf.params));
   }

   public void fallsBackOnStoredParameters() throws IOException
   {
      conf = defaultUpload().params(null).build();
      mockRequiredServices();
      sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertThat(paramCaptor.getValue().get(), is(conf.storedParams));
   }

   public void uploadParametersAreStored() throws IOException
   {
      conf = defaultUpload().build();
      mockRequiredServices();
      sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertThat(persistedRawDocument.getValue().getAdapterParameters(), is(conf.params));
   }

   public void storedParametersNotOverwrittenWithEmpty() throws IOException
   {
      conf = defaultUpload().params("").build();
      mockRequiredServices();
      sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertThat(persistedRawDocument.getValue().getAdapterParameters(), is(conf.storedParams));
   }
}
