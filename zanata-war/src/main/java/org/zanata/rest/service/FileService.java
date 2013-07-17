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
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.AuthorizationException;
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
import org.zanata.file.DocumentUpload;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
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
      GlobalDocumentId id = new GlobalDocumentId(projectSlug, iterationSlug, docId);

      SourceDocumentUpload uploader = new SourceDocumentUpload(identity, session, documentDAO, projectIterationDAO,
            documentServiceImpl, virusScanner, translationFileServiceImpl);
      return uploader.tryUploadSourceFile(id, uploadForm);
   }

   // TODO this shares a lot of logic with .tryUploadSourceFile(), try to unify.
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
         checkTranslationUploadPreconditions(projectSlug, iterationSlug, docId, localeId, uploadForm,
               identity, projectIterationDAO, session, documentDAO, translationFileServiceImpl);
         locale = findHLocale(localeId);
         checkTranslationUploadAllowed(projectSlug, iterationSlug, localeId, locale);

         Optional<File> tempFile;
         int totalChunks;

         if (DocumentUpload.isSinglePart(uploadForm))
         {
            totalChunks = 1;
            tempFile = Optional.<File>absent();
         }
         else
         {
            HDocumentUpload upload = DocumentUpload.saveUploadPart(new GlobalDocumentId(projectSlug, iterationSlug, docId), locale,
                  uploadForm, session, projectIterationDAO);
            totalChunks = upload.getParts().size();
            if (!uploadForm.getLast())
            {
               return Response.status(Status.ACCEPTED)
                     .entity(new ChunkUploadResponse(upload.getId(), totalChunks, true,
                           "Chunk accepted, awaiting remaining chunks."))
                     .build();
            }
            tempFile = Optional.of(DocumentUpload.combineToTempFileAndDeleteUploadRecord(upload, session, translationFileServiceImpl));
         }

         TranslationsResource transRes;
         if (uploadForm.getFileType().equals(".po"))
         {
            InputStream poStream = DocumentUpload.getInputStream(tempFile, uploadForm);
            transRes = translationFileServiceImpl.parsePoFile(poStream, projectSlug, iterationSlug, docId);
         }
         else
         {
            if (!tempFile.isPresent())
            {
               tempFile = Optional.of(DocumentUpload.persistTempFileFromUpload(uploadForm, translationFileServiceImpl));
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

   private static void checkTranslationUploadPreconditions(String projectSlug, String iterationSlug,
         String docId, String localeId, DocumentFileUploadForm uploadForm,
         ZanataIdentity identity,
         ProjectIterationDAO projectIterationDAO,
         Session session,
         DocumentDAO documentDAO,
         TranslationFileService translationFileServiceImpl)
   {
      DocumentUpload.checkUploadPreconditions(new GlobalDocumentId(projectSlug, iterationSlug, docId),
            uploadForm, identity, projectIterationDAO, session);

      // TODO check translation upload allowed

      checkDocumentExists(projectSlug, iterationSlug, docId, uploadForm, documentDAO);
      checkValidTranslationUploadType(uploadForm, translationFileServiceImpl);
   }

   private static void checkValidTranslationUploadType(DocumentFileUploadForm uploadForm,
         TranslationFileService translationFileServiceImpl)
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

   private static void checkDocumentExists(String projectSlug, String iterationSlug, String docId,
         DocumentFileUploadForm uploadForm,
         DocumentDAO documentDAO)
   {
      if (DocumentUpload.isNewDocument(new GlobalDocumentId(projectSlug, iterationSlug, docId), documentDAO))
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
