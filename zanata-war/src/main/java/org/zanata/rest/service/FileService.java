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

import javax.annotation.Nonnull;
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

import org.hibernate.Criteria;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.AuthorizationException;
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
import org.zanata.exception.ChunkUploadException;
import org.zanata.exception.HashMismatchException;
import org.zanata.exception.VirusDetectedException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.file.GlobalDocumentId;
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
import com.google.common.base.Strings;

import com.google.common.io.ByteStreams;

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

   @In
   private VirusScanner virusScanner;

   // FIXME remove when using DAO for HDocumentUpload
   @In
   private Session session;
   private static final HLocale NULL_LOCALE = null;

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
      return tryUploadSourceFile(projectSlug, iterationSlug, docId, uploadForm);
   }

   private Response tryUploadSourceFile(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
   {
      try
      {
         GlobalDocumentId id = new GlobalDocumentId(projectSlug, iterationSlug, docId);
         checkSourceUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);

         Optional<File> tempFile;
         int totalChunks;

         if (!uploadForm.getLast())
         {
            HDocumentUpload upload = saveUploadPart(id.getProjectSlug(), id.getVersionSlug(), id.getDocId(), NULL_LOCALE, uploadForm);
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
            HDocumentUpload upload = saveUploadPart(projectSlug, iterationSlug, docId, NULL_LOCALE, uploadForm);
            totalChunks = upload.getParts().size();
            tempFile = Optional.of(combineToTempFileAndDeleteUploadRecord(upload));
         }

         if (uploadForm.getFileType().equals(".pot"))
         {
            InputStream potStream = getInputStream(tempFile, uploadForm);
            parsePotFile(potStream, projectSlug, iterationSlug, docId, uploadForm);
         }
         else
         {
            if (!tempFile.isPresent())
            {
               tempFile = Optional.of(persistTempFileFromUpload(uploadForm));
            }
            processAdapterFile(tempFile.get(), projectSlug, iterationSlug, docId, uploadForm);
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

   private static InputStream getInputStream(Optional<File> tempFile, DocumentFileUploadForm uploadForm) throws FileNotFoundException
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

   private void checkSourceUploadPreconditions(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
   {
      try
      {
         checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);
         checkSourceUploadAllowed(projectSlug, iterationSlug);
      }
      catch (AuthorizationException e)
      {
         throw new ChunkUploadException(Status.UNAUTHORIZED, e.getMessage());
      }
      checkValidSourceUploadType(uploadForm);
   }

   private void checkSourceUploadAllowed(String projectSlug, String iterationSlug)
   {
      if (!isDocumentUploadAllowed(projectSlug, iterationSlug))
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "You do not have permission to upload source documents to project-version \""
               + projectSlug + ":" + iterationSlug + "\".");
      }
   }

   private void checkValidSourceUploadType(DocumentFileUploadForm uploadForm)
   {
      if (!uploadForm.getFileType().equals(".pot")
            && !translationFileServiceImpl.hasAdapterFor(DocumentType.typeFor(uploadForm.getFileType())))
      {
         throw new ChunkUploadException(Status.BAD_REQUEST,
               "The type \"" + uploadForm.getFileType() + "\" specified in form parameter 'type' "
               + "is not valid for a source file on this server.");
      }
   }

   private File persistTempFileFromUpload(DocumentFileUploadForm uploadForm)
   {
      File tempFile;
      try
      {
         MessageDigest md = MessageDigest.getInstance("MD5");
         InputStream fileContents = new DigestInputStream(uploadForm.getFileStream(), md);
         tempFile = translationFileServiceImpl.persistToTempFile(fileContents);
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

   private void processAdapterFile(@Nonnull File tempFile, String projectSlug, String iterationSlug,
         String docId, DocumentFileUploadForm uploadForm)
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

   private File combineToTempFileAndDeleteUploadRecord(HDocumentUpload upload)
   {
      File tempFile;
      try
      {
         tempFile = combineToTempFile(upload, translationFileServiceImpl);
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

   private static boolean isSinglePart(DocumentFileUploadForm uploadForm)
   {
      return uploadForm.getFirst() && uploadForm.getLast();
   }

   private boolean useOfflinePo(String projectSlug, String iterationSlug, String docId)
   {
      return !isNewDocument(projectSlug, iterationSlug, docId, documentDAO) && !translationFileServiceImpl.isPoDocument(projectSlug, iterationSlug, docId);
   }

   private static boolean isNewDocument(String projectSlug, String iterationSlug, String docId, DocumentDAO dao)
   {
      return dao.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId) == null;
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

   private HDocumentUpload retrieveUploadObject(DocumentFileUploadForm uploadForm)
   {
      // TODO put in DAO
      Criteria criteria = session.createCriteria(HDocumentUpload.class);
      criteria.add(Restrictions.idEq(uploadForm.getUploadId()));
      HDocumentUpload upload = (HDocumentUpload) criteria.uniqueResult();
      return upload;
   }

   private HDocumentUpload createMultipartUpload(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm, HLocale locale)
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

   private void saveUploadPart(DocumentFileUploadForm uploadForm, HDocumentUpload upload)
   {
      Blob partContent = session.getLobHelper().createBlob(uploadForm.getFileStream(), uploadForm.getSize().intValue());
      HDocumentUploadPart newPart = new HDocumentUploadPart();
      newPart.setContent(partContent);
      upload.getParts().add(newPart);
      session.saveOrUpdate(upload);
      session.flush();
   }

   private void checkUploadPreconditions(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
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

         HDocumentUpload upload = retrieveUploadObject(uploadForm);
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

   private boolean isDocumentUploadAllowed(String projectSlug, String iterationSlug)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      return projectIteration.getStatus() == EntityStatus.ACTIVE && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("import-template", projectIteration);
   }

   private void parsePotFile(InputStream potStream, String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
   {
      parsePotFile(potStream, docId, uploadForm.getFileType(), projectSlug, iterationSlug, useOfflinePo(projectSlug, iterationSlug, docId));
   }

   private void parsePotFile(InputStream documentStream, String docId, String fileType, String projectSlug, String iterationSlug, boolean asOfflinePo)
   {
      Resource doc;
      doc = translationFileServiceImpl.parseUpdatedPotFile(documentStream, docId, fileType, asOfflinePo);
      doc.setLang( new LocaleId("en-US") );
      // TODO Copy Trans values
      documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, new StringSet(ExtensionType.GetText.toString()), false);
   }

   private static Response sourceUploadSuccessResponse(boolean isNewDocument, int acceptedChunks)
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

   // TODO this shares a lot of logic with .uploadSourceFile(), try to unify.
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
      return tryUploadTranslationFile(projectSlug, iterationSlug, docId, localeId, merge, uploadForm);
   }

   private Response tryUploadTranslationFile(String projectSlug, String iterationSlug, String docId, String localeId, String mergeType, DocumentFileUploadForm uploadForm)
   {
      HLocale locale;
      try
      {
         checkTranslationUploadPreconditions(projectSlug, iterationSlug, docId, localeId, uploadForm);
         locale = findHLocale(localeId);
         checkTranslationUploadAllowed(projectSlug, iterationSlug, localeId, locale);

         Optional<File> tempFile;
         int totalChunks;

         if (isSinglePart(uploadForm))
         {
            totalChunks = 1;
            tempFile = Optional.<File>absent();
         }
         else
         {
            HDocumentUpload upload = saveUploadPart(projectSlug, iterationSlug, docId, locale, uploadForm);
            totalChunks = upload.getParts().size();
            if (!uploadForm.getLast())
            {
               return Response.status(Status.ACCEPTED)
                     .entity(new ChunkUploadResponse(upload.getId(), totalChunks, true,
                           "Chunk accepted, awaiting remaining chunks."))
                     .build();
            }
            tempFile = Optional.of(combineToTempFileAndDeleteUploadRecord(upload));
         }

         TranslationsResource transRes;
         if (uploadForm.getFileType().equals(".po"))
         {
            InputStream poStream = getInputStream(tempFile, uploadForm);
            transRes = translationFileServiceImpl.parsePoFile(poStream, projectSlug, iterationSlug, docId);
         }
         else
         {
            if (!tempFile.isPresent())
            {
               tempFile = Optional.of(persistTempFileFromUpload(uploadForm));
            }
            // FIXME this is misusing the 'filename' field. the method should probably take a
            // type anyway
            transRes = translationFileServiceImpl.parseAdapterTranslationFile(tempFile.get(),
                  projectSlug, iterationSlug, docId, localeId, uploadForm.getFileType());
         }
         if (tempFile.isPresent())
         {
            tempFile.get().delete();
         }

         Set<String> extensions = newExtensions(uploadForm.getFileType().equals(".po"));
         // TODO useful error message for failed saving?
         List<String> warnings = translationServiceImpl.translateAllInDoc(projectSlug, iterationSlug,
               docId, locale.getLocaleId(), transRes, extensions, mergeTypeFromString(mergeType));

         return transUploadResponse(totalChunks, warnings);
      }
      catch (AuthorizationException e)
      {
         return Response.status(Status.UNAUTHORIZED)
               .entity(new ChunkUploadResponse(e.getMessage()))
               .build();
      }
      catch (FileNotFoundException e)
      {
         log.error("failed to create input stream from temp file", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR)
               .entity(e).build();
      }
      catch (ChunkUploadException e)
      {
         return Response.status(e.getStatusCode())
               .entity(new ChunkUploadResponse(e.getMessage()))
               .build();
      }
   }

   private Response transUploadResponse(int totalChunks, List<String> warnings)
   {
      ChunkUploadResponse response = new ChunkUploadResponse();
      response.setExpectingMore(false);
      response.setAcceptedChunks(totalChunks);
      if (warnings != null && !warnings.isEmpty())
      {
         response.setSuccessMessage(buildWarningString(warnings));
      }
      else
      {
         response.setSuccessMessage("Translations uploaded successfully");
      }
      return Response.status(Status.OK).entity(response).build();
   }

   private String buildWarningString(List<String> warnings)
   {
      StringBuilder warningText = new StringBuilder("Upload succeeded but had the following warnings:");
      for (String warning : warnings)
      {
         warningText.append("\n\t");
         warningText.append(warning);
      }
      warningText.append("\n");
      String warningString = warningText.toString();
      return warningString;
   }

   private Set<String> newExtensions(boolean gettextExtensions)
   {
      Set<String> extensions;
      if (gettextExtensions)
      {
         extensions = new StringSet(ExtensionType.GetText.toString());
      }
      else
      {
         extensions = Collections.<String>emptySet();
      }
      return extensions;
   }

   private HDocumentUpload saveUploadPart(String projectSlug, String iterationSlug, String docId, HLocale locale, DocumentFileUploadForm uploadForm)
   {
      HDocumentUpload upload;
      if (uploadForm.getFirst())
      {
         upload = createMultipartUpload(projectSlug, iterationSlug, docId, uploadForm, locale);
      }
      else
      {
         upload = retrieveUploadObject(uploadForm);
      }
      saveUploadPart(uploadForm, upload);
      return upload;
   }

   private void checkTranslationUploadAllowed(String projectSlug, String iterationSlug, String localeId, HLocale locale)
   {
      if (!isTranslationUploadAllowed(projectSlug, iterationSlug, locale))
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "You do not have permission to upload translations for locale \"" + localeId +
               "\" to project-version \"" + projectSlug + ":" + iterationSlug + "\".");
      }
   }

   private boolean isTranslationUploadAllowed(String projectSlug, String iterationSlug, HLocale localeId)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      // TODO should this check be "add-translation" or "modify-translation"?
      // They appear to be granted identically at the moment.
      return projectIteration.getStatus() == EntityStatus.ACTIVE && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("add-translation", projectIteration.getProject(), localeId);
   }

   private HLocale findHLocale(String localeString)
   {
      LocaleId localeId;
      try
      {
         localeId = new LocaleId(localeString);
      }
      catch (IllegalArgumentException e)
      {
         throw new ChunkUploadException(Status.BAD_REQUEST,
               "Invalid value for locale", e);
      }

      HLocale locale = localeDAO.findByLocaleId(localeId);
      if (locale == null)
      {
         throw new ChunkUploadException(Status.NOT_FOUND,
               "The specified locale \"" + localeString + "\" does not exist on this server.");
      }
      return locale;
   }

   private void checkTranslationUploadPreconditions(String projectSlug, String iterationSlug, String docId, String localeId, DocumentFileUploadForm uploadForm)
   {
      checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);

      // TODO check translation upload allowed

      checkDocumentExists(projectSlug, iterationSlug, docId, uploadForm);
      checkValidTranslationUploadType(uploadForm);
   }

   private void checkValidTranslationUploadType(DocumentFileUploadForm uploadForm)
   {
      String fileType = uploadForm.getFileType();
      if (!fileType.equals(".po")
            && !translationFileServiceImpl.hasAdapterFor(DocumentType.typeFor(fileType)))
      {
         throw new ChunkUploadException(Status.BAD_REQUEST,
               "The type \"" + fileType + "\" specified in form parameter 'type' " +
               "is not valid for a translation file on this server.");
      }
   }

   private void checkDocumentExists(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
   {
      if (isNewDocument(projectSlug, iterationSlug, docId, documentDAO))
      {
         throw new ChunkUploadException(Status.NOT_FOUND, 
               "No document with id \"" + docId + "\" exists in project-version \"" +
               projectSlug + ":" + iterationSlug + "\".");
      }
   }

   private MergeType mergeTypeFromString(String type)
   {
      if ("import".equals(type))
      {
         return MergeType.IMPORT;
      }
      else
      {
         return MergeType.AUTO;
      }
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
            // TODO rhbz953734 - translatedDocResourceService will map review content state to old state. For now this is acceptable. Once we have new REST options, we should review this
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
            String name = projectSlug+":"+iterationSlug+":"+docId;
            virusScanner.scan(tempFile, name);
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
      public void write(@SuppressWarnings("null") @Nonnull OutputStream output) throws IOException, WebApplicationException
      {
         FileInputStream input = new FileInputStream(this.file);
         try
         {
            ByteStreams.copy(input, output);
         }
         finally
         {
            input.close();
         }
      }
   }

}
