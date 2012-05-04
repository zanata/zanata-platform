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
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.TranslationFileService;

import java.io.InputStream;

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
      else
      {
         throw new ZanataServiceException("Unsupported Document file: " + fileName);
      }
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
      // get rid of leading slashes ("/")
      if( docPath.startsWith("/") )
      {
         docPath = docPath.substring(1);
      }
      // Add a trailing slash ("/") if there isn't one
      if( !docPath.endsWith("/") )
      {
         docPath = docPath.concat("/");
      }

      res.setName( docPath + fileName );
      return res;
   }
}
