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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.jboss.seam.security.AuthorizationException;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.VirusScanner;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;

import com.google.common.base.Optional;

@Slf4j
public class TranslationDocumentUpload extends DocumentUpload
{

   public TranslationDocumentUpload(ZanataIdentity identity,
         Session session,
         DocumentDAO documentDAO,
         ProjectIterationDAO projectIterationDAO,
         DocumentService documentServiceImpl,
         VirusScanner virusScanner,
         TranslationFileService translationFileServiceImpl)
   {
      super(identity,
            session,
            documentDAO,
            projectIterationDAO,
            documentServiceImpl,
            virusScanner,
            translationFileServiceImpl);
   }

   public static Response tryUploadTranslationFile(String projectSlug, String iterationSlug, String docId,
         String localeId, String mergeType, DocumentFileUploadForm uploadForm,
         ZanataIdentity identity,
         ProjectIterationDAO projectIterationDAO,
         Session session,
         DocumentDAO documentDAO,
         LocaleDAO localeDAO,
         TranslationFileService translationFileServiceImpl,
         TranslationService translationServiceImpl)
   {
      HLocale locale;
      try
      {
         checkTranslationUploadPreconditions(projectSlug, iterationSlug, docId, localeId, uploadForm,
               identity, projectIterationDAO, session, documentDAO, translationFileServiceImpl);
         locale = findHLocale(localeId, localeDAO);
         checkTranslationUploadAllowed(projectSlug, iterationSlug, localeId, locale,
               projectIterationDAO, identity);

         Optional<File> tempFile;
         int totalChunks;

         if (isSinglePart(uploadForm))
         {
            totalChunks = 1;
            tempFile = Optional.<File>absent();
         }
         else
         {
            HDocumentUpload upload = saveUploadPart(new GlobalDocumentId(projectSlug, iterationSlug, docId), locale,
                  uploadForm, session, projectIterationDAO);
            totalChunks = upload.getParts().size();
            if (!uploadForm.getLast())
            {
               return Response.status(Status.ACCEPTED)
                     .entity(new ChunkUploadResponse(upload.getId(), totalChunks, true,
                           "Chunk accepted, awaiting remaining chunks."))
                     .build();
            }
            tempFile = Optional.of(combineToTempFileAndDeleteUploadRecord(upload, session, translationFileServiceImpl));
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

   public static void checkTranslationUploadPreconditions(String projectSlug, String iterationSlug,
         String docId, String localeId, DocumentFileUploadForm uploadForm,
         ZanataIdentity identity,
         ProjectIterationDAO projectIterationDAO,
         Session session,
         DocumentDAO documentDAO,
         TranslationFileService translationFileServiceImpl)
   {
      checkUploadPreconditions(new GlobalDocumentId(projectSlug, iterationSlug, docId),
            uploadForm, identity, projectIterationDAO, session);

      // TODO check translation upload allowed

      checkDocumentExists(projectSlug, iterationSlug, docId, uploadForm, documentDAO);
      checkValidTranslationUploadType(uploadForm, translationFileServiceImpl);
   }

   public static void checkDocumentExists(String projectSlug, String iterationSlug, String docId,
         DocumentFileUploadForm uploadForm,
         DocumentDAO documentDAO)
   {
      if (isNewDocument(new GlobalDocumentId(projectSlug, iterationSlug, docId), documentDAO))
      {
         throw new ChunkUploadException(Status.NOT_FOUND,
               "No document with id \"" + docId + "\" exists in project-version \"" +
               projectSlug + ":" + iterationSlug + "\".");
      }
   }

   public static void checkValidTranslationUploadType(DocumentFileUploadForm uploadForm,
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

   public static HLocale findHLocale(String localeString, LocaleDAO localeDAO)
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

   public static void checkTranslationUploadAllowed(String projectSlug, String iterationSlug, String localeId, HLocale locale,
         ProjectIterationDAO projectIterationDAO,
         ZanataIdentity identity)
   {
      if (!isTranslationUploadAllowed(projectSlug, iterationSlug, locale,
            projectIterationDAO, identity))
      {
         throw new ChunkUploadException(Status.FORBIDDEN,
               "You do not have permission to upload translations for locale \"" + localeId +
               "\" to project-version \"" + projectSlug + ":" + iterationSlug + "\".");
      }
   }

   public static boolean isTranslationUploadAllowed(String projectSlug, String iterationSlug, HLocale localeId,
         ProjectIterationDAO projectIterationDAO,
         ZanataIdentity identity)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      // TODO should this check be "add-translation" or "modify-translation"?
      // They appear to be granted identically at the moment.
      return projectIteration.getStatus() == EntityStatus.ACTIVE && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("add-translation", projectIteration.getProject(), localeId);
   }

   public static Set<String> newExtensions(boolean gettextExtensions)
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

   public static Response transUploadResponse(int totalChunks, List<String> warnings)
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

   public static String buildWarningString(List<String> warnings)
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

   public static MergeType mergeTypeFromString(String type)
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

}
