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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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


import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.files.DocumentFileUploadForm;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.FileSystemService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.service.FileSystemService.DownloadDescriptorProperties;

@Name("fileService")
@Path(FileResource.FILE_RESOURCE)
@Produces( { MediaType.APPLICATION_OCTET_STREAM })
@Consumes( { MediaType.APPLICATION_OCTET_STREAM })
public class FileService implements FileResource
{
   public static final String SOURCE_DOWNLOAD_TEMPLATE = "/source/{projectSlug}/{iterationSlug}/{fileType}";

   public static final String SOURCE_UPLOAD_TEMPLATE = "/source/{projectSlug}/{iterationSlug}";
   public static final String TRANSLATION_UPLOAD_TEMPLATE = "/translation/{projectSlug}/{iterationSlug}/{locale}";

   private static final String RAW_DOCUMENT = "raw";
   private static final String SUBSTITUTE_APPROVED_AND_FUZZY = "half-baked";
   private static final String SUBSTITUTE_APPROVED = "baked";

   @Logger
   private Log log;

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



   @POST
   @Path(SOURCE_UPLOAD_TEMPLATE)
   @Consumes( MediaType.MULTIPART_FORM_DATA)
   public Response uploadSourceFile( @PathParam("projectSlug") String projectSlug,
                                     @PathParam("iterationSlug") String iterationSlug,
                                     @QueryParam("docId") String docId,
                                     @MultipartForm DocumentFileUploadForm uploadForm )
   {
      // TODO extract method for checks common to source and translation upload
      Response errorResponse = checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);
      if (errorResponse != null)
      {
         return errorResponse;
      }

      if (!isDocumentUploadAllowed(projectSlug, iterationSlug))
      {
         return Response.status(Status.FORBIDDEN)
               .entity("You do not have permission to upload source documents for this project-version\n")
               .build();
      }

      String fileType = uploadForm.getFileType();
      boolean isPotFile = fileType.equals(".pot");
      if (!isPotFile && !translationFileServiceImpl.hasAdapterFor(fileType))
      {
         return Response.status(Status.BAD_REQUEST)
               .entity("The type \"" + fileType + "\" specified in form parameter 'type' is not valid for a source file on this server.\n")
               .build();
      }

