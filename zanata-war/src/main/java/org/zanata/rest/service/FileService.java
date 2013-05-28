/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.util.Hex;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
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
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.FileSystemService;
import org.zanata.service.FileSystemService.DownloadDescriptorProperties;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

@Name("fileService")
@Path(FileResource.FILE_RESOURCE)
@Produces( { MediaType.APPLICATION_OCTET_STREAM })
@Consumes( { MediaType.APPLICATION_OCTET_STREAM })
@Slf4j
public class FileService implements FileResource
{
   private static final String FILE_TYPE_OFFLINE_PO = "offlinepo";
   private static final String FILE_TYPE_OFFLINE_PO_TEMPLATE = "offlinepot";

   @In
   private ZanataIdentity identity;

   @In
   private LocaleDAO localeDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private DocumentService documentServiceImpl;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In(create=true)
   private TranslatedDocResourceService translatedDocResourceService;

   @In
   private FileSystemService fileSystemServiceImpl;

   @In
   private TranslationService translationServiceImpl;

   @In
   private TranslationFileService translationFileServiceImpl;

   @In
   private ResourceUtils resourceUtils;

   // FIXME remove when using DAO for HDocumentUpload
   @In
   private Session session;

   @Override
   @GET
   @Path(ACCEPTED_TYPES_RESOURCE)
   @Produces( MediaType.TEXT_PLAIN )
   // /file/accepted_types
   public Response acceptedFileTypes()
   {
      StringSet acceptedTypes = new StringSet("");
      acceptedTypes.addAll(translationFileServiceImpl.getSupportedExtensions());
      return Response.ok(acceptedTypes.toString()).build();
   }

   @Override
   @POST
   @Path(SOURCE_UPLOAD_TEMPLATE)
   @Consumes( MediaType.MULTIPART_FORM_DATA)
   @Produces( MediaType.APPLICATION_XML)
   public Response uploadSourceFile( @PathParam("projectSlug") String projectSlug,
                                     @PathParam("iterationSlug") String iterationSlug,
                                     @QueryParam("docId") String docId,
                                     @MultipartForm DocumentFileUploadForm uploadForm )
   {
      Response errorResponse = checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);
      if (errorResponse != null)
      {
         return errorResponse;
      }

      if (!isDocumentUploadAllowed(projectSlug, iterationSlug))
      {
         return Response.status(Status.FORBIDDEN)
               .entity(new ChunkUploadResponse("You do not have permission to upload source documents to project-version \""
                      + projectSlug + ":" + iterationSlug + "\"."))
               .build();
      }

      String fileType = uploadForm.getFileType();
      boolean isPotFile = fileType.equals(".pot");
      if (!isPotFile && !translationFileServiceImpl.hasAdapterFor(fileType))
      {
         return Response.status(Status.BAD_REQUEST)
               .entity(new ChunkUploadResponse("The type \"" + fileType + "\" specified in form parameter 'type' is not valid for a source file on this server."))
               .build();
      }

