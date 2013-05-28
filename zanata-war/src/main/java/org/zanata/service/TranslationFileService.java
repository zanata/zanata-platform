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
package org.zanata.service;

import org.zanata.adapter.FileFormatAdapter;
import org.zanata.common.DocumentType;
import org.zanata.exception.ZanataServiceException;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.google.common.base.Optional;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

/**
 * Provides basic services to transform and process translation files.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface TranslationFileService
{
   /**
    * Extract the translated strings from a document file to a usable form, using appropriate
    * id mapping.
    * 
    * @param fileContents the document to parse
    * @param fileName the name including extension for the file (used to determine how to parse file)
    * @return a representation of the translations
    * @throws ZanataServiceException if there is no adapter available for the
    *            document format, or there is an error during parsing
    */
   TranslationsResource parseTranslationFile(InputStream fileContents, String fileName,
         String localeId, String projectSlug, String iterationSlug, String docId)
               throws ZanataServiceException;

   /**
    * Extract the translatable strings from a new document file or from a new version of an existing
    * document file to a usable form.
    * 
    * @param fileContents
    * @param docId the id of an existing document
    * @param uploadFileName name of the new file being parsed, used only to identify format
    * @param offlinePo true to use msgctxt as the id for each text flow
    * @return a usable representation of the document
    */
   Resource parseUpdatedPotFile(InputStream fileContents, String docId, String uploadFileName, boolean offlinePo);

   /**
    * Extracts the translatable strings from a document file to a usable form.
    * 
    * @param documentFile location of the document to parse
    * @param path to use within the Zanata project-iteration
    * @param fileName to use within the Zanata project-iteration
    * @param params adapter-specific parameter string. See documentation for
    *           individual adapters.
    * @return a usable representation of the document
    * @throws ZanataServiceException if there is no adapter available for the
    *            document format, or there is an error during parsing
    */
   Resource parseAdapterDocumentFile(URI documentFile, String path, String fileName,
         Optional<String> params) throws ZanataServiceException;

   /**
    * Extract the translatable strings from a new version of an existing document file to a usable form.
    * 
    * @param documentFile location of the document to parse
    * @param docId the id of an existing document
    * @param uploadFileName name of the new file being parsed, used only to identify format
    * @param params adapter-specific parameter string. See documentation for
    *           individual adapters.
    * @return a usable representation of the document
    * @throws ZanataServiceException
    */
   Resource parseUpdatedAdapterDocumentFile(URI documentFile, String docId, String uploadFileName,
         Optional<String> params) throws ZanataServiceException;

   /**
    * Check whether a handler for the given document type is available.
    * 
    * @param fileNameOrExtension full filename with extension, or just extension
    * @return
    */
   boolean hasAdapterFor(DocumentType type);

   /**
    * Check whether a handler for the given file type is available.
    *
    * @param fileNameOrExtension full filename with extension, or just extension
    * @return
    * @deprecated use {@link #hasAdapterFor(DocumentType)}s
    */
   @Deprecated
   boolean hasAdapterFor(String fileNameOrExtension);

   Set<String> getSupportedExtensions();

   /**
    * @deprecated use {@link #getAdapterFor(DocumentType)}
    */
   @Deprecated
   FileFormatAdapter getAdapterFor(String fileNameOrExtension);

   FileFormatAdapter getAdapterFor(DocumentType type);

   /**
    * Persist an input stream to a temporary file.
    * 
    * The created file should be removed using {@link #removeTempFile(File)} when it is no longer required.
    * 
    * @param fileContents stream of bytes to persist
    * @return reference to the created file
    */
   File persistToTempFile(InputStream fileContents);

   /**
    * Attempts to remove a temporary file from disk.
    * 
    * If the file cannot be removed, it is marked for removal on application exit.
    * 
    * @param tempFile file to remove
    */
   void removeTempFile(File tempFile);

   boolean hasPersistedDocument(String projectSlug, String iterationSlug, String docPath, String docName);

   String getFileExtension(String projectSlug, String iterationSlug, String docPath, String docName);

   /**
    * 
    * @param fileNameOrExtension
    * @return the extension for a given filename, or the extension that was passed in
    */
   String extractExtension(String fileNameOrExtension);

   /**
    * @return true if the specified document is of type po, false if it is any other
    *              type, including null.
    */
   boolean isPoDocument(String projectSlug, String iterationSlug, String docId);

   String generateDocId(String path, String fileName);

}
