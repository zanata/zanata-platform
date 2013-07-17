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
import java.io.SequenceInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Vector;

import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.util.Hex;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.exception.HashMismatchException;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HDocumentUploadPart;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.service.VirusScanner;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;

@Slf4j
public class DocumentUpload
{

   protected final DocumentService documentServiceImpl;
   protected final VirusScanner virusScanner;
   protected final ZanataIdentity identity;
   protected final Session session;
   protected final DocumentDAO documentDAO;
   protected final ProjectIterationDAO projectIterationDAO;
   protected final TranslationFileService translationFileServiceImpl;

   public DocumentUpload(
         ZanataIdentity identity,
         Session session,
         DocumentDAO documentDAO,
         ProjectIterationDAO projectIterationDAO,
         DocumentService documentServiceImpl,
         VirusScanner virusScanner,
         TranslationFileService translationFileServiceImpl)
   {
      this.identity = identity;
      this.session = session;
      this.documentDAO = documentDAO;
      this.projectIterationDAO = projectIterationDAO;
      this.documentServiceImpl = documentServiceImpl;
      this.virusScanner = virusScanner;
      this.translationFileServiceImpl = translationFileServiceImpl;
   }

   public static void checkUploadPreconditions(GlobalDocumentId id,
         DocumentFileUploadForm uploadForm, ZanataIdentity identity,
         ProjectIterationDAO projectIterationDAO, Session session)
   {
      if (!identity.isLoggedIn())
      {
         throw new AuthorizationException("Valid combination of username and api-key for this" +
               " server were not included in the request.");
      }
   
      if (id.getDocId() == null || id.getDocId().isEmpty())
      {
         throw new ChunkUploadException(Status.PRECONDITION_FAILED,
               "Required query string parameter 'docId' was not found.");
      }
   
      if (uploadForm.getFileStream() == null)
      {
         throw new ChunkUploadException(Status.PRECONDITION_FAILED,
               "Required form parameter 'file' containing file content was not found.");
      }
   
      if (uploadForm.getFirst() == null || uploadForm.getLast() == null)
      {
         throw new ChunkUploadException(Status.PRECONDITION_FAILED,
               "Form parameters 'first' and 'last' must both be provided.");
      }
   
      if (!uploadForm.getFirst())
      {
         if (uploadForm.getUploadId() == null)
         {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                  "Form parameter 'uploadId' must be provided when this is not the first part.");
         }
   
         HDocumentUpload upload = retrieveUploadObject(uploadForm, session);
         if (upload == null)
         {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                  "No incomplete uploads found for uploadId '" + uploadForm.getUploadId() + "'.");
         }
         if (!upload.getDocId().equals(id.getDocId()))
         {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                  "Supplied uploadId '" + uploadForm.getUploadId()
                  + "' in request is not valid for document '" + id.getDocId() + "'.");
         }
      }
   
      String fileType = uploadForm.getFileType();
      if (fileType == null || fileType.isEmpty())
      {
         throw new ChunkUploadException(Status.PRECONDITION_FAILED,
               "Required form parameter 'type' was not found.");
      }
   
      if (DocumentType.typeFor(fileType) == null)
      {
         throw new ChunkUploadException(Status.PRECONDITION_FAILED,
               "Value '" + fileType + "' is not a recognized document type.");
      }
   
      String contentHash = uploadForm.getHash();
      if (contentHash == null || contentHash.isEmpty())
      {
         throw new ChunkUploadException(Status.PRECONDITION_FAILED,
               "Required form parameter 'hash' was not found.");
      }
   
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(id.getProjectSlug(), id.getVersionSlug());
      if (projectIteration == null)
      {
         throw new ChunkUploadException(Status.NOT_FOUND,
               "The specified project-version \"" + id.getProjectSlug() + ":" + id.getVersionSlug() +
               "\" does not exist on this server.");
      }
   
      if (projectIteration.getProject().getStatus() != EntityStatus.ACTIVE)
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "The project \"" + id.getProjectSlug() + "\" is not active. Document upload is not allowed.");
      }
   
      if (projectIteration.getStatus() != EntityStatus.ACTIVE)
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "The project-version \"" + id.getProjectSlug() + ":" + id.getVersionSlug() +
               "\" is not active. Document upload is not allowed.");
      }
   }

   private static HDocumentUpload retrieveUploadObject(DocumentFileUploadForm uploadForm, Session session)
   {
      // TODO put in DAO
      Criteria criteria = session.createCriteria(HDocumentUpload.class);
      criteria.add(Restrictions.idEq(uploadForm.getUploadId()));
      HDocumentUpload upload = (HDocumentUpload) criteria.uniqueResult();
      return upload;
   }

   public static HDocumentUpload saveUploadPart(GlobalDocumentId id,
         HLocale locale, DocumentFileUploadForm uploadForm, Session session,
         ProjectIterationDAO projectIterationDAO)
   {
      HDocumentUpload upload;
      if (uploadForm.getFirst())
      {
         upload = createMultipartUpload(id, uploadForm, locale, projectIterationDAO);
      }
      else
      {
         upload = retrieveUploadObject(uploadForm, session);
      }
      saveUploadPart(uploadForm, upload, session);
      return upload;
   }

   private static HDocumentUpload createMultipartUpload(GlobalDocumentId id, DocumentFileUploadForm uploadForm, HLocale locale,
         ProjectIterationDAO projectIterationDAO)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(id.getProjectSlug(), id.getVersionSlug());
      HDocumentUpload newUpload = new HDocumentUpload();
      newUpload.setProjectIteration(projectIteration);
      newUpload.setDocId(id.getDocId());
      newUpload.setType(DocumentType.typeFor(uploadForm.getFileType()));
      // locale intentionally left null for source
      newUpload.setLocale(locale);
      newUpload.setContentHash(uploadForm.getHash());
      return newUpload;
   }

   private static void saveUploadPart(DocumentFileUploadForm uploadForm, HDocumentUpload upload,
         Session session)
   {
      Blob partContent = session.getLobHelper().createBlob(uploadForm.getFileStream(), uploadForm.getSize().intValue());
      HDocumentUploadPart newPart = new HDocumentUploadPart();
      newPart.setContent(partContent);
      upload.getParts().add(newPart);
      session.saveOrUpdate(upload);
      session.flush();
   }

   public static boolean isSinglePart(DocumentFileUploadForm uploadForm)
   {
      return uploadForm.getFirst() && uploadForm.getLast();
   }

   public static File combineToTempFileAndDeleteUploadRecord(HDocumentUpload upload, Session session,
         TranslationFileService transFileService)
   {
      File tempFile;
      try
      {
         tempFile = DocumentUpload.combineToTempFile(upload, transFileService);
      }
      catch (HashMismatchException e)
      {
         throw new ChunkUploadException(Status.CONFLICT,
               "MD5 hash \"" + e.getExpectedHash() + "\" sent with initial request does not match" +
               " server-generated hash of combined parts \"" + e.getGeneratedHash() +
               "\". Upload aborted. Retry upload from first part.");
      }
      catch (SQLException e)
      {
         throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
               "Error while retreiving document upload part contents", e);
      }
      finally
      {
         // no more need for upload
         session.delete(upload);
      }
      return tempFile;
   }

   private static File combineToTempFile(HDocumentUpload upload, TranslationFileService service) throws SQLException
   {
      Vector<InputStream> partStreams = new Vector<InputStream>();
      for (HDocumentUploadPart part : upload.getParts())
      {
         partStreams.add(part.getContent().getBinaryStream());
      }
   
      MessageDigest md;
      try
      {
         md = MessageDigest.getInstance("MD5");
      }
      catch (NoSuchAlgorithmException e)
      {
         log.error("MD5 algorithm not available.", e);
         throw new RuntimeException(e);
      }
      InputStream combinedParts = new SequenceInputStream(partStreams.elements());
      combinedParts = new DigestInputStream(combinedParts, md);
      File tempFile = service.persistToTempFile(combinedParts);
      String md5hash = new String(Hex.encodeHex(md.digest()));
   
      if (!md5hash.equals(upload.getContentHash()))
      {
         throw new HashMismatchException("MD5 hashes do not match.\n", upload.getContentHash(), md5hash);
      }
      return tempFile;
   }

   public static InputStream getInputStream(Optional<File> tempFile, DocumentFileUploadForm uploadForm) throws FileNotFoundException
   {
      if (tempFile.isPresent())
      {
         return new FileInputStream(tempFile.get());
      }
      else
      {
         return uploadForm.getFileStream();
      }
   }

   public static boolean isNewDocument(GlobalDocumentId id, DocumentDAO dao)
   {
      return dao.getByProjectIterationAndDocId(id.getProjectSlug(), id.getVersionSlug(), id.getDocId()) == null;
   }

   public static File persistTempFileFromUpload(DocumentFileUploadForm uploadForm, TranslationFileService transFileService)
   {
      File tempFile;
      try
      {
         MessageDigest md = MessageDigest.getInstance("MD5");
         InputStream fileContents = new DigestInputStream(uploadForm.getFileStream(), md);
         tempFile = transFileService.persistToTempFile(fileContents);
         String md5hash = new String(Hex.encodeHex(md.digest()));
         if (!md5hash.equals(uploadForm.getHash()))
         {
            throw new ChunkUploadException(Status.CONFLICT,
                  "MD5 hash \"" + uploadForm.getHash() +
                  "\" sent with request does not match server-generated hash. " +
                  "Aborted upload operation.");
         }
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
               "MD5 hash algorithm not available", e);
      }
      return tempFile;
   }

}
