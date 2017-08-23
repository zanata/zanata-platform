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

import java.io.Serializable;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.common.FileTypeInfo;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Represents various types of translatable files to be processed by the server
 * instead of the REST client.
 * This REST interface provides endpoints for uploading and downloading files in
 * multiple formats.
 */
@Path(FileResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_OCTET_STREAM })
@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
@ResourceLabel("Files")
public interface FileResource extends Serializable {
    public static final String SERVICE_PATH = "/file";
    @Deprecated
    public static final String FILE_RESOURCE = SERVICE_PATH;
    public static final String ACCEPTED_TYPES_RESOURCE = "/accepted_types";
    public static final String ACCEPTED_TYPE_LIST_RESOURCE = "/accepted_document_types";
    public static final String FILE_TYPE_INFO_RESOURCE = "/file_type_info";
    public static final String DOWNLOAD_TEMPLATE = "/download/{downloadId}";
    public static final String FILE_DOWNLOAD_TEMPLATE =
            "/translation/{projectSlug}/{iterationSlug}/{locale}/{fileType}";
    public static final String TRANSLATION_UPLOAD_TEMPLATE =
            "/translation/{projectSlug}/{iterationSlug}/{locale}";
    public static final String SOURCE_UPLOAD_TEMPLATE =
            "/source/{projectSlug}/{iterationSlug}";
    public static final String SOURCE_DOWNLOAD_TEMPLATE =
            "/source/{projectSlug}/{iterationSlug}/{fileType}";

    /**
     * Specifies to download the original source file that was uploaded,
     * byte-for-byte.
     */
    public static final String FILETYPE_RAW_SOURCE_DOCUMENT = "raw";

    /**
     * Specifies to download a preview of the translated document in the
     * original source format, showing all non-empty translations (even if not
     * approved). Where translations are empty, source strings are used.
     */
    public static final String FILETYPE_TRANSLATED_APPROVED_AND_FUZZY =
            "half-baked";

    /**
     * Specifies to download a completed version of a translated document in the
     * original source format, showing only approved translations. Where no
     * approved translation is available, source strings are used.
     */
    public static final String FILETYPE_TRANSLATED_APPROVED = "baked";

    /**
     * Deprecated. Returns the source file extensions supported by the server.
     * @return a semicolon-separated list of file extensions
     * @see DocumentType#getSourceExtensions()
     * @see #fileTypeInfoList()
     */
    @Deprecated
    @GET
    @Path(ACCEPTED_TYPES_RESOURCE)
    @Produces(MediaType.TEXT_PLAIN)
    // /file/accepted_types
    public
    Response acceptedFileTypes();

    /**
     * Deprecated. A list of document types supported by the server. The result
     * will not be deserializable if the server supports document types which
     * the client does not know about (eg compiled against a newer version of
     * zanata-api), also any changes to the list of supported file extensions
     * for existing types will not be visible to the client.
     * @return a List of DocumentType
     * @see DocumentType
     * @see #fileTypeInfoList()
     */
    @Deprecated
    @GET
    @Path(ACCEPTED_TYPE_LIST_RESOURCE)
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(List.class)
    public
    Response acceptedFileTypeList();

    /**
     * A list of document types supported by the server, along with their
     * default file extensions.
     * @return a List of FileTypeInfo
     * @see FileTypeInfo
     */
    @GET
    @Path(FILE_TYPE_INFO_RESOURCE)
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(List.class)
    Response fileTypeInfoList();

    /**
     * Upload a source file (or file chunk) to Zanata. Allows breaking up files
     * into smaller chunks for very large files. In this case, the first invocation
     * of this service will return an 'upload id' which needs to be used in
     * subsequent calls to tie all the uploaded chunks together.
     * The file will only be processed when all chunks have been fully uploaded.
     * With each uploaded chunk, the multipart message's 'last' parameter will
     * indicate if it is the last expected chunk.
     *
     * @param projectSlug The project slug where to store the document.
     * @param iterationSlug The project version slug where to store the document.
     * @param docId The full Document identifier
     * @param uploadForm The multi-part form body for the file or chunk.
     * @return A message with information about the upload operation.
     */
    @POST
    @Path(SOURCE_UPLOAD_TEMPLATE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    // /file/source/{projectSlug}/{iterationSlug}?docId={docId}
    @TypeHint(ChunkUploadResponse.class)
    public
    Response uploadSourceFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @QueryParam("docId") String docId,
            @MultipartForm DocumentFileUploadForm uploadForm);

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
     * @param docId The full Document identifier.
     * @param merge Indicates whether to merge translations or overwrite all
     *              translations with the contents of the uploaded file.
     * @param uploadForm The multi-part form body for the file or chunk.
     * @return A message with information about the upload operation.
     */
    @POST
    @Path(TRANSLATION_UPLOAD_TEMPLATE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    // /file/translation/{projectSlug}/{iterationSlug}/{locale}?docId={docId}&merge={merge}
    @TypeHint(ChunkUploadResponse.class)
    public
    Response uploadTranslationFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("locale") String localeId,
            @QueryParam("docId") String docId,
            @QueryParam("merge") String merge,
            @MultipartForm DocumentFileUploadForm uploadForm);

    /**
     * Downloads a single source file.
     *
     * @param projectSlug
     * @param iterationSlug
     * @param fileType
     *            use 'raw' for original source if available, or 'pot' to
     *            generate pot from source strings
     * @param docId
     * @return response with status code 404 if the document is not found, 415
     *         if fileType is not valid for the document, otherwise 200 with
     *         attached document.
     */
    @GET
    @Path(SOURCE_DOWNLOAD_TEMPLATE)
    // /file/source/{projectSlug}/{iterationSlug}/{fileType}?docId={docId}
            public
            Response downloadSourceFile(
                    @PathParam("projectSlug") String projectSlug,
                    @PathParam("iterationSlug") String iterationSlug,
                    @PathParam("fileType") String fileType,
                    @QueryParam("docId") String docId);

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
     * @param fileExtension
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
    @Path(FILE_DOWNLOAD_TEMPLATE)
    // /file/translation/{projectSlug}/{iterationSlug}/{locale}/{fileType}?docId={docId}
            public
            Response downloadTranslationFile(
                    @PathParam("projectSlug") String projectSlug,
                    @PathParam("iterationSlug") String iterationSlug,
                    @PathParam("locale") String locale,
                    @PathParam("fileType") String fileExtension,
                    @QueryParam("docId") String docId);

    /**
     * Downloads a previously generated file.
     *
     * @param downloadId
     *            The Zanata generated download id.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - A translation file in the requested format with
     *         translations for the requested document in a project, iteration
     *         and locale. <br>
     *         NOT FOUND(404) - If a downloadable file is not found for the
     *         given id, or is not yet ready for download (i.e. the system is
     *         still preparing it).<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path(DOWNLOAD_TEMPLATE)
    // /file/download/{downloadId}
            public
            Response download(@PathParam("downloadId") String downloadId);
}
