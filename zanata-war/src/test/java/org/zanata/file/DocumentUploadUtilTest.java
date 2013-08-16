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
import static org.testng.Assert.fail;

import org.hibernate.Session;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentUploadDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.HDocumentUpload;
import org.zanata.service.TranslationFileService;

@Test(groups = { "unit-tests" })
public class DocumentUploadUtilTest extends DocumentUploadTest
{

   @Mock Session session;
   @Mock TranslationFileService translationFileService;
   @Mock UploadPartPersistService uploadPartPersistService;

   private DocumentUploadUtil util;

   @BeforeMethod
   public void beforeEachMethod()
   {
      MockitoAnnotations.initMocks(this);
      seam.reset();
      seam.ignoreNonResolvable()
            .use("identity", identity)
            .use("session", session)
            .use("documentDAO", documentDAO)
            .use("documentUploadDAO", documentUploadDAO)
            .use("projectIterationDAO", projectIterationDAO)
            .use("translationFileService", translationFileService)
            .use("uploadPartPersistService", uploadPartPersistService)
            .allowCycles();

      util = seam.autowire(DocumentUploadUtil.class);
   }

   public void notValidIfNotLoggedIn()
   {
      conf = defaultUpload().build();
      mockNotLoggedIn();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if user is not logged in");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(UNAUTHORIZED));
         assertThat(e.getMessage(), is("Valid combination of username and api-key for this " +
               "server were not included in the request."));
      }
   }

   public void notValidIfNoFileContent()
   {
      conf = defaultUpload().fileStream(null).build();
      mockLoggedIn();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if there is no file content");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
         assertThat(e.getMessage(), is("Required form parameter 'file' containing file content " +
               "was not found."));
      }
   }

   public void notValidIfNoFileType()
   {
      conf = defaultUpload().fileType(null).build();
      mockLoggedIn();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if file type is not set.");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
         assertThat(e.getMessage(), is("Required form parameter 'type' was not found."));
      }
   }

   public void notValidIfNoContentHash()
   {
      conf = defaultUpload().hash(null).build();
      mockLoggedIn();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if hash is not set.");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
         assertThat(e.getMessage(), is("Required form parameter 'hash' was not found."));
      }
   }

   public void notValidIfVersionDoesNotExist()
   {
      conf = defaultUpload().build();
      mockLoggedIn();
      mockVersionDoesNotExist();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if project-version does not exist.");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(NOT_FOUND));
         assertThat(e.getMessage(), is("The specified project-version \"myproject:myversion\" " +
               "does not exist on this server."));
      }
   }

   public void notValidIfProjectIsReadOnly()
   {
      notValidIfProjectStatusIs(EntityStatus.READONLY);
   }

   public void notValidIfProjectIsObsolete()
   {
      notValidIfProjectStatusIs(EntityStatus.OBSOLETE);
   }

   private void notValidIfProjectStatusIs(EntityStatus nonActiveStatus)
   {
      conf = defaultUpload().projectStatus(nonActiveStatus).build();
      mockLoggedIn();
      mockProjectAndVersionStatus();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if project is read only or obsolete.");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(FORBIDDEN));
         assertThat(e.getMessage(), is("The project \"myproject\" is not active. Document upload " +
               "is not allowed."));
      }
   }

   public void notValidIfVersionIsReadOnly()
   {
      notValidIfVersionStatusIs(EntityStatus.READONLY);
   }

   public void notValidIfVersionIsObsolete()
   {
      notValidIfVersionStatusIs(EntityStatus.OBSOLETE);
   }

   private void notValidIfVersionStatusIs(EntityStatus nonActiveStatus)
   {
      conf = defaultUpload().versionStatus(nonActiveStatus).build();
      mockLoggedIn();
      mockProjectAndVersionStatus();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if version is read only or obsolete.");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(FORBIDDEN));
         assertThat(e.getMessage(), is("The project-version \"myproject:myversion\" is not " +
               "active. Document upload is not allowed."));
      }
   }

   public void notValidIfFileTypeInvalid()
   {
      conf = defaultUpload().fileType("invalid").build();
      mockLoggedIn();
      mockProjectAndVersionStatus();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if file type is not valid.");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
         assertThat(e.getMessage(), is("Value 'invalid' is not a recognized document type."));
      }
   }

   public void subsequentPartNoUploadId()
   {
      conf = defaultUpload().first(false).uploadId(null).build();
      mockLoggedIn();
      mockProjectAndVersionStatus();
      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
         fail("Should throw exception if this is not the first part but no uploadId is supplied");
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
         assertThat(e.getMessage(), is("Form parameter 'uploadId' must be provided when this is " +
               "not the first part."));

      }
   }

   public void subsequentPartUploadNotPresent()
   {
      conf = defaultUpload().first(false).uploadId(5L).build();
      mockLoggedIn();
      mockProjectAndVersionStatus();
      Mockito.when(documentUploadDAO.findById(conf.uploadId)).thenReturn(null);

      try
      {
         util.failIfUploadNotValid(conf.id, conf.uploadForm);
      }
      catch (ChunkUploadException e)
      {
         assertThat(e.getStatusCode(), is(PRECONDITION_FAILED));
         assertThat(e.getMessage(), is("No incomplete uploads found for uploadId '5'."));
      }
   }

   // TODO damason: test not first part but docId does not match existing upload

   // TODO damason: test returning correct stream depending whether file exists

   // TODO damason: test combining upload parts
   // TODO damason: test mismatched hash when combining upload parts
   // TODO damason: test mismatched hash when persisting temp file
}
