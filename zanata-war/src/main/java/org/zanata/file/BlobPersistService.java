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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;

import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.DocumentDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.HDocumentUploadPart;
import org.zanata.model.HRawDocument;

@Name("blobPersistService")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Slf4j
public class BlobPersistService implements FilePersistService, UploadPartPersistService
{

   @In
   private Session session;
   @In
   private DocumentDAO documentDAO;

   public HDocumentUploadPart newUploadPartFromStream(InputStream partContentStream, int contentLength)
   {
      HDocumentUploadPart newPart = new HDocumentUploadPart();
      Blob partContent = session.getLobHelper().createBlob(partContentStream, contentLength);
      newPart.setContent(partContent);
      return newPart;
   }

   public void persistRawDocumentContentFromFile(HRawDocument rawDocument, File rawFile)
   {
      FileInputStream tempFileStream;
      try
      {
         tempFileStream = new FileInputStream(rawFile);
      }
      catch (FileNotFoundException e)
      {
         // TODO damason: throw more appropriate exception and handle in caller
         log.error("Failed to open stream from temp source file", e);
         throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
               "Error saving uploaded document on server, download in original format may fail.\n",
               e);
      }
      LobHelper lobHelper = documentDAO.getLobHelper();
      Blob fileContents = lobHelper.createBlob(tempFileStream, (int) rawFile.length());
      rawDocument.setContent(fileContents);
   }

}
