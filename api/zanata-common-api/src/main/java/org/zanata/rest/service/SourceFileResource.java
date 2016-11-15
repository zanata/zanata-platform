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

import java.io.InputStream;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.JobStatus;

import static org.zanata.rest.service.SourceFileResource.SERVICE_PATH;

/**
 * REST Interface for upload and download of source files.
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Path(SERVICE_PATH)
@Consumes({ MediaType.WILDCARD })
@Produces({ MediaType.WILDCARD })
public interface SourceFileResource extends RestResource {
    String SERVICE_PATH = "/proj/{projectSlug}/ver/{versionSlug}/document/source";

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
     * @param versionSlug The project version slug where to store the document.
     * @param docId The full Document identifier (including file extension)
     * @param fileStream Contents of the file to be uploaded
     * @param size size of the file in bytes (for sanity checking; use -1 if unknown)
     * @param projectType A ProjectType used for mapping of file extensions.
     * @return A message with information about the upload operation.
     */
    @POST
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON })
    @TypeHint(JobStatus.class)
    Response uploadSourceFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @QueryParam("docId") String docId,
            InputStream fileStream,
            @QueryParam("size") @DefaultValue("-1") long size,
            @QueryParam("projectType") @Nullable ProjectType projectType);

    /**
     * Downloads a single source file.
     *
     * @param projectSlug
     * @param versionSlug
     * @param docId The full Document identifier
     * @param projectType A ProjectType used for mapping of file extensions.
     * @return response with status code 404 if the document is not found, 415
     *         if fileType is not valid for the document, otherwise 200 with
     *         attached document.
     */
    @GET
    Response downloadSourceFile(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @QueryParam("docId") String docId,
            @QueryParam("projectType") String projectType);

}
