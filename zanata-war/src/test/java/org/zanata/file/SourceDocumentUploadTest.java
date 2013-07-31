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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;

@Test(groups = { "unit-tests" })
public class SourceDocumentUploadTest
{
   private static final GlobalDocumentId ANY_ID = new GlobalDocumentId("myproject", "myversion", "mydoc");
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
   private MockConfig conf;

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

   private void mockRequiredServices() throws IOException
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
      Resource document = new Resource();
      when(translationFileService.parseUpdatedAdapterDocumentFile(
            Matchers.<URI> any(), eq(conf.docId), eq(conf.fileType), paramCaptor.capture()))
            .thenReturn(document);
      when(documentService.saveDocument(eq(conf.projectSlug), eq(conf.versionSlug), Matchers.<Resource> any(),
            Matchers.anySetOf(String.class), Matchers.anyBoolean()))
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
      response = sourceUpload.tryUploadSourceFile(ANY_ID, ANY_UPLOAD_FORM);
      assertResponseHasStatus(UNAUTHORIZED);
      assertResponseHasErrorMessage("Valid combination of username and api-key for this server " +
            "were not included in the request.");
      assertUploadTerminated();
   }

   public void usefulMessageIfNoFileContent()
   {
      MockConfig conf = defaultUpload().fileStream(null).build();
      mockLoggedIn();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(PRECONDITION_FAILED);
      assertResponseHasErrorMessage("Required form parameter 'file' containing file content was " +
            "not found.");
      assertUploadTerminated();
   }

   public void usefulMessageIfNoFileType()
   {
      MockConfig conf = defaultUpload().fileType(null).build();
      mockLoggedIn();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(PRECONDITION_FAILED);
      assertResponseHasErrorMessage("Required form parameter 'type' was not found.");
      assertUploadTerminated();
   }

   public void usefulMessageIfNoContentHash()
   {
      MockConfig conf = defaultUpload().hash(null).build();
      mockLoggedIn();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
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
      conf = defaultUpload().projectStatus(nonActiveStatus).build();
      mockRequiredServices();

      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
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
      conf = defaultUpload().versionStatus(nonActiveStatus).build();
      mockRequiredServices();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(FORBIDDEN);
      assertResponseHasErrorMessage("The project-version \"myproject:myversion\" is not active. " +
            "Document upload is not allowed.");
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

   public void usefulMessageWhenHashInvalid() throws IOException
   {
      conf = defaultUpload().hash("incorrect hash").build();
      mockRequiredServices();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(CONFLICT);
      assertResponseHasErrorMessage("MD5 hash \"incorrect hash\" sent with request does not match" +
            " server-generated hash. Aborted upload operation.");
   }

   public void canUploadNewDocument() throws IOException
   {
      conf = defaultUpload().build();
      mockRequiredServices();
      response = sourceUpload.tryUploadSourceFile(conf.id, conf.uploadForm);
      assertResponseHasStatus(CREATED);
      assertThat(responseEntity().getAcceptedChunks(), is(1));
      assertThat(responseEntity().isExpectingMore(), is(false));
      assertThat(responseEntity().getSuccessMessage(),
            is("Upload of new source document successful."));
      assertThat(responseEntity().getErrorMessage(), is(nullValue()));
   }

   public void canUploadExistingDocument() throws IOException
   {
      conf = defaultUpload().existingDocument(new HDocument()).build();
      mockRequiredServices();
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

   /**
    * @return builder with default valid set of upload parameters
    *         for a valid 4-character plain text document that
    *         does not yet exist in the server.
    */
   public static MockConfig.Builder defaultUpload()
   {
      MockConfig.Builder builder = new MockConfig.Builder();
      builder.setSimpleUpload();
      return builder;
   }

   /**
    * Simplifies setup of mock scenarios that differ slightly from a standard scenario.
    * 
    * Exposes immutable fields to be used when mocking so that it is easy to use the
    * same values when verifying.
    *
    */
   private static class MockConfig
   {
      public final String fileType;
      public final boolean first, last;
      public final long size;
      public final InputStream fileStream;
      public final String hash;
      public final String params, storedParams;

      public final DocumentFileUploadForm uploadForm;

      public final GlobalDocumentId id;
      public final String projectSlug, versionSlug, docId;
      public final EntityStatus projectStatus, versionStatus;

      public HDocument existingDocument;

      public final boolean hasImportTemplatePermission, plaintextAdapterAvailable;

      private MockConfig(Builder builder)
      {
         id = new GlobalDocumentId(builder.projectSlug, builder.versionSlug, builder.docId);
         projectSlug = builder.projectSlug;
         versionSlug = builder.versionSlug;
         docId = builder.docId;
         projectStatus = builder.projectStatus;
         versionStatus = builder.versionStatus;

         fileType = builder.fileType;
         first = builder.first;
         last = builder.last;
         size = builder.size;
         fileStream = builder.fileStream;
         hash = builder.hash;
         params = builder.params;
         storedParams = builder.storedParams;

         uploadForm = new DocumentFileUploadForm();
         uploadForm.setFileType(fileType);
         uploadForm.setFirst(first);
         uploadForm.setLast(last);
         uploadForm.setSize(size);
         uploadForm.setFileStream(fileStream);
         uploadForm.setHash(hash);
         uploadForm.setAdapterParams(params);

         existingDocument = builder.existingDocument;

         hasImportTemplatePermission = builder.hasImportTemplatePermission;
         plaintextAdapterAvailable = builder.plaintextAdapterAvailable;
      }

      @SuppressWarnings("unused")
      private static class Builder
      {
         private static final String basicDocumentContent = "test";
         private static final String hashOfBasicDocumentContent = "d41d8cd98f00b204e9800998ecf8427e";

         private String projectSlug, versionSlug, docId;
         private EntityStatus projectStatus, versionStatus;
         private String fileType;
         private boolean first, last;
         private long size;
         private InputStream fileStream;
         private String hash;
         private String params, storedParams;
         public HDocument existingDocument;
         private boolean hasImportTemplatePermission, plaintextAdapterAvailable;

         public Builder projectSlug(String projectSlug)
         {
            this.projectSlug = projectSlug;
            return this;
         }

         public Builder versionSlug(String versionSlug)
         {
            this.versionSlug = versionSlug;
            return this;
         }

         public Builder docId(String docId)
         {
            this.docId = docId;
            return this;
         }

         public Builder projectStatus(EntityStatus projectStatus)
         {
            this.projectStatus = projectStatus;
            return this;
         }

         public Builder versionStatus(EntityStatus versionStatus)
         {
            this.versionStatus = versionStatus;
            return this;
         }

         public Builder fileType(String fileType)
         {
            this.fileType = fileType;
            return this;
         }

         public Builder first(boolean first)
         {
            this.first = first;
            return this;
         }

         public Builder last(boolean last)
         {
            this.last = last;
            return this;
         }

         public Builder size(long size)
         {
            this.size = size;
            return this;
         }

         public Builder fileStream(InputStream fileStream)
         {
            this.fileStream = fileStream;
            return this;
         }

         public Builder hash(String hash)
         {
            this.hash = hash;
            return this;
         }

         public Builder params(String params)
         {
            this.params = params;
            return this;
         }

         public Builder storedParams(String storedParams)
         {
            this.storedParams = storedParams;
            return this;
         }

         public Builder existingDocument(HDocument document)
         {
            this.existingDocument = document;
            return this;
         }

         public Builder hasImportTemplatePermission(boolean hasPermission)
         {
            this.hasImportTemplatePermission = hasPermission;
            return this;
         }

         public Builder plaintextAdapterAvailable(boolean available)
         {
            this.plaintextAdapterAvailable = available;
            return this;
         }

         public Builder setSimpleUpload()
         {
            projectSlug = "myproject";
            versionSlug = "myversion";
            docId = "mydoc";
            projectStatus = EntityStatus.ACTIVE;
            versionStatus = EntityStatus.ACTIVE;

            fileType("txt");
            first = true;
            last = true;
            size = 4L;
            fileStream = new ByteArrayInputStream(basicDocumentContent.getBytes());
            hash = hashOfBasicDocumentContent;
            params = "params";
            storedParams = "stored params";

            existingDocument = null;

            hasImportTemplatePermission = true;
            plaintextAdapterAvailable = true;

            return this;
         }

         /**
          * Set up mocks based on configured values.
          */
         public MockConfig build()
         {
            return new MockConfig(this);
         }

      }
   }
}
