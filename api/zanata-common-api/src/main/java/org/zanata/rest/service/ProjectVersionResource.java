/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.FilterFields;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.TransUnitStatus;
import org.zanata.rest.dto.User;
import org.zanata.rest.dto.resource.ResourceMeta;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * Project version resource API, replacing {@link ProjectIterationResource}.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path(ProjectVersionResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Project Version")
public interface ProjectVersionResource extends Serializable {
    public static final String PROJECT_SERVICE_PATH = "/project";

    public static final String VERSION_SLUG_TEMPLATE = "/{versionSlug:"
            + RestConstants.SLUG_PATTERN + "}";

    public static final String SERVICE_PATH = PROJECT_SERVICE_PATH
            + "/" + ProjectResource.PROJECT_SLUG_TEMPLATE
            + "/version";

    /**
     * Returns header information for a project iteration.
     *
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
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
    @Path(VERSION_SLUG_TEMPLATE)
    public Response head(@PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug);

    /**
     * Creates or modifies a Project iteration.
     *
     * @param projectVersion
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
    @Path(VERSION_SLUG_TEMPLATE)
    public Response put(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            ProjectIteration projectVersion);

    /**
     * Get a project iteration's sample general configuration file(zanata.xml).
     * Note: this will only be a sample configuration which does not contain any
     * customization done on the client side, such as, locale mapping and/or
     * command hooks.
     *
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
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
    @Path(VERSION_SLUG_TEMPLATE + "/config")
    @Produces({ MediaType.APPLICATION_XML })
    public Response sampleConfiguration(
        @PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug);

    /**
     * Returns data for a single Project iteration.
     *
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Contains the Project version data. <br>
     *         NOT FOUND(404) - response, if a Project version could not be
     *         found for the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE)
    @TypeHint(ProjectIteration.class)
    public Response getVersion(@PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug);

    /**
     * Get list of contributor (translator/review) for the given
     * project version in date range.
     *
     * @param projectSlug
     *            project identifier
     * @param versionSlug
     *            version identifier
     * @param dateRange
     *            date range from..to (yyyy-mm-dd..yyyy-mm-dd)
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_VERSION_JSON,
            MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE + "/contributors/{dateRange}")
    @TypeHint(User[].class)
    public Response getContributors(
        @PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug,
        @PathParam("dateRange") String dateRange);

    /**
     * Retrieves a full list of locales enabled in project version.
     *
     * @see ProjectIterationLocalesResource#get()
     *
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         NOT FOUND(404) - If a Version could not be found for the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_VERSION_LOCALES_JSON,
            MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE + "/locales")
    @TypeHint(LocaleDetails[].class)
    public Response getLocales(@PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug);

    /**
     * Retrieve the List of Documents (Resources) belongs to a Project version.
     *
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response with a list of documents wrapped in a
     *         "resources" element. Each child element will be a
     *         "resource-meta". <br>
     *         NOT FOUND(404) - If a Project iteration could not be found with
     *         the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE + "/docs")
    @TypeHint(ResourceMeta[].class)
    public Response getDocuments(@PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug);

    /**
     * Queries for a list of translation unit id with status in a document.
     *
     * @param filterConstraints
     *            Optional filtering based on one or several fields.
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
     * @param docId
     *            The document identifier.
     * @param localeId
     *            target locale, default to 'en-US'
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         NOT FOUND(404) - If a document or locale could not be found for
     *         the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     *
     */
    @POST
    @Produces({ MediaTypes.APPLICATION_ZANATA_TRANS_UNIT_RESOURCE_JSON,
            MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE + "/doc/{docId}/status/{localeId}")
    @TypeHint(TransUnitStatus[].class)
    public Response getTransUnitStatus(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @PathParam("docId") String docId,
            @DefaultValue("en-US") @PathParam("localeId") String localeId,
            FilterFields filterFields);
}
