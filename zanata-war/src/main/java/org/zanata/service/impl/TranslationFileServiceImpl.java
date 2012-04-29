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
import org.zanata.dao.DocumentDAO;
import org.zanata.exception.ZanataServiceException;
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
         return this.parsePoFile(fileContents);
      }
      else
      {
         throw new ZanataServiceException("Unsupported Translation file: " + fileName);
      }
   }

   private TranslationsResource parsePoFile( InputStream fileContents )
   {
      PoReader2 poReader = new PoReader2();
      TranslationsResource transRes = poReader.extractTarget(new InputSource(fileContents) );
      return transRes;
   }
}
