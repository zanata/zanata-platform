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
package org.zanata.service.impl;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xml.sax.InputSource;
import org.zanata.adapter.DTDAdapter;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.IDMLAdapter;
import org.zanata.adapter.OpenOfficeAdapter;
import org.zanata.adapter.PlainTextAdapter;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.TranslationFileService;

import com.google.common.base.Optional;
import com.google.common.collect.MapMaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.zanata.common.DocumentType.*;

/**
 * Default implementation of the TranslationFileService interface.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationFileServiceImpl")
@Scope(STATELESS)
@AutoCreate
public class TranslationFileServiceImpl implements TranslationFileService
{
   private static Map<DocumentType, Class<? extends FileFormatAdapter>> DOCTYPEMAP = new MapMaker().makeMap();
   private static DocumentType[] ODF_TYPES =
      {
      OPEN_DOCUMENT_TEXT,
      OPEN_DOCUMENT_TEXT_FLAT,
      OPEN_DOCUMENT_PRESENTATION,
      OPEN_DOCUMENT_PRESENTATION_FLAT,
      OPEN_DOCUMENT_SPREADSHEET,
      OPEN_DOCUMENT_SPREADSHEET_FLAT,
      OPEN_DOCUMENT_GRAPHICS,
      OPEN_DOCUMENT_GRAPHICS_FLAT,
      OPEN_DOCUMENT_DATABASE,
      OPEN_DOCUMENT_FORMULA
      };

   static
   {
      for (DocumentType type : ODF_TYPES)
      {
         DOCTYPEMAP.put(type, OpenOfficeAdapter.class);
      }
      DOCTYPEMAP.put(PLAIN_TEXT, PlainTextAdapter.class);
      DOCTYPEMAP.put(XML_DOCUMENT_TYPE_DEFINITION, DTDAdapter.class);
      DOCTYPEMAP.put(IDML, IDMLAdapter.class);
   }

   private static Set<String> SUPPORTED_EXTENSIONS = buildSupportedExtensionSet();

   private static Set<String> buildSupportedExtensionSet()
   {
      Set<String> supported = new HashSet<String>();
      for (DocumentType type : DOCTYPEMAP.keySet())
      {
         supported.add(type.getExtension());
      }
      return supported;
   }

   @Logger
   Log log;

   @In
   private DocumentDAO documentDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @Override
   public TranslationsResource parseTranslationFile(InputStream fileContents, String fileName,
         String localeId, String projectSlug, String iterationSlug, String docId)
               throws ZanataServiceException
   {
      if( fileName.endsWith(".po") )
      {
         boolean originalIsPo = isPoDocument(projectSlug, iterationSlug, docId);
         try
         {
            return parsePoFile(fileContents, !originalIsPo);
         }
         catch (Exception e)
         {
            throw new ZanataServiceException("Invalid PO file contents on file: " + fileName, e);
         }
      }
      else if (hasAdapterFor(fileName))
      {
         File tempFile = persistToTempFile(fileContents);
         Optional<String> params = documentDAO.getAdapterParams(projectSlug, iterationSlug, docId);
         TranslationsResource transRes;
         try
         {
            transRes = getAdapterFor(fileName).parseTranslationFile(tempFile.toURI(), localeId, params);
         }
         catch (FileFormatAdapterException e)
         {
            throw new ZanataServiceException("Error parsing translation file: " + fileName, e);
         }
         removeTempFile(tempFile);
         return transRes;
      }
      else
      {
         throw new ZanataServiceException("Unsupported Translation file: " + fileName);
      }
   }

   @Override
   public String generateDocId(String path, String fileName)
   {
      String docName = fileName;
      if (docName.endsWith(".pot"))
      {
         docName = docName.substring(0, docName.lastIndexOf('.'));
      }
      return convertToValidPath(path) + docName;
   }

   @Override
   public Resource parseUpdatedPotFile(InputStream fileContents, String docId, String fileName, boolean offlinePo)
   {
      if (fileName.endsWith(".pot"))
      {
         try
         {
            return parsePotFile(fileContents, docId, offlinePo);
         }
         catch (Exception e)
         {
            throw new ZanataServiceException("Invalid POT file contents on file: " + fileName, e);
         }
      }
      else
      {
         throw new ZanataServiceException("Unsupported Document file: " + fileName);
      }
   }

   @Override
   public Resource parseAdapterDocumentFile(URI documentFile, String documentPath, String fileName, Optional<String> params) throws ZanataServiceException
   {
      return parseUpdatedAdapterDocumentFile(documentFile, convertToValidPath(documentPath) + fileName, fileName, params);
   }

   @Override
   public Resource parseUpdatedAdapterDocumentFile(URI documentFile, String docId, String fileName, Optional<String> params) throws ZanataServiceException
   {
      if (hasAdapterFor(fileName))
      {
         FileFormatAdapter adapter = getAdapterFor(fileName);
         Resource doc;
         try
         {
            doc = adapter.parseDocumentFile(documentFile, new LocaleId("en"), params);
         }
         catch (FileFormatAdapterException e)
         {
            throw new ZanataServiceException("Error parsing document file: " + fileName, e);
         }
         doc.setName(docId);
         return doc;
      }
      else
      {
         throw new ZanataServiceException("Unsupported Document file: " + fileName);
      }
   }

   /**
    * A valid path is either empty, or has a trailing slash and no leading slash.
    * 
    * @param path
    * @return valid path
    */
   private String convertToValidPath(String path)
   {
      path = path.trim();
      while( path.startsWith("/") )
      {
         path = path.substring(1);
      }
      if( path.length() > 0 && !path.endsWith("/") )
      {
         path = path.concat("/");
      }
      return path;
   }

   private TranslationsResource parsePoFile( InputStream fileContents, boolean offlinePo )
   {
      PoReader2 poReader = new PoReader2(offlinePo);
      return poReader.extractTarget(new InputSource(fileContents) );
   }

   private Resource parsePotFile(InputStream fileContents, String docId, boolean offlinePo)
   {
      PoReader2 poReader = new PoReader2(offlinePo);
      // assume english as source locale
      Resource res = poReader.extractTemplate(new InputSource(fileContents), new LocaleId("en"), docId);
      return res;
   }

   // TODO replace these with values from DocumentType
   @Override
   public Set<String> getSupportedExtensions()
   {
      return SUPPORTED_EXTENSIONS;
   }

   @Override
   public boolean hasAdapterFor(DocumentType type)
   {
      return DOCTYPEMAP.containsKey(type);
   }

   private boolean hasAdapterFor(String fileNameOrExtension)
   {
      String extension = extractExtension(fileNameOrExtension);
      if (extension == null)
      {
         return false;
      }
      DocumentType documentType = DocumentType.typeFor(extension);
      if (documentType == null)
      {
         return false;
      }
      return hasAdapterFor(documentType);
   }

   /**
    * @deprecated use {@link #getAdapterFor(DocumentType)}
    */
   @Deprecated
   @Override
   public FileFormatAdapter getAdapterFor(String fileNameOrExtension)
   {
      // FIXME throw exception when not found

      String extension = extractExtension(fileNameOrExtension);
      if (extension == null)
      {
         return null;
      }
      DocumentType documentType = DocumentType.typeFor(extension);
      if (documentType == null)
      {
         return null;
      }
      return getAdapterFor(documentType);
   }

   @Override
   public DocumentType getDocumentType(String fileNameOrExtension)
   {
      return DocumentType.typeFor(extractExtension(fileNameOrExtension));
   }

   @Override
   public FileFormatAdapter getAdapterFor(DocumentType type)
   {
      Class<? extends FileFormatAdapter> clazz = DOCTYPEMAP.get(type);
      if (clazz == null)
      {
         throw new RuntimeException("No adapter for document type: " + type);
      }
      try
      {
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to construct adapter for document type: "+type, e);
      }
   }

   @Override
   public String extractExtension(String fileNameOrExtension)
   {
      if (fileNameOrExtension == null || fileNameOrExtension.length() == 0 || fileNameOrExtension.endsWith("."))
      {
         // could throw exception here
         return null;
      }

      String extension;
      if (fileNameOrExtension.contains("."))
      {
         extension = fileNameOrExtension.substring(fileNameOrExtension.lastIndexOf('.') + 1);
      }
      else
      {
         extension = fileNameOrExtension;
      }
      return extension;
   }

   @Override
   public File persistToTempFile(InputStream fileContents)
   {
      File tempFile = null;
      try
      {
         tempFile = File.createTempFile("zupload", ".tmp");
         byte[] buffer = new byte[4096]; // To hold file contents
         int bytesRead;
         FileOutputStream output = new FileOutputStream(tempFile);
         while ((bytesRead = fileContents.read(buffer)) != -1)
         {
            output.write(buffer, 0, bytesRead);
         }
         output.close();
      }
      catch (IOException e)
      {
         throw new ZanataServiceException("Error while writing uploaded file to temporary location", e);
      }
      return tempFile;
   }

   @Override
   public void removeTempFile(File tempFile)
   {
      if (tempFile != null)
      {
         if (!tempFile.delete())
         {
            log.warn("unable to remove temporary file {}, marking for delete on exit", tempFile.getAbsolutePath());
            tempFile.deleteOnExit();
         }
      }
   }

   @Override
   public boolean hasPersistedDocument(String projectSlug, String iterationSlug, String docPath, String docName)
   {
      HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docPath+docName);
      return doc.getRawDocument() != null;
   }

   @Override
   public String getFileExtension(String projectSlug, String iterationSlug, String docPath, String docName)
   {
      HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docPath+docName);
      return doc.getRawDocument().getType().getExtension();
   }

   @Override
   public boolean isPoDocument(String projectSlug, String iterationSlug, String docId)
   {
      HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      ProjectType projectType = projectIteration.getProjectType();
      if (projectType == null)
      {
         projectType = projectIteration.getProject().getDefaultProjectType();
      }
      if (projectType == ProjectType.Gettext || projectType == ProjectType.Podir)
      {
         return true;
      }

      if (projectType == ProjectType.File)
      {
         HDocument doc = documentDAO.getByDocIdAndIteration(projectIteration, docId);
         if (doc.getRawDocument() == null)
         {
            // po is the only format in File projects for which no raw document is stored
            return true;
         }

         // additional check in case we do start storing raw documents for po
         DocumentType docType = doc.getRawDocument().getType();
         return docType == GETTEXT_PORTABLE_OBJECT || docType == GETTEXT_PORTABLE_OBJECT_TEMPLATE;
      }
      return false;
   }

}
