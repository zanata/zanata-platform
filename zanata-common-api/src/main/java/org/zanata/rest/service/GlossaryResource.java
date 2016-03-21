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

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.common.LocaleId;
import org.zanata.rest.GlossaryFileUploadForm;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryInfo;
import org.zanata.rest.dto.GlossaryResults;
import org.zanata.rest.dto.ResultList;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Path(GlossaryResource.SERVICE_PATH)
@ExternallyManagedLifecycle
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
public interface GlossaryResource extends RestResource {
    public static final String SERVICE_PATH = "/glossary";

    /**
     * Maximum result for per page.
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * Return source locales available for all glossary entries
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing all Glossary entries in the system.
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/info")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(GlossaryInfo.class)
    public Response getInfo();

    /**
     * Returns Glossary entries for the given source and translation locale with
     * paging
     *
     * @param srcLocale
     *            Source locale - Required (default value: en-US).
     * @param transLocale
     *            Translation locale
     * @param page
     *            Current request page (default value: 1)
     * @param sizePerPage
     *            Size of entry per page (default/max value: 1000)
     *            {@link #MAX_PAGE_SIZE}
     * @param filter
     *            String filter for source content
     * @param fields
     *            Fields to sort. Comma separated. e.g sort=desc,-part_of_speech
     *            See {@link org.zanata.common.GlossarySortField}
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing all Glossary entries for the given
     *         locale.
     *         Bad request(400) - If page or sizePerPage is negative value, or sizePerPage is more than 1000.
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected
     *         error in the server while performing this operation.
     */
    @GET
    @Path("/entries")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
            MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(ResultList.class)
    public Response getEntries(
            @DefaultValue("en-US") @QueryParam("srcLocale") LocaleId srcLocale,
            @QueryParam("transLocale") LocaleId transLocale,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("1000") @QueryParam("sizePerPage") int sizePerPage,
            @QueryParam("filter") String filter,
            @QueryParam("sort") String fields);

    /**
     * Create or update glossary entry
     * @param glossaryEntries The glossary entries to create/update
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the glossary entry were successfully created/updated.
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/entries")
    @TypeHint(GlossaryResults.class)
    public Response post(List<GlossaryEntry> glossaryEntries);

    /**
     * Upload glossary file (po, cvs)
     *
     * @param form {@link org.zanata.rest.GlossaryFileUploadForm}
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         CREATED(201) - If files successfully uploaded.
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     *
     */
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @TypeHint(GlossaryResults.class)
    public Response upload(@MultipartForm GlossaryFileUploadForm form);

    /**
     *
     * Delete glossary which given id.
     *
     * @param id
     *          id for source glossary term
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the glossary entry were successfully deleted.
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/entries/{id}")
    @TypeHint(GlossaryEntry.class)
    public Response deleteEntry(@PathParam("id") Long id);

    /**
     * Delete all glossary terms.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - If the glossary entries were successfully deleted.
     *         UNAUTHORIZED(401) - If the user does not have the proper
     *         permissions to perform this operation.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @DELETE
    public Response deleteAllEntries();

}
