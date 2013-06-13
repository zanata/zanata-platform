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

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
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

   @Mock private HProject project;
   @Mock private HProjectIteration projectIteration;

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
      .allowCycles();

      fileService = seam.autowire(FileService.class);
   }

   public void respondUnauthorizedIfNotLoggedIn()
   {
      when(identity.isLoggedIn()).thenReturn(false);
      Response response = fileService.uploadSourceFile("project", "version", "doc", new DocumentFileUploadForm());
      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.UNAUTHORIZED));
   }

   public void usefulMessageIfNoFileContent()
   {
      mockLoggedIn();

      DocumentFileUploadForm uploadForm = nonsenseUploadForm();
      uploadForm.setFileStream(null);

      Response response = fileService.uploadSourceFile("project", "version", "doc", uploadForm);

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.PRECONDITION_FAILED));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("Required form parameter 'file' containing file content was not found."));
   }

   public void usefulMessageIfNoFileType()
   {
      mockLoggedIn();

      DocumentFileUploadForm uploadForm = nonsenseUploadForm();
      uploadForm.setFileType(null);

      Response response = fileService.uploadSourceFile("project", "version", "doc", uploadForm);

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.PRECONDITION_FAILED));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("Required form parameter 'type' was not found."));
   }

   public void usefulMessageIfNoContentHash()
   {
      mockLoggedIn();

      DocumentFileUploadForm uploadForm = nonsenseUploadForm();
      uploadForm.setHash(null);

      Response response = fileService.uploadSourceFile("project", "version", "doc", uploadForm);

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.PRECONDITION_FAILED));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("Required form parameter 'hash' was not found."));
   }

   public void usefulMessageIfProjectIsReadOnly()
   {
      testUsefulMessageForInactiveProject(EntityStatus.READONLY);
   }

   public void usefulMessageIfProjectIsObsolete()
   {
      testUsefulMessageForInactiveProject(EntityStatus.OBSOLETE);
   }

   private void testUsefulMessageForInactiveProject(EntityStatus nonActiveStatus)
   {
      mockLoggedIn();
      mockProjectAndVersionStatus(nonActiveStatus, EntityStatus.ACTIVE);

      Response response = fileService.uploadSourceFile("myproject", "myversion", "mydoc", nonsenseUploadForm());

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.FORBIDDEN));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("The project \"myproject\" is not active. Document upload is not allowed."));
   }

   public void usefulMessageIfVersionIsReadOnly()
   {
      testUsefulMessageForInactiveVersion(EntityStatus.READONLY);
   }

   public void usefulMessageIfVersionIsObsolete()
   {
      testUsefulMessageForInactiveVersion(EntityStatus.OBSOLETE);
   }

   private void testUsefulMessageForInactiveVersion(EntityStatus nonActiveStatus)
   {
      mockLoggedIn();
      mockProjectAndVersionStatus(EntityStatus.ACTIVE, nonActiveStatus);

      Response response = fileService.uploadSourceFile("myproject", "myversion", "mydoc", nonsenseUploadForm());

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.FORBIDDEN));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("The project-version \"myproject:myversion\" is not active. Document upload is not allowed."));
   }

   public void usefulMessageWhenSourceUploadNotAllowed()
   {
      mockLoggedIn();
      mockProjectAndVersionStatus(EntityStatus.ACTIVE, EntityStatus.ACTIVE);
      when(identity.hasPermission("import-template", projectIteration)).thenReturn(false);
      DocumentFileUploadForm uploadForm = nonsenseUploadForm();
      Response response = fileService.uploadSourceFile("myproject", "myversion", "mydoc", uploadForm);

      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.FORBIDDEN));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("You do not have permission to upload source documents to project-version \"myproject:myversion\"."));
   }

   public void usefulMessageWhenFileTypeInvalid()
   {
      mockLoggedIn();
      mockProjectAndVersionStatus(EntityStatus.ACTIVE, EntityStatus.ACTIVE);

      when(identity.hasPermission("import-template", projectIteration)).thenReturn(true);
      when(translationFileService.hasAdapterFor(DocumentType.PLAIN_TEXT)).thenReturn(false);

      Response response = fileService.uploadSourceFile("myproject", "myversion", "mydoc", nonsenseUploadForm());
      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.BAD_REQUEST));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("The type \"txt\" specified in form parameter 'type' is not valid for a source file on this server."));
   }

   public void usefulMessageWhenHashInvalid()
   {
      mockLoggedIn();
      mockProjectAndVersionStatus(EntityStatus.ACTIVE, EntityStatus.ACTIVE);
      when(identity.hasPermission("import-template", projectIteration)).thenReturn(true);
      when(translationFileService.hasAdapterFor(DocumentType.PLAIN_TEXT)).thenReturn(true);
      DocumentFileUploadForm uploadForm = nonsenseUploadForm();
      uploadForm.setHash("incorrect hash");

      Response response = fileService.uploadSourceFile("myproject", "myversion", "mydoc", uploadForm);
      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.CONFLICT));
      ChunkUploadResponse chunkResponse = (ChunkUploadResponse) response.getEntity();
      assertThat(chunkResponse.getAcceptedChunks(), is(0));
      assertThat(chunkResponse.isExpectingMore(), is(false));
      assertThat(chunkResponse.getErrorMessage(),
            is("MD5 hash \"incorrect hash\" sent with request does not match server-generated hash. Aborted upload operation."));
   }

   private void mockLoggedIn()
   {
      when(identity.isLoggedIn()).thenReturn(true);
   }

   private DocumentFileUploadForm nonsenseUploadForm()
   {
      DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
      uploadForm.setFileType("txt");
      uploadForm.setFirst(true);
      uploadForm.setLast(true);
      uploadForm.setSize(4L);
      uploadForm.setFileStream(new ByteArrayInputStream(basicDocumentContent.getBytes()));
      uploadForm.setHash(hashOfBasicDocumentContent);
      uploadForm.setAdapterParams("params");
      return uploadForm;
   }

   private void mockProjectAndVersionStatus(EntityStatus projectStatus, EntityStatus versionStatus)
   {
      when(projectIterationDAO.getBySlug("myproject", "myversion")).thenReturn(projectIteration);
      when(projectIteration.getProject()).thenReturn(project);
      when(project.getStatus()).thenReturn(projectStatus);
      when(projectIteration.getStatus()).thenReturn(versionStatus);
   }

}
