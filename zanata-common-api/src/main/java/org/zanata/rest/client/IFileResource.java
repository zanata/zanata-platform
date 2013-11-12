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
package org.zanata.rest.client;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.service.FileResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST client interface for file upload and download.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 * @see DocumentFileUploadForm
 * @see FileResource
 */
@Path(FileResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_OCTET_STREAM })
@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
public interface IFileResource extends FileResource {

    @Override
    @GET
    @Path(ACCEPTED_TYPES_RESOURCE)
    @Produces(MediaType.TEXT_PLAIN)
    // /file/accepted_types
            public
            ClientResponse<String> acceptedFileTypes();

    @Override
    @POST
    @Path(SOURCE_UPLOAD_TEMPLATE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public ClientResponse<ChunkUploadResponse> uploadSourceFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @QueryParam("docId") String docId,
            @MultipartForm DocumentFileUploadForm uploadForm);

    @Override
    @POST
    @Path(TRANSLATION_UPLOAD_TEMPLATE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public ClientResponse<ChunkUploadResponse> uploadTranslationFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("locale") String localeId,
            @QueryParam("docId") String docId,
            @QueryParam("merge") String merge,
            @MultipartForm DocumentFileUploadForm uploadForm);

    @Override
    @GET
    @Path(SOURCE_DOWNLOAD_TEMPLATE)
    public ClientResponse downloadSourceFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("fileType") String fileType,
            @QueryParam("docId") String docId);

    @Override
    @GET
    @Path(FILE_DOWNLOAD_TEMPLATE)
    public ClientResponse downloadTranslationFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("locale") String locale,
            @PathParam("fileType") String fileExtension,
            @QueryParam("docId") String docId);
}
