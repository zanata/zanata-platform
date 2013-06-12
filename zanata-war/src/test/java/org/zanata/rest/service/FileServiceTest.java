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
package org.zanata.rest.service;

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

import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class FileServiceTest
{

   private static final String basicDocumentContent = "test";
   private static final String hashOfBasicDocumentContent = "d41d8cd98f00b204e9800998ecf8427e";

   SeamAutowire seam = SeamAutowire.instance();

   @Mock private ZanataIdentity identity;

   @Mock private ProjectIterationDAO projectIterationDAO;
   @Mock private TranslationFileService translationFileService;
   @Mock private DocumentService documentService;
   @Mock private DocumentDAO documentDAO;

   @Mock private HProject project;
   @Mock private HProjectIteration projectIteration;

   @Captor private ArgumentCaptor<Optional<String>> paramCaptor;
   @Captor private ArgumentCaptor<HRawDocument> persistedRawDocument;

   private FileResource fileService;

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
      .allowCycles();

      fileService = seam.autowire(FileService.class);
   }

   public void respondUnauthorizedIfNotLoggedIn()
   {
      mockLoggedIn(false);
      assertErrorResponse(
            fileService.uploadSourceFile("project", "version", "doc", new DocumentFileUploadForm()),
            Status.UNAUTHORIZED,
            "Valid combination of username and api-key for this server were not included in the request.");
   }

   public void usefulMessageIfNoFileContent()
   {
      MockConfig conf = defaultUpload().fileStream(null).build();
      mockLoggedIn(true);
      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.PRECONDITION_FAILED,
            "Required form parameter 'file' containing file content was not found.");
   }

   public void usefulMessageIfNoFileType()
   {
      MockConfig conf = defaultUpload().fileType(null).build();
      mockLoggedIn(true);
      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.PRECONDITION_FAILED,
            "Required form parameter 'type' was not found.");
   }

   public void usefulMessageIfNoContentHash()
   {
      MockConfig conf = defaultUpload().hash(null).build();
      mockLoggedIn(true);
      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.PRECONDITION_FAILED,
            "Required form parameter 'hash' was not found.");
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

      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.FORBIDDEN,
            "The project \"myproject\" is not active. Document upload is not allowed.");
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

      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.FORBIDDEN,
            "The project-version \"myproject:myversion\" is not active. Document upload is not allowed.");
   }

   public void usefulMessageWhenSourceUploadNotAllowed() throws IOException
   {
      MockConfig conf = defaultUpload().hasImportTemplatePermission(false).build();
      mockRequiredServices(conf);

      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.FORBIDDEN,
            "You do not have permission to upload source documents to project-version \"myproject:myversion\".");
   }

   public void usefulMessageWhenFileTypeInvalid() throws IOException
   {
      // Note: could pass non-valid type rather than hacking it at the back
      MockConfig conf = defaultUpload().plaintextAdapterAvailable(false).build();
      mockRequiredServices(conf);

      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.BAD_REQUEST,
            "The type \"txt\" specified in form parameter 'type' is not valid for a source file on this server.");
   }

   public void usefulMessageWhenHashInvalid() throws IOException
   {
      MockConfig conf = defaultUpload().hash("incorrect hash").build();
      mockRequiredServices(conf);

      assertErrorResponse(
            fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm),
            Status.CONFLICT,
            "MD5 hash \"incorrect hash\" sent with request does not match server-generated hash. Aborted upload operation.");
   }

   public void canUploadNewDocument() throws IOException
   {
      MockConfig conf = defaultUpload().build();
      mockRequiredServices(conf);

      Response response = fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm);

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.CREATED));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(1));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getSuccessMessage(),
            is("Upload of new source document successful."));
      assertThat(chunkResponse.getErrorMessage(), is(nullValue()));
   }

   public void canUploadExistingDocument() throws IOException
   {
      MockConfig conf = defaultUpload().existingDocument(new HDocument()).build();
      mockRequiredServices(conf);

      Response response = fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm);

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.OK));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(1));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getSuccessMessage(),
            is("Upload of new version of source document successful."));
      assertThat(chunkResponse.getErrorMessage(), is(nullValue()));
   }

   public void usesGivenParameters() throws IOException
   {
      MockConfig conf = defaultUpload().build();
      mockRequiredServices(conf);
      fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm);
      assertThat(paramCaptor.getValue().get(), is(conf.params));
   }

   public void fallsBackOnStoredParameters() throws IOException
   {
      MockConfig conf = defaultUpload().params(null).build();
      mockRequiredServices(conf);
      fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm);
      assertThat(paramCaptor.getValue().get(), is(conf.storedParams));
   }

   public void uploadParametersAreStored() throws IOException
   {
      MockConfig conf = defaultUpload().build();
      mockRequiredServices(conf);
      fileService.uploadSourceFile(conf.projectSlug, conf.versionSlug, conf.docId, conf.uploadForm);
      assertThat(persistedRawDocument.getValue().getAdapterParameters(), is(conf.params));
   }

   private static void assertErrorResponse(Response response, Status errorStatus, String errorMessage)
   {
      assertThat(Status.fromStatusCode(response.getStatus()), is(errorStatus));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is(errorMessage));
   }

   private void mockRequiredServices(MockConfig conf) throws IOException
   {
      mockLoggedIn(true);
      mockProjectAndVersionStatus(conf);
      when(identity.hasPermission("import-template", projectIteration)).thenReturn(conf.hasImportTemplatePermission);
      when(translationFileService.hasAdapterFor(DocumentType.PLAIN_TEXT)).thenReturn(conf.plaintextAdapterAvailable);
      ZanataCredentials creds = new ZanataCredentials();
      creds.setUsername("johnsmith");
      when(identity.getCredentials()).thenReturn(creds);
      File someFile = File.createTempFile("tests", "something");
      when(translationFileService.persistToTempFile(Matchers.<InputStream>any())).thenReturn(someFile);
      when(documentDAO.getAdapterParams(conf.projectSlug, conf.versionSlug, conf.docId))
            .thenReturn(Optional.fromNullable(conf.storedParams));
      when(documentDAO.addRawDocument(Matchers.<HDocument>any(), persistedRawDocument.capture()))
            .thenReturn(new HRawDocument());
      when(documentDAO.getByProjectIterationAndDocId(conf.projectSlug, conf.versionSlug,
            conf.docId)).thenReturn(conf.existingDocument);
      Resource document = new Resource();
      when(translationFileService.parseUpdatedAdapterDocumentFile(
            Matchers.<URI>any(), eq(conf.docId), eq(conf.fileType), paramCaptor.capture()))
            .thenReturn(document);
      when(documentService.saveDocument(eq(conf.projectSlug), eq(conf.versionSlug), Matchers.<Resource>any(),
            Matchers.anySet(), Matchers.anyBoolean()))
            .thenReturn(new HDocument());
   }

   private void mockLoggedIn(boolean loggedIn)
   {
      when(identity.isLoggedIn()).thenReturn(loggedIn);
   }

   private void mockProjectAndVersionStatus(MockConfig conf)
   {
      when(projectIterationDAO.getBySlug(conf.projectSlug, conf.versionSlug)).thenReturn(projectIteration);
      when(projectIteration.getProject()).thenReturn(project);
      when(project.getStatus()).thenReturn(conf.projectStatus);
      when(projectIteration.getStatus()).thenReturn(conf.versionStatus);
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
      // exposed for reference in test assertions
      public final String fileType;
      public final boolean first, last;
      public final long size;
      // or just a string
      public final InputStream fileStream;
      public final String hash;
      public final String params, storedParams;

      public final DocumentFileUploadForm uploadForm;

      public final String projectSlug, versionSlug, docId;
      public final EntityStatus projectStatus, versionStatus;

      public HDocument existingDocument;

      public final boolean hasImportTemplatePermission, plaintextAdapterAvailable;

      private MockConfig(Builder builder)
      {
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

      private static class Builder
      {
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