      boolean isNewDocument = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId) == null;

      boolean isSinglePart = uploadForm.getFirst() && uploadForm.getLast();

      if (isSinglePart && isPotFile)
      {
         parsePotFile(uploadForm.getFileStream(), docId, fileType, projectSlug, iterationSlug);
         return sourceUploadSuccessResponse(isNewDocument);
      }

      // persist bytes to file
      File tempFile = null;
      if (uploadForm.getFirst())
      {
         try
         {
            tempFile = translationFileServiceImpl.persistToTempFile(uploadForm.getFileStream());
         }
         catch (ZanataServiceException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
      }
      else
      {
         // TODO append to existing temp file
         return Response.status(Status.fromStatusCode(501))
               .entity("Multiple part file upload is not yet implemented. Send entire file with first=true and last=true.\n")
               .build();
      }

      if (!uploadForm.getLast())
      {
         return Response.status(Status.ACCEPTED)
               .entity("File part accepted, awaiting remaining parts.\n")
               .build();
      }

      // have entire file, proceed with parsing
      if (isPotFile)
      {
         try
         {
            parsePotFile(new FileInputStream(tempFile), docId, fileType, projectSlug, iterationSlug);
         }
         catch (FileNotFoundException e)
         {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                  .entity(e)
                  .build();
         }
         translationFileServiceImpl.removeTempFile(tempFile);
         return sourceUploadSuccessResponse(isNewDocument);
      }

      // must be adapter file

      try {
         Resource doc = translationFileServiceImpl.parseUpdatedDocumentFile(tempFile.toURI(), docId, fileType);
         doc.setLang( new LocaleId("en-US") );
         // TODO Copy Trans values
         documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, Collections.<String>emptySet(), false);
      }
      catch (SecurityException e)
      {
         return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
      }
      catch (ZanataServiceException e)
      {
         return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
      }

      try
      {
         persistSourceDocument(projectSlug, iterationSlug, docId, tempFile);
      }
      catch (FileNotFoundException e)
      {
         return Response.status(Status.INTERNAL_SERVER_ERROR)
               .entity("Error saving uploaded document on server, download in original format may fail.\n")
               .build();
      }
      catch (ZanataServiceException e)
      {
         return Response.status(Status.INTERNAL_SERVER_ERROR)
               .entity("Error saving uploaded document on server, download in original format may fail.\n")
               .build();
      }
      finally
      {
         translationFileServiceImpl.removeTempFile(tempFile);
      }

      return sourceUploadSuccessResponse(isNewDocument);
   }

   private Response checkUploadPreconditions(String projectSlug, String iterationSlug, String docId, DocumentFileUploadForm uploadForm)
   {
      if (!identity.isLoggedIn())
      {
         return Response.status(Status.UNAUTHORIZED)
               .entity("Valid combination of username and api-key for this server were not included in the request\n")
               .build();
      }
      if (docId == null || docId.length() == 0)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity("Required query string parameter 'docId' was not found.\n")
               .build();
      }

      if (uploadForm.getFileStream() == null)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity("Required form parameter 'file' containing file content was not found.\n")
               .build();
      }

      if (uploadForm.getFirst() == null || uploadForm.getLast() == null)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity("Form parameters 'first' and 'last' must both be provided.\n")
               .build();
      }

      String fileType = uploadForm.getFileType();
      if (fileType == null || fileType.length() == 0)
      {
         return Response.status(Status.PRECONDITION_FAILED)
               .entity("Required form parameter 'type' was not found.\n")
               .build();
      }

      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      if (projectIteration == null)
      {
         return Response.status(Status.NOT_FOUND)
               .entity("The specified project-version does not exist on this server.")
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

   private void parsePotFile(InputStream documentStream, String docId, String fileType, String projectSlug, String iterationSlug)
   {
      Resource doc;
      doc = translationFileServiceImpl.parseUpdatedDocumentFile(documentStream, docId, fileType);
      doc.setLang( new LocaleId("en-US") );
      // TODO Copy Trans values
      documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, new StringSet(ExtensionType.GetText.toString()), false);
   }

   private void persistSourceDocument(String projectSlug, String iterationSlug, String docId, File tempFile) throws FileNotFoundException
   {
      String documentPath = "";
      String docName = docId;
      if (docId.contains("/"))
      {
         documentPath = docId.substring(0, docId.lastIndexOf('/'));
         if (!docId.endsWith("/"))
         {
            docName = docId.substring(docId.lastIndexOf('/') + 1);
         }
      }

      translationFileServiceImpl.persistDocument(new FileInputStream(tempFile), projectSlug, iterationSlug, documentPath, docName);
   }


   private Response sourceUploadSuccessResponse(boolean isNewDocument)
   {
      Response response;
      if (isNewDocument)
      {
         response = Response.status(Status.CREATED)
               .entity("Upload of new source document successful.\n")
               .build();
      }
      else
      {
         response = Response.status(Status.OK)
               .entity("Upload of new version of source document successful.\n")
               .build();
      }
      return response;
   }


   @POST
   @Path(TRANSLATION_UPLOAD_TEMPLATE)
   @Consumes( MediaType.MULTIPART_FORM_DATA)
   public Response uploadTranslationFile( @PathParam("projectSlug") String projectSlug,
                                          @PathParam("iterationSlug") String iterationSlug,
                                          @PathParam("locale") String locale,
                                          @QueryParam("docId") String docId,
                                          @QueryParam("merge") String merge,
                                          @MultipartForm DocumentFileUploadForm uploadForm )
   {
      Response errorResponse = checkUploadPreconditions(projectSlug, iterationSlug, docId, uploadForm);
      if (errorResponse != null)
      {
         return errorResponse;
      }

      HLocale localeId = localeDAO.findByLocaleId(new LocaleId(locale));
      if (localeId == null)
      {
         return Response.status(Status.NOT_FOUND)
               .entity("The specified locale does not exist on this server\n")
               .build();
      }

      if (!isTranslationUploadAllowed(projectSlug, iterationSlug, localeId))
      {
         return Response.status(Status.FORBIDDEN)
               .entity("You do not have permission to upload translations for the specified locale to this project-version\n")
               .build();
      }

      if (documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId) == null)
      {
         return Response.status(Status.NOT_FOUND)
               .entity("No document with the specified id exists in this project-version\n")
               .build();
      }


      String fileType = uploadForm.getFileType();
      boolean isPoFile = fileType.equals(".po");
      if (!isPoFile && !translationFileServiceImpl.hasAdapterFor(fileType))
      {
         return Response.status(Status.BAD_REQUEST)
               .entity("The type \"" + fileType + "\" specified in form parameter 'type' is not valid for a translation file on this server.\n")
               .build();
      }

      // TODO useful error messages for failed parsing
      TranslationsResource transRes = translationFileServiceImpl.parseTranslationFile(uploadForm.getFileStream(),
            uploadForm.getFileType(), locale);

      MergeType mergeType;
      if ("import".equals(merge))
      {
         log.info("merge type import");
         mergeType = MergeType.IMPORT;
      }
      else
      {
         log.info("merge type: " + merge);
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
            docId, localeId.getLocaleId(), transRes, extensions, mergeType);

      if (warnings != null && !warnings.isEmpty())
      {
         StringBuilder entity = new StringBuilder("Upload succeeded but had the following warnings:");
         for (String warning : warnings)
         {
            entity.append("\n\t");
            entity.append(warning);
         }
         entity.append("\n");
         return Response.status(Status.OK).entity(entity.toString()).build();
      }
      return Response.status(Status.OK).entity("Translations uploaded successfully\n").build();
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
   @GET
   @Path(SOURCE_DOWNLOAD_TEMPLATE)
   // /file/source/{projectSlug}/{iterationSlug}/{fileType}?docId={docId}
   public Response downloadSourceFile( @PathParam("projectSlug") String projectSlug,
                                       @PathParam("iterationSlug") String iterationSlug,
                                       @PathParam("fileType") String fileType,
                                       @QueryParam("docId") String docId)
   {
      final Response response;
      HDocument document = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);

      if( document == null )
      {
         response = Response.status(Status.NOT_FOUND).build();
      }
      else if (RAW_DOCUMENT.equals(fileType) && translationFileServiceImpl.hasPersistedDocument(projectSlug, iterationSlug, document.getPath(), document.getName()))
      {
         InputStream fileContents = translationFileServiceImpl.streamDocument(projectSlug, iterationSlug, document.getPath(), document.getName());
         StreamingOutput output = new InputStreamStreamingOutput(fileContents);
         response = Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + "\"")
               .entity(output).build();
      }
      else if ("pot".equals(fileType))
      {
         Resource res = resourceUtils.buildResource(document);
         StreamingOutput output = new POTStreamingOutput(res);
         response = Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + ".pot\"")
               .entity(output).build();
      }
      else
      {
         response = Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
      }
      return response;
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
      final Response response;
      HDocument document = this.documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);

      if( document == null )
      {
         response = Response.status(Status.NOT_FOUND).build();
      }
      else if ("po".equals(fileType))
      {
         final Set<String> extensions = new HashSet<String>();
         extensions.add("gettext");
         extensions.add("comment");

         // Perform translation of Hibernate DTOs to JAXB DTOs
         TranslationsResource transRes = 
               (TranslationsResource) this.translatedDocResourceService.getTranslations(docId, new LocaleId(locale), extensions, true).getEntity();
         Resource res = this.resourceUtils.buildResource(document);

         StreamingOutput output = new POStreamingOutput(res, transRes);
         response = Response.ok()
               .header("Content-Disposition", "attachment; filename=\"" + document.getName() + ".po\"")
               .entity(output).build();
      }
      else if ( (SUBSTITUTE_APPROVED.equals(fileType) || SUBSTITUTE_APPROVED_AND_FUZZY.equals(fileType))
            && translationFileServiceImpl.hasPersistedDocument(projectSlug, iterationSlug, document.getPath(), document.getName()))
      {
         final Set<String> extensions = Collections.<String>emptySet();
         TranslationsResource transRes = 
               (TranslationsResource) this.translatedDocResourceService.getTranslations(docId, new LocaleId(locale), extensions, true).getEntity();
         // Filter to only provide translated targets. "Preview" downloads include fuzzy.
         // Using new list is used as transRes list appears not to be a modifiable implementation.
         Map<String, TextFlowTarget> translations = new HashMap<String, TextFlowTarget>();
         boolean useFuzzy = SUBSTITUTE_APPROVED_AND_FUZZY.equals(fileType);
         for (TextFlowTarget target : transRes.getTextFlowTargets())
         {
            if (target.getState() == ContentState.Approved || (useFuzzy && target.getState() == ContentState.NeedReview))
            {
               translations.put(target.getResId(), target);
            }
         }

         URI uri = translationFileServiceImpl.getDocumentURI(projectSlug, iterationSlug, document.getPath(), document.getName());
         StreamingOutput output = new FormatAdapterStreamingOutput(uri, translations, locale, translationFileServiceImpl.getAdapterFor(docId));
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

      public POStreamingOutput( Resource resource, TranslationsResource transRes )
      {
         this.resource = resource;
         this.transRes = transRes;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {         
         PoWriter2 writer = new PoWriter2();
         writer.writePo(output, "UTF-8", this.resource, this.transRes);
      }
   }

   private class POTStreamingOutput implements StreamingOutput
   {
      private Resource resource;

      public POTStreamingOutput(Resource resource)
      {
         this.resource = resource;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         PoWriter2 writer = new PoWriter2();
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

      public FormatAdapterStreamingOutput(URI originalDoc, Map<String, TextFlowTarget> translations, String locale, FileFormatAdapter adapter)
      {
         this.translations = translations;
         this.locale = locale;
         this.original = originalDoc;
         this.adapter = adapter;
      }

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
         adapter.writeTranslatedFile(output, original, translations, locale);
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
}
