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

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.QualifiedName;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * Manage projects
 * <br>
 * projectSlug: Project Identifier.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Path(ProjectResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Project")
public interface ProjectResource extends Serializable {
    public static final String PROJECT_SLUG_TEMPLATE = "{projectSlug:"
            + RestConstants.SLUG_PATTERN + "}";
    public static final String SERVICE_PATH = "/projects/p/"
            + PROJECT_SLUG_TEMPLATE;

    /**
     * Returns header information for a project.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - An "Etag" header for the requested project. <br>
     *         NOT FOUND(404) - If a project could not be found for the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @HEAD
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response head();

    /**
     * Returns data for a single Project.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Containing the Project data.<br>
     *         NOT FOUND(404) - If a Project could not be found for the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(Project.class)
    public Response get();

    /**
     * Creates or modifies a Project.
     *
     * @param project
     *            The project's information.
     * @return The following response status codes will be returned from this
     *         method:<br>
     *         OK(200) - If an already existing project was updated as a result
     *         of this operation.<br>
     *         CREATED(201) - If a new project was added.<br>
     *         FORBIDDEN(403) - If the user was not allowed to create/modify the
     *         project. In this case an error message is contained in the
     *         response.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @PUT
    @Consumes({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response put(Project project);

    /**
     * Return project glossary qualifiedName
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Project glossary qualified name used in the system.
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/glossary/qualifiedName")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
            MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(QualifiedName.class)
    public Response getGlossaryQualifiedName();
}