      boolean isNewDocument = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId) == null;
      boolean useOfflinePo = !isNewDocument && !translationFileServiceImpl.isPoDocument(projectSlug, iterationSlug, docId);

      boolean isSinglePart = uploadForm.getFirst() && uploadForm.getLast();

      int uploadChunks = 1;

      if (isSinglePart && isPotFile)
      {
         parsePotFile(uploadForm.getFileStream(), docId, fileType, projectSlug, iterationSlug, useOfflinePo);
         return sourceUploadSuccessResponse(isNewDocument, uploadChunks);
      }

      // persist bytes to file
      File tempFile = null;
      if (isSinglePart)
      {
         // single part, can go straight to temp file.
         try
         {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream fileContents = new DigestInputStream(uploadForm.getFileStream(), md);
            tempFile = translationFileServiceImpl.persistToTempFile(fileContents);
            String md5hash = new String(Hex.encodeHex(md.digest()));
            if (!md5hash.equals(uploadForm.getHash()))
            {
               return Response.status(Status.CONFLICT)
                     .entity(new ChunkUploadResponse("MD5 hash \"" + uploadForm.getHash()
                           + "\" sent with request does not match server-generated hash \""
                           + md5hash + "\". Aborted upload operation."))
                     .build();
            }
         }
         catch (ZanataServiceException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
         catch (NoSuchAlgorithmException e)
         {
            log.error("MD5 hash algorithm not available", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
      }
      else if (uploadForm.getFirst())
      {
         HDocumentUpload upload;
         try {
            upload = saveFirstUploadPart(projectSlug, iterationSlug, docId, uploadForm, fileType, null);
         }
         catch (IOException e)
         {
            log.error("failed to create database storage object for part file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e).build();
         }
         return Response.status(Status.ACCEPTED)
               .entity(new ChunkUploadResponse(upload.getId(), upload.getParts().size(), true, "First chunk accepted, awaiting remaining chunks."))
               .build();
      }
      else
      {
         HDocumentUpload upload;
         try
         {
            upload = saveSubsequentUploadPart(uploadForm);
         }
         catch (IOException e)
         {
            log.error("failed to create database storage object for part file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e).build();
         }

         if (!uploadForm.getLast())
         {
            return Response.status(Status.ACCEPTED)
                  .entity(new ChunkUploadResponse(upload.getId(), upload.getParts().size(), true, "Chunk accepted, awaiting remaining chunks."))
                  .build();
         }

         uploadChunks = upload.getParts().size();

         try
         {
            tempFile = combineToTempFile(upload);
         }
         catch (HashMismatchException e)
         {
            return Response.status(Status.CONFLICT)
                  .entity(new ChunkUploadResponse("MD5 hash \"" + e.getExpectedHash()
                        + "\" sent with initial request does not match server-generated hash of combined parts \""
                        + e.getGeneratedHash() + "\". Upload aborted. Retry upload from first part."))
                        .build();
         }
         catch (SQLException e)
         {
            log.error("Error while retreiving document upload part contents", e);
            throw new RuntimeException(e);
         }
         finally
         {
            // no more need for upload
            session.delete(upload);
         }
      }

      // have entire file, proceed with parsing

      if (isPotFile)
      {
         try
         {
            parsePotFile(new FileInputStream(tempFile), docId, fileType, projectSlug, iterationSlug, useOfflinePo);
         }
         catch (FileNotFoundException e)
         {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
         translationFileServiceImpl.removeTempFile(tempFile);
         return sourceUploadSuccessResponse(isNewDocument, uploadChunks);
      }

      // is adapter file
      try
      {
         virusScan(tempFile);
      }
      catch (VirusDetectedException e)
      {
         log.warn("File failed virus scan: {}", e.getMessage());
         return Response.status(Status.BAD_REQUEST).entity("uploaded file did not pass virus scan").build();
      }

      HDocument document;
      try {
         Resource doc = translationFileServiceImpl.parseUpdatedAdapterDocumentFile(tempFile.toURI(), docId, fileType);
         doc.setLang( new LocaleId("en-US") );
         // TODO Copy Trans values
         document = documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, Collections.<String>emptySet(), false);
      }
      catch (SecurityException e)
      {
         return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
      }
      catch (ZanataServiceException e)
      {
         return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
      }

      HRawDocument rawDocument = new HRawDocument();
      rawDocument.setDocument(document);
      rawDocument.setContentHash(uploadForm.getHash());
      rawDocument.setType(DocumentType.typeFor(fileType));
      rawDocument.setUploadedBy(identity.getCredentials().getUsername());
      FileInputStream tempFileStream;
      try
      {
         tempFileStream = new FileInputStream(tempFile);
      }
      catch (FileNotFoundException e)
      {
         log.error("Failed to open stream from temp source file", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR)
               .entity("Error saving uploaded document on server, download in original format may fail.\n")
               .build();
      }
      Blob fileContents = Hibernate.createBlob(tempFileStream, (int)tempFile.length());
      rawDocument.setContent(fileContents);
      documentDAO.addRawDocument(document, rawDocument);
      documentDAO.flush();

      translationFileServiceImpl.removeTempFile(tempFile);
      return sourceUploadSuccessResponse(isNewDocument, uploadChunks);
   }

   /**
    * Scans the specified file by calling out to ClamAV.
    * <p>
    * The current implementation looks for clamdscan on the system path, but
    * merely logs an error if it can't be found (or if clamd is not running),
    * rather than rejecting the file.
    * @param file
    * @throws VirusDetectedException if a virus is detected
    */
   // FIXME put this in its own class
   public static void virusScan(File file) throws VirusDetectedException
   {
      // TODO make command name and path configurable
      String scanproc = "clamdscan"; // clamscan works too, but takes ~15 seconds
      CommandLine cmdLine = new CommandLine(scanproc);
      cmdLine.addArgument("--no-summary");
      cmdLine.addArgument(file.getPath());
      DefaultExecutor executor = new DefaultExecutor();
      ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
      executor.setWatchdog(watchdog);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ExecuteStreamHandler psh = new PumpStreamHandler(output);
      executor.setStreamHandler(psh);
      executor.setExitValues(new int[] {0, 1, 2});
      try
      {
         int exitValue = executor.execute(cmdLine);
         switch(exitValue)
         {
         case 0:
            log.debug(scanproc+" clean result: {}", output);
            return;
         case 1:
            throw new VirusDetectedException(scanproc+" detected virus: " + output);
         case 2:
         default:
            log.error(scanproc+" returned error, unable to scan for viruses. output: " + output);
            // TODO enforce virus scanning by throwing exception here
//            throw new ZanataServiceException(scanproc+" return code: "+exitValue+", output: " + output);
         }
      }
      catch (IOException e)
      {
         log.error("error launching "+scanproc+", unable to scan for viruses", e);
         // TODO enforce virus scanning by throwing exception here
//         throw new ZanataServiceException(e);
      }
   }

   private File combineToTempFile(HDocumentUpload upload) throws SQLException
   {
      Vector<InputStream> partStreams = new Vector<InputStream>();
      for (HDocumentUploadPart part : upload.getParts())
      {
         partStreams.add(part.getContent().getBinaryStream());
      }
      InputStream combinedParts = new SequenceInputStream(partStreams.elements());

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
      combinedParts = new DigestInputStream(combinedParts, md);
      File tempFile = translationFileServiceImpl.persistToTempFile(combinedParts);
      String md5hash = new String(Hex.encodeHex(md.digest()));

      if (!md5hash.equals(upload.getContentHash()))
      {
         throw new HashMismatchException("MD5 hashes do not match.\n", upload.getContentHash(), md5hash);
      }
      return tempFile;
   }

   private HDocumentUpload saveSubsequentUploadPart(DocumentFileUploadForm uploadForm) throws IOException
   {
      HDocumentUpload upload = retrieveUploadObject(uploadForm);
      saveUploadPart(uploadForm, upload);
      return upload;
   }

   private HDocumentUpload retrieveUploadObject(DocumentFileUploadForm uploadForm)
   {
      // TODO put in DAO
      Criteria criteria = session.createCriteria(HDocumentUpload.class);
      criteria.add(Restrictions.idEq(uploadForm.getUploadId()));
      HDocumentUpload upload = (HDocumentUpload) criteria.uniqueResult();
      return upload;
   }

   private HDocumentUpload saveFirstUploadPart(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm, String fileType, HLocale locale) throws IOException
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HDocumentUpload newUpload = new HDocumentUpload();
      newUpload.setProjectIteration(projectIteration);
      newUpload.setDocId(docId);
      newUpload.setType(DocumentType.typeFor(fileType));
      // locale intentionally left null for source
      newUpload.setLocale(locale);
      newUpload.setContentHash(uploadForm.getHash());
      saveUploadPart(uploadForm, newUpload);
      return newUpload;
   }

   private void saveUploadPart(DocumentFileUploadForm uploadForm, HDocumentUpload upload) throws IOException
   {
      Blob partContent;
      partContent = Hibernate.createBlob(uploadForm.getFileStream(), uploadForm.getSize().intValue());
      HDocumentUploadPart newPart = new HDocumentUploadPart();
      newPart.setContent(partContent);
      upload.getParts().add(newPart);
      session.saveOrUpdate(upload);
      session.flush();
   }

   private Response checkUploadPreconditions(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
   {
      if (!identity.isLoggedIn())
      {
         return Response.status(Status.UNAUTHORIZED)
               .entity(new ChunkUploadResponse("Valid combination of username and api-key for this" +
                                               " server were not included in the request."))
               .build();
      }

      if (docId == null || docId.isEmpty())
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity(new ChunkUploadResponse("Required query string parameter 'docId' was not found."))
               .build();
      }

      if (uploadForm.getFileStream() == null)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity(new ChunkUploadResponse("Required form parameter 'file' containing file content was not found."))
               .build();
      }

      if (uploadForm.getFirst() == null || uploadForm.getLast() == null)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity(new ChunkUploadResponse("Form parameters 'first' and 'last' must both be provided."))
               .build();
      }

      if (!uploadForm.getFirst())
      {
         if (uploadForm.getUploadId() == null)
         {
            return Response.status(Status.PRECONDITION_FAILED)
                  .entity(new ChunkUploadResponse("Form parameter 'uploadId' must be provided when this is not the first part."))
                  .build();
         }

         HDocumentUpload upload = retrieveUploadObject(uploadForm);
         if (upload == null)
         {
            return Response.status(Status.PRECONDITION_FAILED)
                  .entity(new ChunkUploadResponse("No incomplete uploads found for uploadId '" + uploadForm.getUploadId() + "'."))
                  .build();
         }
         if (!upload.getDocId().equals(docId))
         {
            return Response.status(Status.PRECONDITION_FAILED)
                  .entity(new ChunkUploadResponse("Supplied uploadId '" + uploadForm.getUploadId() + "' in request is not valid for document '" + docId + "'."))
                  .build();
         }
      }

      String fileType = uploadForm.getFileType();
      if (fileType == null || fileType.isEmpty())
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity(new ChunkUploadResponse("Required form parameter 'type' was not found."))
               .build();
      }

      if (DocumentType.typeFor(fileType) == null)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity(new ChunkUploadResponse("Value '" + fileType + "' is not a recognized document type."))
               .build();
      }

      String contentHash = uploadForm.getHash();
      if (contentHash == null || contentHash.isEmpty())
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity(new ChunkUploadResponse("Required form parameter 'hash' was not found."))
               .build();
      }

      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      if (projectIteration == null)
      {
         return Response.status(Status.NOT_FOUND)
               .entity(new ChunkUploadResponse("The specified project-version \"" + projectSlug + ":" + iterationSlug + "\" does not exist on this server."))
               .build();
      }

      if (projectIteration.getProject().getStatus() != EntityStatus.ACTIVE)
      {
         return Response.status(Status.FORBIDDEN)
               .entity(new ChunkUploadResponse("The project \"" + projectSlug + "\" is not active. Document upload is not allowed."))
               .build();
      }

      if (projectIteration.getStatus() != EntityStatus.ACTIVE)
      {
         return Response.status(Status.FORBIDDEN)
               .entity(new ChunkUploadResponse("The project-version \"" + projectSlug + ":" + iterationSlug + "\" is not active. Document upload is not allowed."))
               .build();
      }

      return null;
   }

   private boolean isDocumentUploadAllowed(String projectSlug, String iterationSlug)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      return projectIteration.getStatus() == EntityStatus.ACTIVE && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("import-template", projectIteration);
   }

   private void parsePotFile(InputStream documentStream, String docId, String fileType, String projectSlug, String iterationSlug, boolean asOfflinePo)
   {
      Resource doc;
      doc = translationFileServiceImpl.parseUpdatedPotFile(documentStream, docId, fileType, asOfflinePo);
      doc.setLang( new LocaleId("en-US") );
      // TODO Copy Trans values
      documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, new StringSet(ExtensionType.GetText.toString()), false);
   }

   private Response sourceUploadSuccessResponse(boolean isNewDocument, int acceptedChunks)
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


   @Override
   @POST
   @Path(TRANSLATION_UPLOAD_TEMPLATE)
   @Consumes( MediaType.MULTIPART_FORM_DATA)
   @Produces( MediaType.APPLICATION_XML)
   public Response uploadTranslationFile( @PathParam("projectSlug") String projectSlug,
                                          @PathParam("iterationSlug") String iterationSlug,
                                          @PathParam("locale") String localeId,
                                          @QueryParam("docId") String docId,
                                          @QueryParam("merge") String merge,
                                          @MultipartForm DocumentFileUploadForm uploadForm )
   {
      Response errorResponse = checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);
      if (errorResponse != null)
      {
         return errorResponse;
      }

      HLocale locale = localeDAO.findByLocaleId(new LocaleId(localeId));
      if (localeId == null)
      {
         return Response.status(Status.NOT_FOUND)
               .entity(new ChunkUploadResponse("The specified locale \"" + localeId + "\" does not exist on this server."))
               .build();
      }

      if (!isTranslationUploadAllowed(projectSlug, iterationSlug, locale))
      {
         return Response.status(Status.FORBIDDEN)
               .entity(new ChunkUploadResponse("You do not have permission to upload translations for locale \"" + localeId
                     + "\" to project-version \"" + projectSlug + ":" + iterationSlug + "\"."))
               .build();
      }

      if (documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId) == null)
      {
         return Response.status(Status.NOT_FOUND)
               .entity(new ChunkUploadResponse("No document with id \"" + docId + "\" exists in project-version \"" + projectSlug + ":" + iterationSlug + "\"."))
               .build();
      }


      String fileType = uploadForm.getFileType();
      boolean isPoFile = fileType.equals(".po");
      if (!isPoFile && !translationFileServiceImpl.hasAdapterFor(fileType))
      {
         return Response.status(Status.BAD_REQUEST)
               .entity(new ChunkUploadResponse("The type \"" + fileType + "\" specified in form parameter 'type' is not valid for a translation file on this server."))
               .build();
      }

      InputStream fileContents;
      int acceptedChunks;
      if (uploadForm.getFirst() && uploadForm.getLast())
      {
         // TODO wrap in hash digester and check hash
         fileContents = uploadForm.getFileStream();
         acceptedChunks = 1;
      }
      else if (uploadForm.getFirst())
      {
         HDocumentUpload upload;
         try
         {
            upload = saveFirstUploadPart(projectSlug, iterationSlug, docId, uploadForm, fileType, locale);
         }
         catch (IOException e)
         {
            log.error("failed to create database storage object for part file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e).build();
         }
         return Response.status(Status.ACCEPTED)
               .entity(new ChunkUploadResponse(upload.getId(), upload.getParts().size(), true, "First chunk accepted, awaiting remaining chunks."))
               .build();
      }
      else
      {
         HDocumentUpload upload;
         try
         {
            upload = saveSubsequentUploadPart(uploadForm);
            acceptedChunks = upload.getParts().size();
         }
         catch (IOException e)
         {
            log.error("failed to create database storage object for part file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e).build();
         }

         if (!uploadForm.getLast())
         {
            return Response.status(Status.ACCEPTED)
                  .entity(new ChunkUploadResponse(upload.getId(), upload.getParts().size(), true, "Chunk accepted, awaiting remaining chunks."))
                  .build();
         }

         try
         {
            File tempFile = combineToTempFile(upload);
            fileContents = new FileInputStream(tempFile);
         }
         catch (HashMismatchException e)
         {
            return Response.status(Status.CONFLICT)
                  .entity("MD5 hash \"" + e.getExpectedHash()
                        + "\" sent with initial request does not match server-generated hash of combined parts \""
                        + e.getGeneratedHash() + "\". Upload aborted. Retry upload from first part.\n")
                        .build();
         }
         catch (SQLException e)
         {
            log.error("Error while retreiving document upload part contents", e);
            throw new RuntimeException(e);
         }
         catch (FileNotFoundException e)
         {
            log.error("Error while generating input stream for temp file", e);
            throw new RuntimeException(e);
         }
         finally
         {
            // no more need for upload
            session.delete(upload);
         }
      }

      // TODO useful error messages for failed parsing
      TranslationsResource transRes =
            translationFileServiceImpl.parseTranslationFile(fileContents,
                                                            uploadForm.getFileType(),
                                                            localeId,
                                                            translationFileServiceImpl.isPoDocument(projectSlug, iterationSlug, docId));

      MergeType mergeType;
      if ("import".equals(merge))
      {
         mergeType = MergeType.IMPORT;
      }
      else
      {
         mergeType = MergeType.AUTO;
      }

      Set<String> extensions;
      if (isPoFile)
      {
         extensions = new StringSet(ExtensionType.GetText.toString());
      }
      else
      {
         extensions = Collections.<String>emptySet();
      }

      // TODO useful error message for failed saving?
      List<String> warnings = translationServiceImpl.translateAllInDoc(projectSlug, iterationSlug,
            docId, locale.getLocaleId(), transRes, extensions, mergeType);

      ChunkUploadResponse response = new ChunkUploadResponse();
      response.setExpectingMore(false);
      response.setAcceptedChunks(acceptedChunks);

      if (warnings != null && !warnings.isEmpty())
      {
         StringBuilder entity = new StringBuilder("Upload succeeded but had the following warnings:");
         for (String warning : warnings)
         {
            entity.append("\n\t");
            entity.append(warning);
         }
         entity.append("\n");
         response.setSuccessMessage(entity.toString());
      }
      else
      {
         response.setSuccessMessage("Translations uploaded successfully");
      }
      return Response.status(Status.OK).entity(response).build();
   }

   private boolean isTranslationUploadAllowed(String projectSlug, String iterationSlug, HLocale localeId)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      // TODO should this check be "add-translation" or "modify-translation"?
      // They appear to be granted identically at the moment.
      return projectIteration.getStatus() == EntityStatus.ACTIVE && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("add-translation", projectIteration.getProject(), localeId);
   }

   /**
    * Downloads a single source file.
    * 
    * @param projectSlug
    * @param iterationSlug
    * @param fileType use 'raw' for original source if available, or 'pot' to
    *           generate pot from source strings
    * @param docId
    * @return response with status code 404 if the document is not found, 415 if
    *         fileType is not valid for the document, otherwise 200 with
    *         attached document.
    */
   @Override
   @GET
   @Path(SOURCE_DOWNLOAD_TEMPLATE)
   // /file/source/{projectSlug}/{iterationSlug}/{fileType}?docId={docId}
   public Response downloadSourceFile( @PathParam("projectSlug") String projectSlug,
                                       @PathParam("iterationSlug") String iterationSlug,
                                       @PathParam("fileType") String fileType,
                                       @QueryParam("docId") String docId)
   {
      // TODO scan (again) for virus
      HDocument document = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);
      if( document == null )
      {
         return Response.status(Status.NOT_FOUND).build();
      }

      if (FILETYPE_RAW_SOURCE_DOCUMENT.equals(fileType))
      {
         if (document.getRawDocument() == null)
         {
            return Response.status(Status.NOT_FOUND).build();
         }
         InputStream fileContents;
         try
         {
            fileContents = document.getRawDocument().getContent().getBinaryStream();
         }
         catch (SQLException e)
         {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
         StreamingOutput output = new InputStreamStreamingOutput(fileContents);
         return Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + "\"")
               .entity(output).build();
      }
      else if ("pot".equals(fileType) || FILE_TYPE_OFFLINE_PO_TEMPLATE.equals(fileType))
      {
         // Note: could give 404 or unsupported media type for "pot" in non-po projects,
         //       and suggest using offlinepo
         Resource res = resourceUtils.buildResource(document);
         StreamingOutput output = new POTStreamingOutput(res, FILE_TYPE_OFFLINE_PO_TEMPLATE.equals(fileType));
         return Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + ".pot\"")
               .type(MediaType.TEXT_PLAIN)
               .entity(output).build();
      }
      else
      {
         return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
      }
   }

   /**
    * Downloads a single translation file.
    * 
    * To download a preview-document or translated document where a raw source
    * document is available, use fileType 'half_baked' and 'baked' respectively.
    * 
    * @param projectSlug Project identifier.
    * @param iterationSlug Project iteration identifier.
    * @param locale Translations for this locale will be contained in the
    *           downloaded document.
    * @param fileType File type to be downloaded. (Options: 'po', 'half_baked',
    *           'baked')
    * @param docId Document identifier to fetch translations for.
    * @return The following response status codes will be returned from this
    *         operation:<br>
    *         OK(200) - A translation file in the requested format with
    *         translations for the requested document in a project, iteration
    *         and locale. <br>
    *         NOT FOUND(404) - If a document is not found with the given
    *         parameters.<br>
    *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
    *         the server while performing this operation.
    */
   @Override
   @GET
   @Path(FILE_DOWNLOAD_TEMPLATE)
   // /file/translation/{projectSlug}/{iterationSlug}/{locale}/{fileType}?docId={docId}
   public Response downloadTranslationFile( @PathParam("projectSlug") String projectSlug,
                                            @PathParam("iterationSlug") String iterationSlug,
                                            @PathParam("locale") String locale,
                                            @PathParam("fileType") String fileType,
                                            @QueryParam("docId") String docId )
   {
      // TODO scan (again) for virus
      final Response response;
      HDocument document = this.documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);

      if( document == null )
      {
         response = Response.status(Status.NOT_FOUND).build();
      }
      else if ("po".equals(fileType) || FILE_TYPE_OFFLINE_PO.equals(fileType))
      {
         // Note: could return 404 or Unsupported media type for "po" in non-po projects,
         //       and suggest to use offlinepo
         final Set<String> extensions = new HashSet<String>();
         extensions.add("gettext");
         extensions.add("comment");

         // Perform translation of Hibernate DTOs to JAXB DTOs
         TranslationsResource transRes = 
               (TranslationsResource) this.translatedDocResourceService.getTranslations(docId, new LocaleId(locale), extensions, true, null).getEntity();
         Resource res = this.resourceUtils.buildResource(document);

         StreamingOutput output = new POStreamingOutput(res, transRes, FILE_TYPE_OFFLINE_PO.equals(fileType));
         response = Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + ".po\"")
               .type(MediaType.TEXT_PLAIN)
               .entity(output).build();
      }
      else if (FILETYPE_TRANSLATED_APPROVED.equals(fileType) || FILETYPE_TRANSLATED_APPROVED_AND_FUZZY.equals(fileType) )
      {
         if (!translationFileServiceImpl.hasPersistedDocument(projectSlug, iterationSlug, document.getPath(), document.getName()))
         {
            return Response.status(Status.NOT_FOUND).build();
         }
         final Set<String> extensions = Collections.<String>emptySet();
         TranslationsResource transRes =
               (TranslationsResource) this.translatedDocResourceService.getTranslations(docId, new LocaleId(locale), extensions, true, null).getEntity();
         // Filter to only provide translated targets. "Preview" downloads include fuzzy.
         // New list is used as transRes list appears not to be a modifiable implementation.
         Map<String, TextFlowTarget> translations = new HashMap<String, TextFlowTarget>();
         boolean useFuzzy = FILETYPE_TRANSLATED_APPROVED_AND_FUZZY.equals(fileType);
         for (TextFlowTarget target : transRes.getTextFlowTargets())
         {
            // FIXME rhbz953734 - translatedDocResourceService will map review content state to old state.
            if (target.getState() == ContentState.Approved || (useFuzzy && target.getState() == ContentState.NeedReview))
            {
               translations.put(target.getResId(), target);
            }
         }

         HDocument hDocument = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);
         URI uri;
         try
         {
            File tempFile = translationFileServiceImpl.persistToTempFile(hDocument.getRawDocument().getContent().getBinaryStream());
            uri = tempFile.toURI();
         }
         catch (SQLException e)
         {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
         FileFormatAdapter adapter = translationFileServiceImpl.getAdapterFor(hDocument.getRawDocument().getType());
         String rawParamString = hDocument.getRawDocument().getAdapterParameters();
         Optional<String> params = Optional.<String>fromNullable(Strings.emptyToNull(rawParamString));
         StreamingOutput output = new FormatAdapterStreamingOutput(uri, translations, locale, adapter, params);
         response = Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + "\"")
               .entity(output).build();
      }
      else
      {
         response = Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
      }
      return response;
   }

   /**
    * Downloads a previously generated file.
    * 
    * @param downloadId The Zanata generated download id.
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - A translation file in the requested format with translations for the requested document in a
    * project, iteration and locale. <br>
    * NOT FOUND(404) - If a downloadable file is not found for the given id, or is not yet ready for download 
    * (i.e. the system is still preparing it).<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Path(DOWNLOAD_TEMPLATE)
   // /file/download/{downloadId}
   public Response download( @PathParam("downloadId") String downloadId )
   {
      // TODO scan (again) for virus
      try
      {
         // Check that the download exists by looking at the download descriptor
         Properties descriptorProps = this.fileSystemServiceImpl.findDownloadDescriptorProperties(downloadId);
         
         if( descriptorProps == null )
         {
            return Response.status(Status.NOT_FOUND).build(); 
         }
         else
         {
            File toDownload = this.fileSystemServiceImpl.findDownloadFile(downloadId);
            
            if( toDownload == null )
            {
               return Response.status(Status.NOT_FOUND).build(); 
            }
            else
            {
               return Response.ok()
                     .header("Content-Disposition", "attachment; filename=\"" 
                           + descriptorProps.getProperty(DownloadDescriptorProperties.DownloadFileName.toString()) 
                           + "\"")
                     .header("Content-Length", toDownload.length())
                     .entity( new FileStreamingOutput(toDownload) )
                     .build();
            }
         }
      }
      catch (IOException e)
      {
         return Response.serverError().status( Status.INTERNAL_SERVER_ERROR ).build();
      }
   }


   /*
    * Private class that implements PO file streaming of a document.
    */
   private class POStreamingOutput implements StreamingOutput
   {
      private Resource resource;
      private TranslationsResource transRes;
      private boolean offlinePo;

      /**
       * @param offlinePo true if text flow id should be inserted into msgctxt
       *                  to allow reverse mapping.
       */
      public POStreamingOutput( Resource resource, TranslationsResource transRes, boolean offlinePo)
      {
         this.resource = resource;
         this.transRes = transRes;
         this.offlinePo = offlinePo;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         PoWriter2 writer = new PoWriter2(false, offlinePo);
         writer.writePo(output, "UTF-8", this.resource, this.transRes);
      }
   }

   private class POTStreamingOutput implements StreamingOutput
   {
      private Resource resource;
      private boolean offlinePot;

      /**
       * @param offlinePot true if text flow id should be inserted into msgctxt
       *                   to allow reverse mapping
       */
      public POTStreamingOutput(Resource resource, boolean offlinePot)
      {
         this.resource = resource;
         this.offlinePot = offlinePot;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         PoWriter2 writer = new PoWriter2(false, offlinePot);
         writer.writePot(output, "UTF-8", resource);
      }
   }

   private class InputStreamStreamingOutput implements StreamingOutput
   {
      private InputStream input;

      public InputStreamStreamingOutput(InputStream input)
      {
         this.input = input;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         byte[] buffer = new byte[4096]; // To hold file contents
         int bytesRead; // How many bytes in buffer

         while ((bytesRead = input.read(buffer)) != -1)
         {
            output.write(buffer, 0, bytesRead);
         }
      }
   }

   private class FormatAdapterStreamingOutput implements StreamingOutput
   {
      private Map<String, TextFlowTarget> translations;
      private String locale;
      private URI original;
      private FileFormatAdapter adapter;
      private Optional<String> params;

      public FormatAdapterStreamingOutput(URI originalDoc, Map<String, TextFlowTarget> translations,
            String locale, FileFormatAdapter adapter, Optional<String> params)
      {
         this.translations = translations;
         this.locale = locale;
         this.original = originalDoc;
         this.adapter = adapter;
         this.params = params;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         adapter.writeTranslatedFile(output, original, translations, locale, params);
      }
   }

   /*
    * Private class that implements downloading from a previously prepared file. 
    */
   private class FileStreamingOutput implements StreamingOutput
   {
      private File file;

      public FileStreamingOutput( File file )
      {
         this.file = file;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         FileInputStream input = new FileInputStream(this.file);
         byte[] buffer = new byte[4096]; // To hold file contents
         int bytesRead; // How many bytes in buffer

         while ((bytesRead = input.read(buffer)) != -1)
         {
            output.write(buffer, 0, bytesRead);
         }
      }
   }

   // TODO move to FileServiceTest, and make a real unit test
   // idea: store an *encrypted EICAR* in the source, then decrypt it for use in the test
   // NB: don't add EICAR to git!
   public static void main(String[] args)
   {
      Stopwatch stop = new Stopwatch().start();
      virusScan(new File("/tmp/testdoc.odt"));
      System.out.println(stop);
      stop.reset();
      stop.start();
      try
      {
         virusScan(new File("/tmp/EICAR.com"));
      }
      catch (VirusDetectedException e)
      {
         System.out.println(e.getMessage());
      }
      System.out.println(stop);
   }
}
