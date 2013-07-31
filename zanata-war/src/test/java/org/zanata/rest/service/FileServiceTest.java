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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.FilePersistService;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class FileServiceTest
{


   SeamAutowire seam = SeamAutowire.instance();

   @Mock private ZanataIdentity identity;

   @Mock private ProjectIterationDAO projectIterationDAO;
   @Mock private TranslationFileService translationFileService;
   @Mock private DocumentService documentService;
   @Mock private DocumentDAO documentDAO;

   @Mock private FilePersistService filePersistService;

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
      .use("filePersistService", filePersistService)
      .allowCycles();

      fileService = seam.autowire(FileService.class);
   }

   // TODO damason: test that parameters are correctly passed to SourceDocumentUpload



}
