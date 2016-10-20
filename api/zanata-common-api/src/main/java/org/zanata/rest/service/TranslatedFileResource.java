/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.zanata.rest.dto.FileUploadResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.InputStream;

import static org.zanata.rest.service.TranslatedFileResource.SERVICE_PATH;

/**
 * REST Interface for upload and download of translation files.
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Path(SERVICE_PATH)
@Produces({ MediaType.APPLICATION_OCTET_STREAM })
@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
public interface TranslatedFileResource extends RestResource {
    String SERVICE_PATH = "/file2/translation";

    /**
     * Upload a translation file (or file chunk) to Zanata. Allows breaking up files
     * into smaller chunks for very large files. In this case, the first invocation
     * of this service will return an 'upload id' which needs to be used in
     * subsequent calls to tie all the uploaded chunks together.
     * The file will only be processed when all chunks have been fully uploaded.
     * With each uploaded chunk, the multipart message's 'last' parameter will
     * indicate if it is the last expected chunk.
     *
     * @param projectSlug The project slug where to store the document.
     * @param iterationSlug The project version slug where to store the document.
     * @param localeId The locale (language) for the translation file.
     * @param docId The full Document identifier (including file extension)
     * @param merge Indicates whether to merge translations or overwrite all
     *              translations with the contents of the uploaded file.
     * @param fileStream Contents of the file to be uploaded
     * @return A message with information about the upload operation.
     */
    @POST
    @Path("/{projectSlug}/{iterationSlug}/{locale}")
    // /file/translation/{projectSlug}/{iterationSlug}/{locale}?docId={docId}&merge={merge}
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(FileUploadResponse.class)
    Response uploadTranslationFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("locale") String localeId,
            @QueryParam("docId") String docId,
            @QueryParam("merge") String merge,
            InputStream fileStream,
            @QueryParam("projectType") String projectType);

    /**
     * Downloads a single translation file.
     *
     * To download a preview-document or translated document where a raw source
     * document is available, use fileType 'half_baked' and 'baked'
     * respectively.
     *
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project iteration identifier.
     * @param locale
     *            Translations for this locale will be contained in the
     *            downloaded document.
     * @param fileType
     *            File type to be downloaded. (Options: 'po', 'half_baked',
     *            'baked')
     * @param docId
     *            Document identifier to fetch translations for.
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
    @GET
    @Path("/{projectSlug}/{iterationSlug}/{locale}/{fileType}")
    // /file/translation/{projectSlug}/{iterationSlug}/{locale}/{fileType}?docId={docId}
    Response downloadTranslationFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("locale") String locale,
            @PathParam("fileType") String fileType,
            @QueryParam("docId") String docId,
            @QueryParam("projectType") String projectType);

}
