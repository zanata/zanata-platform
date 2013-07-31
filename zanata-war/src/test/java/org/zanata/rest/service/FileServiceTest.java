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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.seam.SeamAutowire;

/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class FileServiceTest
{

   SeamAutowire seam = SeamAutowire.instance();

   @Mock private SourceDocumentUpload sourceUploader;

   @Captor private ArgumentCaptor<GlobalDocumentId> idCaptor;
   @Captor private ArgumentCaptor<DocumentFileUploadForm> formCaptor;

   private FileResource fileService;

   @BeforeMethod
   public void beforeTest()
   {
      MockitoAnnotations.initMocks(this);

      seam.reset();
      seam.ignoreNonResolvable()
            .use("sourceDocumentUploader", sourceUploader)
            .allowCycles();

      fileService = seam.autowire(FileService.class);
   }

   public void correctSourceUploadIdFromParams()
   {
      when(sourceUploader.tryUploadSourceFile(idCaptor.capture(), formCaptor.capture()))
            .thenReturn(Response.ok().build());

      GlobalDocumentId id = new GlobalDocumentId("myproject", "myversion", "myDocument");
      fileService.uploadSourceFile("myproject", "myversion", "myDocument", new DocumentFileUploadForm());

      assertThat(idCaptor.getValue(), is(equalTo(id)));
   }

   public void sourceUploadFormPassedToUploader()
   {
      when(sourceUploader.tryUploadSourceFile(idCaptor.capture(), formCaptor.capture()))
            .thenReturn(Response.ok().build());

      DocumentFileUploadForm uploadForm = new DocumentFileUploadForm();
      fileService.uploadSourceFile("test", "test", "test", uploadForm);

      assertThat(formCaptor.getValue(), is(sameInstance(uploadForm)));
   }

   public void sourceUploadResponseReturnedDirectly()
   {
      Response uploaderResponse = Response.ok().build();
      when(sourceUploader.tryUploadSourceFile(any(GlobalDocumentId.class), any(DocumentFileUploadForm.class)))
            .thenReturn(uploaderResponse);

      Response returnedResponse =
            fileService.uploadSourceFile("test", "test", "test", new DocumentFileUploadForm());

      assertThat(returnedResponse, is(sameInstance(uploaderResponse)));
   }
}
