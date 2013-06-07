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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class FileServiceTest
{

   SeamAutowire seam = SeamAutowire.instance();

   @Mock
   private ZanataIdentity mockIdentity;

   private FileResource fileService;

   @BeforeMethod
   public void beforeTest()
   {
      MockitoAnnotations.initMocks(this);

      seam.reset();
      seam.ignoreNonResolvable()
      .use("identity", mockIdentity)
      .allowCycles();

      fileService = seam.autowire(FileService.class);
   }

   public void respondUnauthorizedIfNotLoggedIn()
   {
      when(mockIdentity.isLoggedIn()).thenReturn(false);
      Response response = fileService.uploadSourceFile("project", "version", "doc", new DocumentFileUploadForm());
      assertThat(Status.fromStatusCode(response.getStatus()), is(Status.UNAUTHORIZED));
   }

}
