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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public interface TranslationFileResource
{

   public static final String FILE_NAME_TEMPLATE = "/{docId}";
   public static final String LOCALE_TEMPLATE = "/{locale}";
   public static final String FILE_EXTENSION_TEMPLATE = ".{fileExt}";
   public static final String FILE_DOWNLOAD_TEMPLATE = LOCALE_TEMPLATE + FILE_NAME_TEMPLATE + FILE_EXTENSION_TEMPLATE;
   
   @GET
   @Path(FILE_DOWNLOAD_TEMPLATE)
   // /file/{locale}/{docId}.{fileExt}
   public Response downloadTranslationFile( @PathParam("locale") String locale, @PathParam("docId") String docId, 
         @PathParam("fileExt") String fileExtension );
   
}
