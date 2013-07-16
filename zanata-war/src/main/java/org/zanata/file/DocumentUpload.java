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
import java.util.Collections;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Criteria;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.util.Hex;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.exception.HashMismatchException;
import org.zanata.exception.VirusDetectedException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HDocumentUploadPart;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.VirusScanner;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Slf4j
public class DocumentUpload
{

   public static final HLocale NULL_LOCALE = null;

   private final DocumentService documentServiceImpl;
   private final VirusScanner virusScanner;
   private final ZanataIdentity identity;
   private final Session session;
   private final DocumentDAO documentDAO;
   private final ProjectIterationDAO projectIterationDAO;
   private final TranslationFileService translationFileServiceImpl;


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

   public Response tryUploadSourceFile(String projectSlug, String iterationSlug, String docId,
         DocumentFileUploadForm uploadForm)
   {
      try
      {
         GlobalDocumentId id = new GlobalDocumentId(projectSlug, iterationSlug, docId);
         checkSourceUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm, identity,
               session, projectIterationDAO, translationFileServiceImpl);
   
         Optional<File> tempFile;
         int totalChunks;
   
         if (!uploadForm.getLast())
         {
            HDocumentUpload upload = saveUploadPart(id.getProjectSlug(), id.getVersionSlug(),
                  id.getDocId(), NULL_LOCALE, uploadForm, session, projectIterationDAO);
            totalChunks = upload.getParts().size();
            return Response.status(Status.ACCEPTED)
                  .entity(new ChunkUploadResponse(upload.getId(), totalChunks, true,
                        "Chunk accepted, awaiting remaining chunks."))
                  .build();
         }
   
         if (isSinglePart(uploadForm))
         {
            totalChunks = 1;
            tempFile = Optional.<File>absent();
         }
         else
         {
            HDocumentUpload upload = saveUploadPart(projectSlug, iterationSlug, docId, NULL_LOCALE,
                  uploadForm, session, projectIterationDAO);
            totalChunks = upload.getParts().size();
            tempFile = Optional.of(combineToTempFileAndDeleteUploadRecord(upload, session, translationFileServiceImpl));
         }
   
         if (uploadForm.getFileType().equals(".pot"))
         {
            InputStream potStream = getInputStream(tempFile, uploadForm);
            parsePotFile(potStream, projectSlug, iterationSlug, docId, uploadForm,
                  translationFileServiceImpl, documentServiceImpl, documentDAO);
         }
         else
         {
            if (!tempFile.isPresent())
            {
               tempFile = Optional.of(persistTempFileFromUpload(uploadForm, translationFileServiceImpl));
            }
            processAdapterFile(tempFile.get(), projectSlug, iterationSlug, docId, uploadForm,
                  virusScanner, documentDAO, documentServiceImpl, translationFileServiceImpl,
                  identity);
         }
         if (tempFile.isPresent())
         {
            tempFile.get().delete();
         }
         return sourceUploadSuccessResponse(isNewDocument(projectSlug, iterationSlug, docId, documentDAO), totalChunks);
      }
      catch (ChunkUploadException e)
      {
         return Response.status(e.getStatusCode())
               .entity(new ChunkUploadResponse(e.getMessage()))
               .build();
      }
      catch (FileNotFoundException e)
      {
         log.error("failed to create input stream from temp file", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR)
               .entity(e).build();
      }
   }

   public static void checkSourceUploadPreconditions(String projectSlug, String iterationSlug, String docId,
         DocumentFileUploadForm uploadForm,
         ZanataIdentity identity,
         Session session,
         ProjectIterationDAO projectIterationDAO,
         TranslationFileService translationFileServiceImpl)
   {
      try
      {
         checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm, identity, projectIterationDAO, session);
         checkSourceUploadAllowed(projectSlug, iterationSlug, identity, projectIterationDAO);
      }
      catch (AuthorizationException e)
      {
         throw new ChunkUploadException(Status.UNAUTHORIZED, e.getMessage());
      }
      checkValidSourceUploadType(uploadForm, translationFileServiceImpl);
   }

   public static void checkUploadPreconditions(String projectSlug, String iterationSlug, String docId,
         DocumentFileUploadForm uploadForm, ZanataIdentity identity,
         ProjectIterationDAO projectIterationDAO, Session session)
   {
      if (!identity.isLoggedIn())
      {
         throw new AuthorizationException("Valid combination of username and api-key for this" +
               " server were not included in the request.");
      }
   
      if (docId == null || docId.isEmpty())
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
         if (!upload.getDocId().equals(docId))
         {
            throw new ChunkUploadException(Status.PRECONDITION_FAILED,
                  "Supplied uploadId '" + uploadForm.getUploadId()
                  + "' in request is not valid for document '" + docId + "'.");
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
   
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      if (projectIteration == null)
      {
         throw new ChunkUploadException(Status.NOT_FOUND,
               "The specified project-version \"" + projectSlug + ":" + iterationSlug +
               "\" does not exist on this server.");
      }
   
      if (projectIteration.getProject().getStatus() != EntityStatus.ACTIVE)
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "The project \"" + projectSlug + "\" is not active. Document upload is not allowed.");
      }
   
      if (projectIteration.getStatus() != EntityStatus.ACTIVE)
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "The project-version \"" + projectSlug + ":" + iterationSlug +
               "\" is not active. Document upload is not allowed.");
      }
   }

   public static HDocumentUpload retrieveUploadObject(DocumentFileUploadForm uploadForm, Session session)
   {
      // TODO put in DAO
      Criteria criteria = session.createCriteria(HDocumentUpload.class);
      criteria.add(Restrictions.idEq(uploadForm.getUploadId()));
      HDocumentUpload upload = (HDocumentUpload) criteria.uniqueResult();
      return upload;
   }

   public static void checkSourceUploadAllowed(String projectSlug, String iterationSlug, ZanataIdentity identity, ProjectIterationDAO projIterDAO)
   {
      if (!isDocumentUploadAllowed(projectSlug, iterationSlug, identity, projIterDAO))
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "You do not have permission to upload source documents to project-version \""
               + projectSlug + ":" + iterationSlug + "\".");
      }
   }

   public static boolean isDocumentUploadAllowed(String projectSlug, String iterationSlug, ZanataIdentity identity, ProjectIterationDAO projIterDAO)
   {
      HProjectIteration projectIteration = projIterDAO.getBySlug(projectSlug, iterationSlug);
      return projectIteration.getStatus() == EntityStatus.ACTIVE && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("import-template", projectIteration);
   }

   public static void checkValidSourceUploadType(DocumentFileUploadForm uploadForm, TranslationFileService transFileService)
   {
      if (!uploadForm.getFileType().equals(".pot")
            && !transFileService.hasAdapterFor(DocumentType.typeFor(uploadForm.getFileType())))
      {
         throw new ChunkUploadException(Status.BAD_REQUEST,
               "The type \"" + uploadForm.getFileType() + "\" specified in form parameter 'type' "
               + "is not valid for a source file on this server.");
      }
   }

   public static HDocumentUpload saveUploadPart(String projectSlug, String iterationSlug, String docId,
         HLocale locale, DocumentFileUploadForm uploadForm, Session session,
         ProjectIterationDAO projectIterationDAO)
   {
      HDocumentUpload upload;
      if (uploadForm.getFirst())
      {
         upload = createMultipartUpload(projectSlug, iterationSlug, docId, uploadForm, locale, projectIterationDAO);
      }
      else
      {
         upload = retrieveUploadObject(uploadForm, session);
      }
      saveUploadPart(uploadForm, upload, session);
      return upload;
   }

   public static HDocumentUpload createMultipartUpload(String projectSlug, String iterationSlug,
         String docId, DocumentFileUploadForm uploadForm, HLocale locale,
         ProjectIterationDAO projectIterationDAO)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HDocumentUpload newUpload = new HDocumentUpload();
      newUpload.setProjectIteration(projectIteration);
      newUpload.setDocId(docId);
      newUpload.setType(DocumentType.typeFor(uploadForm.getFileType()));
      // locale intentionally left null for source
      newUpload.setLocale(locale);
      newUpload.setContentHash(uploadForm.getHash());
      return newUpload;
   }

   public static void saveUploadPart(DocumentFileUploadForm uploadForm, HDocumentUpload upload,
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

   public static File combineToTempFile(HDocumentUpload upload, TranslationFileService service) throws SQLException
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

   public static void parsePotFile(InputStream potStream, String projectSlug, String iterationSlug,
         String docId, DocumentFileUploadForm uploadForm,
         TranslationFileService translationFileServiceImpl,
         DocumentService documentServiceImpl,
         DocumentDAO documentDAO)
   {
      Resource doc;
      doc = translationFileServiceImpl.parseUpdatedPotFile(potStream, docId, uploadForm.getFileType(),
            useOfflinePo(projectSlug, iterationSlug, docId, documentDAO, translationFileServiceImpl));
      doc.setLang( new LocaleId("en-US") );
      // TODO Copy Trans values
      documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, new StringSet(ExtensionType.GetText.toString()), false);
   }

   public static boolean useOfflinePo(String projectSlug, String iterationSlug, String docId,
         DocumentDAO documentDAO, TranslationFileService translationFileServiceImpl)
   {
      return !isNewDocument(projectSlug, iterationSlug, docId, documentDAO) && !translationFileServiceImpl.isPoDocument(projectSlug, iterationSlug, docId);
   }

   public static boolean isNewDocument(String projectSlug, String iterationSlug, String docId, DocumentDAO dao)
   {
      return dao.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId) == null;
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

   public static void processAdapterFile(@Nonnull File tempFile, String projectSlug, String iterationSlug,
         String docId, DocumentFileUploadForm uploadForm,
         VirusScanner virusScanner,
         DocumentDAO documentDAO,
         DocumentService documentServiceImpl,
         TranslationFileService translationFileServiceImpl,
         ZanataIdentity identity)
   {
      String name = projectSlug+":"+iterationSlug+":"+docId;
      try
      {
         virusScanner.scan(tempFile, name);
      }
      catch (VirusDetectedException e)
      {
         log.warn("File failed virus scan: {}", e.getMessage());
         throw new ChunkUploadException(Status.BAD_REQUEST, "Uploaded file did not pass virus scan");
      }
   
      HDocument document;
      Optional<String> params;
      params = Optional.fromNullable(Strings.emptyToNull(uploadForm.getAdapterParams()));
      if (!params.isPresent())
      {
         params = documentDAO.getAdapterParams(projectSlug, iterationSlug, docId);
      }
      try {
         Resource doc = translationFileServiceImpl.parseUpdatedAdapterDocumentFile(tempFile.toURI(), docId, uploadForm.getFileType(), params);
         doc.setLang( new LocaleId("en-US") );
         // TODO Copy Trans values
         document = documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, Collections.<String>emptySet(), false);
      }
      catch (SecurityException e)
      {
         throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
      catch (ZanataServiceException e)
      {
         throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
   
      HRawDocument rawDocument = new HRawDocument();
      rawDocument.setDocument(document);
      rawDocument.setContentHash(uploadForm.getHash());
      rawDocument.setType(DocumentType.typeFor(uploadForm.getFileType()));
      rawDocument.setUploadedBy(identity.getCredentials().getUsername());
      FileInputStream tempFileStream;
      try
      {
         tempFileStream = new FileInputStream(tempFile);
      }
      catch (FileNotFoundException e)
      {
         log.error("Failed to open stream from temp source file", e);
         throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
               "Error saving uploaded document on server, download in original format may fail.\n",
               e);
      }
      LobHelper lobHelper = documentDAO.getLobHelper();
      Blob fileContents = lobHelper.createBlob(tempFileStream, (int)tempFile.length());
      rawDocument.setContent(fileContents);
      if (params.isPresent())
      {
         rawDocument.setAdapterParameters(params.get());
      }
      documentDAO.addRawDocument(document, rawDocument);
      documentDAO.flush();
   
      translationFileServiceImpl.removeTempFile(tempFile);
   }

   public static Response sourceUploadSuccessResponse(boolean isNewDocument, int acceptedChunks)
   {
      Response response;
      ChunkUploadResponse uploadResponse = new ChunkUploadResponse();
      uploadResponse.setAcceptedChunks(acceptedChunks);
      uploadResponse.setExpectingMore(false);
      if (isNewDocument)
      {
         uploadResponse.setSuccessMessage("Upload of new source document successful.");
         response = Response.status(Status.CREATED)
               .entity(uploadResponse)
               .build();
      }
      else
      {
         uploadResponse.setSuccessMessage("Upload of new version of source document successful.");
         response = Response.status(Status.OK)
               .entity(uploadResponse)
               .build();
      }
      return response;
   }

}
