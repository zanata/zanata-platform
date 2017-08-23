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
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Source document API. This API uses format-independent data structures. For
 * format specific source document access see {@link FileResource}
 *
 * <br>projectSlug: Project Identifier.
 * <br>iterationSlug: Project Iteration identifier.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path(SourceDocResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Source Documents")
public interface SourceDocResource extends Serializable {
    @SuppressWarnings("deprecation")
    String SERVICE_PATH = ProjectIterationResource.SERVICE_PATH;
    String RESOURCE_PATH = "/r";
    String DOCID_RESOURCE_PATH = "/resource";

    String RESOURCE_SLUG_REGEX =
            "[\\-_a-zA-Z0-9]+([a-zA-Z0-9_\\-,{.}]*[a-zA-Z0-9]+)?";
    String RESOURCE_NAME_REGEX =
            // as above, with ',' replaced by '/'
            "[\\-_a-zA-Z0-9]+([a-zA-Z0-9_\\-/{.}]*[a-zA-Z0-9]+)?";
    String RESOURCE_SLUG_TEMPLATE =
            RESOURCE_PATH + "/{id:" + RESOURCE_SLUG_REGEX + "}";

    /**
     * Returns header information for a Project's iteration source strings.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing an "Etag" header for the requested
     *         project iteration translations.<br>
     *         NOT FOUND(404) - If a project iteration could not be found for
     *         the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @HEAD
    @Path(RESOURCE_PATH)
    public Response head();

    /**
     * Retrieve the List of Documents (Resources) belonging to a Project
     * iteration.
     *
     * @param extensions
     *            The document extensions to fetch along with the documents
     *            (e.g. "gettext", "comment"). This parameter allows multiple
     *            values e.g. "ext=gettext&ext=comment".
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
    @Path(RESOURCE_PATH)
    @TypeHint(ResourceMeta[].class)
    Response get(@QueryParam("ext") Set<String> extensions);

    /**
     * Creates a new source Document.
     *
     * @param resource
     *            The document information.
     * @param extensions
     *            The document extensions to save with the new document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @param copytrans
     *            Boolean value that indicates whether reasonably close
     *            translations from other projects should be found to initially
     *            populate this document's translations.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         CREATED (201) - If the document was successfully created.<br>
     *         CONFLICT(409) - If another document already exists with the same
     *         name, on the same project iteration.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @POST
    @Path(RESOURCE_PATH)
    public Response post(Resource resource,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

    /**
     * Retrieves information for a source Document.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param extensions
     *            The document extensions to fetch along with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response with the document's information.<br>
     *         NOT FOUND(404) - If a document could not be found with the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     * Deprecated. Use {@link #getResourceWithDocId}
     */
    @Deprecated
    @GET
    @Path(RESOURCE_SLUG_TEMPLATE)
    // /r/{id}
            @TypeHint(Resource.class)
            public
            Response getResource(@PathParam("id") String idNoSlash,
                    @QueryParam("ext") Set<String> extensions);

    /**
     * Retrieves information for a source Document.
     *
     * @param docId
     *            The document identifier.
     * @param extensions
     *            The document extensions to fetch along with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response with the document's information.<br>
     *         NOT FOUND(404) - If a document could not be found with the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @TypeHint(Resource.class)
    @Path(DOCID_RESOURCE_PATH)
    public
    Response getResourceWithDocId(@QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions);

    /**
     * Creates or modifies a source Document.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param resource
     *            The document information.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @param copytrans
     *            Boolean value that indicates whether reasonably close
     *            translations from other projects should be found to initially
     *            populate this document's translations.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         CREATED(201) - If a new document was successfully created.<br>
     *         OK(200) - If an already existing document was modified.<br>
     *         NOT FOUND(404) - If a project or project iteration could not be
     *         found with the given parameters.<br>
     *         FORBIDDEN(403) - If the user is not allowed to modify the
     *         project, project iteration or document. This might be due to the
     *         project or iteration being in Read-Only mode.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     *
     * Deprecated. Use {@link #putResourceWithDocId}
     */
    @Deprecated
    @PUT
    @Path(RESOURCE_SLUG_TEMPLATE)
    // /r/{id}
            public
            Response
            putResource(
                    @PathParam("id") String idNoSlash,
                    Resource resource,
                    @QueryParam("ext") Set<String> extensions,
                    @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);


    /**
     * Creates or modifies a source Document.
     *
     * @param docId
     *            The document identifier.
     * @param resource
     *            The document information.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @param copytrans
     *            Boolean value that indicates whether reasonably close
     *            translations from other projects should be found to initially
     *            populate this document's translations.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         CREATED(201) - If a new document was successfully created.<br>
     *         OK(200) - If an already existing document was modified.<br>
     *         NOT FOUND(404) - If a project or project iteration could not be
     *         found with the given parameters.<br>
     *         FORBIDDEN(403) - If the user is not allowed to modify the
     *         project, project iteration or document. This might be due to the
     *         project or iteration being in Read-Only mode.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @PUT
    @Path(DOCID_RESOURCE_PATH)
    public Response putResourceWithDocId(
            Resource resource,
            @QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

    /**
     * Delete a source Document. The system keeps the history of this document however.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If The document was successfully deleted.<br>
     *         NOT FOUND(404) - If a project or project iteration could not be
     *         found with the given parameters.<br>
     *         FORBIDDEN(403) - If the user is not allowed to modify the
     *         project, project iteration or document. This might be due to the
     *         project or iteration being in Read-Only mode.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     *
     * Deprecated. Use {@link #deleteResourceWithDocId}
     */
    @Deprecated
    @DELETE
    @Path(RESOURCE_SLUG_TEMPLATE)
    // /r/{id}
            public
            Response deleteResource(@PathParam("id") String idNoSlash);

    /**
     * Delete a source Document. The system keeps the history of this document however.
     *
     * @param docId
     *            The document identifier.
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If The document was successfully deleted.<br>
     *         NOT FOUND(404) - If a project or project iteration could not be
     *         found with the given parameters.<br>
     *         FORBIDDEN(403) - If the user is not allowed to modify the
     *         project, project iteration or document. This might be due to the
     *         project or iteration being in Read-Only mode.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @DELETE
    @Path(DOCID_RESOURCE_PATH)
    public Response deleteResourceWithDocId(
            @QueryParam("docId") @DefaultValue("") String docId);

    /**
     * Retrieves meta-data information for a source Document.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param extensions
     *            The document extensions to retrieve with the document's
     *            meta-data (e.g. "gettext", "comment"). This parameter allows
     *            multiple values e.g. "ext=gettext&ext=comment".
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the Document's meta-data was found. The data will be
     *         contained in the response.<br>
     *         NOT FOUND(404) - If a project, project iteration or document
     *         could not be found with the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     *
     * Deprecated. Use {@link #getResourceMetaWithDocId}
     */
    @Deprecated
    @GET
    @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
    // /r/{id}/meta
            @TypeHint(ResourceMeta.class)
            public
            Response getResourceMeta(@PathParam("id") String idNoSlash,
                    @QueryParam("ext") Set<String> extensions);

    /**
     * Retrieves meta-data information for a source Document.
     *
     * @param docId
     *            The document identifier.
     * @param extensions
     *            The document extensions to retrieve with the document's
     *            meta-data (e.g. "gettext", "comment"). This parameter allows
     *            multiple values e.g. "ext=gettext&ext=comment".
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the Document's meta-data was found. The data will be
     *         contained in the response.<br>
     *         NOT FOUND(404) - If a project, project iteration or document
     *         could not be found with the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path(DOCID_RESOURCE_PATH + "/meta")
    @TypeHint(ResourceMeta.class)
    public Response getResourceMetaWithDocId(
            @QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions);

    /**
     * Modifies an existing source document's meta-data.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param messageBody
     *            The document's meta-data.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the Document's meta-data was successfully modified.<br>
     *         NOT FOUND(404) - If a document was not found using the given
     *         parameters.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     * Deprecated. Use {@link #putResourceMetaWithDocId}
     */
    @Deprecated
    @PUT
    @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
    // /r/{id}/meta
            public
            Response putResourceMeta(@PathParam("id") String idNoSlash,
                    ResourceMeta messageBody,
                    @QueryParam("ext") Set<String> extensions);

    /**
     * Modifies an existing source document's meta-data.
     *
     * @param docId
     *            The document identifier.
     * @param messageBody
     *            The document's meta-data.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the Document's meta-data was successfully modified.<br>
     *         NOT FOUND(404) - If a document was not found using the given
     *         parameters.<br>
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @PUT
    @Path(DOCID_RESOURCE_PATH + "/meta")
    public Response putResourceMetaWithDocId(ResourceMeta messageBody,
            @QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions);

}
