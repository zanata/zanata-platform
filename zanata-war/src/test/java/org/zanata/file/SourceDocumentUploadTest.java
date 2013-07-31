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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.zanata.rest.service.FileServiceTest.defaultUpload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.FileServiceTest.MockConfig;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;

// FIXME damason remove dependence on FileServiceTest

@Test(groups = { "unit-tests" })
public class SourceDocumentUploadTest
{
   private static final GlobalDocumentId DEFAULT_ID = new GlobalDocumentId("myproject", "myversion", "mydoc");
   private static final DocumentFileUploadForm ANY_UPLOAD_FORM = new DocumentFileUploadForm();

   SeamAutowire seam = SeamAutowire.instance();

   @Mock private ZanataIdentity identity;
   @Mock private ProjectIterationDAO projectIterationDAO;
   @Mock private TranslationFileService translationFileService;
   @Mock private DocumentDAO documentDAO;
   @Mock private DocumentService documentService;

   @Mock private FilePersistService filePersistService;

   @Mock private HProject project;
   @Mock private HProjectIteration projectIteration;

   @Captor private ArgumentCaptor<Optional<String>> paramCaptor;
   @Captor private ArgumentCaptor<HRawDocument> persistedRawDocument;

   private SourceDocumentUpload sourceUpload;
   private Response response;

   @BeforeMethod
   public void beforeTest()
   {
      MockitoAnnotations.initMocks(this);
      seam.reset();
      seam.ignoreNonResolvable()
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
   }

   private void mockLoggedIn()
   {
      when(identity.isLoggedIn()).thenReturn(true);
   }

   private void mockNotLoggedIn()
   {
      when(identity.isLoggedIn()).thenReturn(false);
   }

   private void mockRequiredServices(MockConfig conf) throws IOException
   {
      mockLoggedIn();
      mockProjectAndVersionStatus(conf);
      when(identity.hasPermission("import-template", projectIteration)).thenReturn(conf.hasImportTemplatePermission);
      when(translationFileService.hasAdapterFor(DocumentType.PLAIN_TEXT)).thenReturn(conf.plaintextAdapterAvailable);
      ZanataCredentials creds = new ZanataCredentials();
      creds.setUsername("johnsmith");
      when(identity.getCredentials()).thenReturn(creds);
      File someFile = File.createTempFile("tests", "something");
      when(translationFileService.persistToTempFile(Matchers.<InputStream> any())).thenReturn(someFile);
      when(documentDAO.getAdapterParams(conf.projectSlug, conf.versionSlug, conf.docId))
            .thenReturn(Optional.fromNullable(conf.storedParams));
      when(documentDAO.addRawDocument(Matchers.<HDocument> any(), persistedRawDocument.capture()))
            .thenReturn(new HRawDocument());
      when(documentDAO.getByProjectIterationAndDocId(conf.projectSlug, conf.versionSlug,
            conf.docId)).thenReturn(conf.existingDocument);
      //            when(documentDAO.getLobHelper()).thenReturn(lobHelper);
      Resource document = new Resource();
      when(translationFileService.parseUpdatedAdapterDocumentFile(
            Matchers.<URI> any(), eq(conf.docId), eq(conf.fileType), paramCaptor.capture()))
            .thenReturn(document);
      when(documentService.saveDocument(eq(conf.projectSlug), eq(conf.versionSlug), Matchers.<Resource> any(),
            Matchers.anySet(), Matchers.anyBoolean()))
            .thenReturn(new HDocument());
   }

   private void mockProjectAndVersionStatus(MockConfig conf)
   {
      when(projectIterationDAO.getBySlug(conf.projectSlug, conf.versionSlug)).thenReturn(projectIteration);
      when(projectIteration.getProject()).thenReturn(project);
      when(project.getStatus()).thenReturn(conf.projectStatus);
      when(projectIteration.getStatus()).thenReturn(conf.versionStatus);
   }

