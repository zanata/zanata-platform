/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.ProjectIteration;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * Deprecated. See {@link ProjectVersionResource}
 *
 *
 * projectSlug: Project Identifier. iterationSlug: Project Iteration identifier.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Path(ProjectIterationResource.SERVICE_PATH)
@Deprecated
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface ProjectIterationResource extends Serializable {
    public static final String ITERATION_SLUG_TEMPLATE = "{iterationSlug:"
            + RestConstants.SLUG_PATTERN + "}";
    public static final String SERVICE_PATH = ProjectResource.SERVICE_PATH
            + "/iterations/i/" + ITERATION_SLUG_TEMPLATE;

    /**
     * Returns header information for a project iteration.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response with an "Etag" header for the requested
     *         project iteration.<br>
     *         NOT FOUND(404) - If a project iteration could not be found for
     *         the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @HEAD
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response head();

    /**
     * Returns data for a single Project iteration.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Contains the Project iteration data. <br>
     *         NOT FOUND(404) - response, if a Project iteration could not be
     *         found for the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(ProjectIteration.class)
    public Response get();

    /**
     * Creates or modifies a Project iteration.
     *
     * @param projectIteration
     *            The project iteration information.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If an already existing project iteration was updated as
     *         a result of this operation.<br>
     *         CREATED(201) - If a new project iteration was added.<br>
     *         NOT FOUND(404) - If no project was found for the given project
     *         slug.<br>
     *         FORBIDDEN(403) - If the user was not allowed to create/modify the
     *         project iteration. In this case an error message is contained in
     *         the response.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @PUT
    @Consumes({ MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response put(ProjectIteration project);

    /**
     * Get a project iteration's sample general configuration file(zanata.xml).
     * Note: this will only be a sample configuration which does not contain any
     * customization done on the client side, such as, locale mapping and/or
     * command hooks.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Contains the Project iteration config xml. <br>
     *         NOT FOUND(404) - response, if a Project iteration could not be
     *         found for the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/config")
    @Produces({ MediaType.APPLICATION_XML })
    public Response sampleConfiguration();
}
