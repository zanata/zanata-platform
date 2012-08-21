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

import org.apache.commons.io.IOUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xml.sax.InputSource;
import org.zanata.adapter.DTDAdapter;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.OpenOfficeAdapter;
import org.zanata.adapter.PlainTextAdapter;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.LocaleId;
import org.zanata.exception.ZanataServiceException;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.TranslationFileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.jboss.seam.ScopeType.STATELESS;

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

   public TranslationsResource parseTranslationFile(InputStream fileContents, String fileName)
   {
      if( fileName.endsWith(".po") )
      {
         try
         {
            return this.parsePoFile(fileContents);
         }
         catch (Exception e)
         {
            throw new ZanataServiceException("Invalid PO file contents on file: " + fileName);
         }
      }
      else if (hasAdapterFor(fileName))
      {
         // TODO handle exceptions
         return getAdapterFor(fileName).parseTranslationFile(fileContents);
      }
      else
      {
         throw new ZanataServiceException("Unsupported Translation file: " + fileName);
      }
   }

   public Resource parseDocumentFile(InputStream fileContents, String path, String fileName)
   {
      if( fileName.endsWith(".pot") )
      {
         // remove the .pot extension
         fileName = fileName.substring(0, fileName.lastIndexOf('.'));

         try
         {
            return this.parsePotFile(fileContents, path, fileName);
         }
         catch (Exception e)
         {
            throw new ZanataServiceException("Invalid POT file contents on file: " + fileName);
         }
      }
      else if (hasAdapterFor(fileName))
      {
         FileFormatAdapter adapter = getAdapterFor(fileName);
         Resource doc = adapter.parseDocumentFile(fileContents, new LocaleId("en"));

         path = convertToValidPath(path);
         doc.setName(path + fileName);
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

   private TranslationsResource parsePoFile( InputStream fileContents )
   {
      PoReader2 poReader = new PoReader2();
      return poReader.extractTarget(new InputSource(fileContents) );
   }

   private Resource parsePotFile( InputStream fileContents, String docPath, String fileName )
   {
      PoReader2 poReader = new PoReader2();
      // assume english as source locale
      Resource res = poReader.extractTemplate(new InputSource(fileContents), new LocaleId("en"), fileName);
      docPath = convertToValidPath(docPath);

      res.setName( docPath + fileName );
      return res;
   }


   @Override
   public boolean hasAdapterFor(String fileNameOrExtension)
   {
      String extension = extractExtension(fileNameOrExtension);
      if (extension == null)
      {
         return false;
      }
      else
      {
         // TODO add real mapping
         return extension.equals("txt") || extension.equals("dtd");
      }
   }

   @Override
   public FileFormatAdapter getAdapterFor(String fileNameOrExtension)
   {
      String extension = extractExtension(fileNameOrExtension);
      if (extension == null)
      {
         return null;
      }
      else
      {
         // TODO add real mapping
         if (extension.equals("txt"))
         {
            return new PlainTextAdapter();
         }
         else if (extension.equals("dtd"))
         {
            return new DTDAdapter();
         }
         else
         {
            return null;
         }
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



   private static final String DOCUMENT_FILE_PERSIST_DIRECTORY = "/tmp/persisted/";

   @Override
   public void persistDocument(InputStream docContents, String projectSlug, String iterationSlug, String docPath, String docName)
   {
      OutputStream persistFile;
      try
      {
         InputStream fileContents = docContents;
         File file = createFileObject(projectSlug, iterationSlug, docPath, docName);
         file.getParentFile().mkdirs();
         file.createNewFile();
         persistFile = new FileOutputStream(file);
         IOUtils.copy(fileContents, persistFile);
         persistFile.close();
         fileContents.close();
      }
      catch (IOException e)
      {
         // FIXME throw more general exception (independent of implementation) on failure
         e.printStackTrace();
      }
   }

   @Override
   public boolean hasPersistedDocument(String projectSlug, String iterationSlug, String docPath, String docName)
   {
      File file = createFileObject(projectSlug, iterationSlug, docPath, docName);
      return file.exists();
   }

   @Override
   public InputStream streamDocument(String projectSlug, String iterationSlug, String docPath, String docName)
   {
      File file = createFileObject(projectSlug, iterationSlug, docPath, docName);
      InputStream fileContents;
      try
      {
         fileContents = new FileInputStream(file);
      }
      catch (FileNotFoundException e)
      {
         return null;
      }
      return fileContents;
   }

   private File createFileObject(String projectSlug, String iterationSlug, String docPath, String docName)
   {
      // make sure docPath is the correct structure (empty OR no slash at beginning, slash at end)
      docPath = docPath.trim();
      while( docPath.startsWith("/") )
      {
         docPath = docPath.substring(1);
      }
      if( docPath.length() > 0 && !docPath.endsWith("/") )
      {
         docPath = docPath.concat("/");
      }

      // TODO use config-specified or platform independent directory (or switch to database storage)
      String pathname = DOCUMENT_FILE_PERSIST_DIRECTORY + projectSlug + File.separator + iterationSlug + File.separator + docPath + docName;
      return new File(pathname);
   }
}
