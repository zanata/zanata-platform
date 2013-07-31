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
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;

@Test(groups = { "unit-tests" })
public class SourceDocumentUploadTest
{

   SeamAutowire seam = SeamAutowire.instance();

   @Mock
   private ZanataIdentity identity;

   private SourceDocumentUpload sourceUpload;

   private static final GlobalDocumentId DEFAULT_ID = new GlobalDocumentId("project", "version", "doc");

   private static final DocumentFileUploadForm ANY_UPLOAD_FORM = new DocumentFileUploadForm();

   private Response response;

   @BeforeMethod
   public void beforeTest()
   {
      MockitoAnnotations.initMocks(this);
      seam.reset();
      seam.ignoreNonResolvable()
            .use("identity", identity)
            .allowCycles();

      sourceUpload = seam.autowire(SourceDocumentUpload.class);
   }

   @AfterMethod
   public void clearResponse()
   {
      response = null;
   }

   private void mockNotLoggedIn()
   {
      when(identity.isLoggedIn()).thenReturn(false);
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

   private void assertResponseHasStatus(Status errorStatus)
   {
      assertThat(fromStatusCode(response.getStatus()), is(errorStatus));
   }

   private void assertResponseHasErrorMessage(String errorMessage)
   {
      assertThat(getResponse().getErrorMessage(), is(errorMessage));
   }

   private void assertUploadTerminated()
   {
      assertThat(getResponse().getAcceptedChunks(), is(0));
      assertThat(getResponse().isExpectingMore(), is(false));
   }

   private ChunkUploadResponse getResponse()
   {
      return (ChunkUploadResponse) response.getEntity();
   }
}
