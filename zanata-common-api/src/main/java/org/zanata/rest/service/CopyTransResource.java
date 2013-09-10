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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.zanata.rest.dto.CopyTransStatus;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path(CopyTransResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
public interface CopyTransResource
{
   public static final String SERVICE_PATH = "/copytrans";

   /**
    * Starts a Translation copy for an individual document.
    *
    * @param projectSlug Project identifier
    * @param iterationSlug Project version identifier
    * @param docId Document Id to copy translations into.
    * @return The following response status codes will be returned from this
    *         operation:<br>
    *         OK(200) - Translation copy was started for the given document. The status of the
    *         process is also returned in the response contents.<br>
    *         UNAUTHORIZED(401) - If the user does not have the proper
    *         permissions to perform this operation.<br>
    *         INTERNAL SERVER ERROR(500) - If there is an unexpected
    *         error in the server while performing this operation. Translation copy will
    *         not start in this case.
    */
   @POST
   @Path("/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId:.+}")
   // /copytrans/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId}
   public CopyTransStatus startCopyTrans( @PathParam("projectSlug") String projectSlug,
                               @PathParam("iterationSlug") String iterationSlug,
                               @PathParam("docId") String docId );

   /**
    * Retrieves the status for a Translation copy process for a document.
    *
    * @param projectSlug Project identifier
    * @param iterationSlug Project version identifier
    * @param docId Document Id
    * @return The following response status codes will be returned from this
    *         operation:<br>
    *         OK(200) - A Translation copy process was found, and its status will be returned
    *         in the body of the response.<br>
    *         NOT_FOUND(404) - If there is no record of a recent translation copy process for
    *         the specified document.
    *         INTERNAL SERVER ERROR(500) - If there is an unexpected
    *         error in the server while performing this operation.
    */
   @GET
   @Path("/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId:.+}")
   // /copytrans/proj/{projectSlug}/iter/{iterationSlug}/doc/{docId}
   public CopyTransStatus getCopyTransStatus( @PathParam("projectSlug") String projectSlug,
                                              @PathParam("iterationSlug") String iterationSlug,
                                              @PathParam("docId") String docId );
}