   public void respondUnauthorizedIfNotLoggedIn()
   {
      mockNotLoggedIn();
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, ANY_UPLOAD_FORM);
      assertResponseHasStatus(UNAUTHORIZED);
      assertResponseHasErrorMessage("Valid combination of username and api-key for this server " +
            "were not included in the request.");
      assertUploadTerminated();
   }

   public void usefulMessageIfNoFileContent()
   {
      MockConfig conf = defaultUpload().fileStream(null).build();
      mockLoggedIn();
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(PRECONDITION_FAILED);
      assertResponseHasErrorMessage("Required form parameter 'file' containing file content was " +
            "not found.");
      assertUploadTerminated();
   }

   public void usefulMessageIfNoFileType()
   {
      MockConfig conf = defaultUpload().fileType(null).build();
      mockLoggedIn();
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(PRECONDITION_FAILED);
      assertResponseHasErrorMessage("Required form parameter 'type' was not found.");
      assertUploadTerminated();
   }

   public void usefulMessageIfNoContentHash()
   {
      MockConfig conf = defaultUpload().hash(null).build();
      mockLoggedIn();
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(PRECONDITION_FAILED);
      assertResponseHasErrorMessage("Required form parameter 'hash' was not found.");
      assertUploadTerminated();
   }

   public void usefulMessageIfProjectIsReadOnly() throws IOException
   {
      testUsefulMessageForInactiveProject(EntityStatus.READONLY);
   }

   public void usefulMessageIfProjectIsObsolete() throws IOException
   {
      testUsefulMessageForInactiveProject(EntityStatus.OBSOLETE);
   }

   private void testUsefulMessageForInactiveProject(EntityStatus nonActiveStatus) throws IOException
   {
      MockConfig conf = defaultUpload().projectStatus(nonActiveStatus).build();
      mockRequiredServices(conf);

      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(FORBIDDEN);
      assertResponseHasErrorMessage("The project \"myproject\" is not active. Document upload is " +
            "not allowed.");
      assertUploadTerminated();
   }

   public void usefulMessageIfVersionIsReadOnly() throws IOException
   {
      testUsefulMessageForInactiveVersion(EntityStatus.READONLY);
   }

   public void usefulMessageIfVersionIsObsolete() throws IOException
   {
      testUsefulMessageForInactiveVersion(EntityStatus.OBSOLETE);
   }

   private void testUsefulMessageForInactiveVersion(EntityStatus nonActiveStatus) throws IOException
   {
      MockConfig conf = defaultUpload().versionStatus(nonActiveStatus).build();
      mockRequiredServices(conf);
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(FORBIDDEN);
      assertResponseHasErrorMessage("The project-version \"myproject:myversion\" is not active. " +
            "Document upload is not allowed.");
      assertUploadTerminated();
   }

   public void usefulMessageWhenSourceUploadNotAllowed() throws IOException
   {
      MockConfig conf = defaultUpload().hasImportTemplatePermission(false).build();
      mockRequiredServices(conf);
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(FORBIDDEN);
      assertResponseHasErrorMessage("You do not have permission to upload source documents to " +
            "project-version \"myproject:myversion\".");
   }

   public void usefulMessageWhenFileTypeInvalid() throws IOException
   {
      // Note: could pass non-valid type rather than hacking it at the back
      MockConfig conf = defaultUpload().plaintextAdapterAvailable(false).build();
      mockRequiredServices(conf);
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(BAD_REQUEST);
      assertResponseHasErrorMessage("The type \"txt\" specified in form parameter 'type' is not " +
            "valid for a source file on this server.");
   }

   public void usefulMessageWhenHashInvalid() throws IOException
   {
      MockConfig conf = defaultUpload().hash("incorrect hash").build();
      mockRequiredServices(conf);
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(CONFLICT);
      assertResponseHasErrorMessage("MD5 hash \"incorrect hash\" sent with request does not match" +
            " server-generated hash. Aborted upload operation.");
   }

   public void canUploadNewDocument() throws IOException
   {
      MockConfig conf = defaultUpload().build();
      mockRequiredServices(conf);
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(CREATED);
      assertThat(responseEntity().getAcceptedChunks(), is(1));
      assertThat(responseEntity().isExpectingMore(), is(false));
      assertThat(responseEntity().getSuccessMessage(),
            is("Upload of new source document successful."));
      assertThat(responseEntity().getErrorMessage(), is(nullValue()));
   }

   public void canUploadExistingDocument() throws IOException
   {
      MockConfig conf = defaultUpload().existingDocument(new HDocument()).build();
      mockRequiredServices(conf);
      response = sourceUpload.tryUploadSourceFile(DEFAULT_ID, conf.uploadForm);
      assertResponseHasStatus(OK);
      assertThat(responseEntity().getAcceptedChunks(), is(1));
      assertThat(responseEntity().isExpectingMore(), is(false));
      assertThat(responseEntity().getSuccessMessage(),
            is("Upload of new version of source document successful."));
      assertThat(responseEntity().getErrorMessage(), is(nullValue()));
   }

   private void assertResponseHasStatus(Status errorStatus)
   {
      assertThat(fromStatusCode(response.getStatus()), is(errorStatus));
   }

   private void assertResponseHasErrorMessage(String errorMessage)
   {
      assertThat(responseEntity().getErrorMessage(), is(errorMessage));
   }

   private void assertUploadTerminated()
   {
      assertThat(responseEntity().getAcceptedChunks(), is(0));
      assertThat(responseEntity().isExpectingMore(), is(false));
   }

   private ChunkUploadResponse responseEntity()
   {
      return (ChunkUploadResponse) response.getEntity();
   }
}
