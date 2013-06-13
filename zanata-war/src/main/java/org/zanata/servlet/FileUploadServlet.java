/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jboss.seam.Component;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.exception.ZanataServiceException;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.service.impl.TranslationFileServiceImpl;
import org.zanata.service.impl.TranslationServiceImpl;
import org.zanata.webtrans.client.ui.FileUploadDialog;

/**
 * Used for translation file upload from GWT editor.
 * For endpoint, see servlet binding for this class in web.xml
 * 
 * @see {@link FileUploadDialog}
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class FileUploadServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   private final static String[] MANDATORY_FIELDS = { "projectSlug", "versionSlug", "docId", "fileName", "targetLocale", "uploadFileElement", "merge" };

   private TranslationFileService translationFileServiceImpl;

   private TranslationService translationServiceImpl;

   private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadServlet.class);

   @Override
   protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
   {
      new ContextualHttpServletRequest(request)
      {
         @Override
         public void process() throws Exception
         {
            doWork(request, response);
         }
      }.run();
   }

   private void validateParams(HashMap<String, FileItem> params)
   {
      for(String mandatoryField: MANDATORY_FIELDS)
      {
         if (!params.containsKey(mandatoryField))
         {
            throw new ZanataServiceException("Mandatory field '" + mandatoryField + "' not found in form");
         }
      }

   }

   private void doWork(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {

      // process only multipart requests
      if (ServletFileUpload.isMultipartContent(req))
      {
         translationFileServiceImpl = (TranslationFileService) Component.getInstance(TranslationFileServiceImpl.class);
         translationServiceImpl = (TranslationService) Component.getInstance(TranslationServiceImpl.class);

         // Create a factory for disk-based file items
         FileItemFactory factory = new DiskFileItemFactory();

         // Create a new file upload handler
         ServletFileUpload upload = new ServletFileUpload(factory);

         // Parse the request
         try
         {
            List<FileItem> items = upload.parseRequest(req);
            HashMap<String, FileItem> params = new HashMap<String, FileItem>();
            for (FileItem item : items)
            {
               LOGGER.info("param- " + item.getFieldName() + " value-" + item.getString());
               params.put(item.getFieldName(), item);
            }

            validateParams(params);

            String projectSlug = params.get("projectSlug").getString();
            String versionSlug = params.get("versionSlug").getString();
            String docId = params.get("docId").getString();
            // process the file
            TranslationsResource transRes = translationFileServiceImpl.parseTranslationFile(
                        params.get("uploadFileElement").getInputStream(),
                        params.get("fileName").getString(),
                        params.get("targetLocale").getString(),
                        projectSlug, versionSlug, docId);

            // translate it
            Set<String> extensions;
            if (params.get("fileName").getString().endsWith(".po"))
            {
               extensions = new StringSet(ExtensionType.GetText.toString());
            }
            else
            {
               extensions = Collections.<String> emptySet();
            }
            MergeType mergeType = Boolean.parseBoolean(params.get("merge").getString()) ? MergeType.AUTO : MergeType.IMPORT;
            List<String> warnings = translationServiceImpl.translateAllInDoc(
                  projectSlug, versionSlug, docId, new LocaleId(params.get("targetLocale").getString()),
                  transRes, extensions, mergeType);

            StringBuilder response = new StringBuilder();
            response.append("Status code: ");
            response.append(HttpServletResponse.SC_OK);
            response.append(" File '" + params.get("fileName").getString() + "' uploaded. \n");
            if(!warnings.isEmpty())
            {
               response.append("Warnings:\n");
               for (String warning : warnings)
               {
                  response.append(warning + "\n");
               }
            }
            resp.setContentLength(response.toString().length());
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setCharacterEncoding("utf8");

            resp.getWriter().print(response.toString());
            resp.getWriter().flush();

         }
         catch (Exception e)
         {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while uploading the file : " + e.getMessage());
         }

      }
      else
      {
         resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Request contents type is not supported.");
      }
   }
}
