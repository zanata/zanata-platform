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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.zanata.rest.dto.CopyTransStatus;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * Represents the state of a copy trans run. These are special processes which
 * search for and reuse translations.
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path(CopyTransResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Copy Trans")
public interface CopyTransResource extends Serializable {
    public static final String SERVICE_PATH = "/copytrans";

    /**
     * Starts a Translation copy for an individual document.
     *
     * @param projectSlug
     *            Project identifier
     * @param iterationSlug
     *            Project version identifier
     * @param docId
     *            Document Id to copy translations into.
     */
    @POST
    @Path("/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId:.+}")
    // /copytrans/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId}
    @TypeHint(CopyTransStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200,
                    condition = "Translation copy was started for the given document. " +
                            "The status of the process is also returned in the response's body."),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public
    CopyTransStatus startCopyTrans(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("docId") String docId);

    /**
     * Retrieves the status for a Translation copy process for a document.
     *
     * @param projectSlug
     *            Project identifier
     * @param iterationSlug
     *            Project version identifier
     * @param docId
     *            Document Id
     */
    @GET
    @Path("/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId:.+}")
    // /copytrans/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId}
    @TypeHint(CopyTransStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200,
                    condition = "A translation copy process was found, and its " +
                            "status will be returned in the body of the response"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public
    CopyTransStatus getCopyTransStatus(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("docId") String docId);
}
