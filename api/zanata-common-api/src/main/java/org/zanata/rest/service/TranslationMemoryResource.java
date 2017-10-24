/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.common.LocaleId;

/**
 * Allows clients to [push and] pull translation memories.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Path(TranslationMemoryResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML /* , "application/x-tmx" */})
@Consumes({ MediaType.APPLICATION_XML /* , "application/x-tmx" */})
@ResourceLabel("Translation Memory")
@StatusCodes({
        @ResponseCode(code = 500,
                condition = "If there is an unexpected error in the server while performing this operation")
})
public interface TranslationMemoryResource {
    public static final String SERVICE_PATH = "/tm";

    public static final String PREFERRED_MEDIA_TYPE = MediaType.APPLICATION_XML;

    /**
     * Returns all translation memory in the system
     *
     * @param srcLocale
     *            Source locale
     * @param locale
     *            Translation locale
     */
    @GET
    @Path("all")
    @TypeHint(StreamingOutput.class)
    public Response getAllTranslationMemory(
            @QueryParam("srcLocale") @Nullable LocaleId srcLocale,
            @QueryParam("locale") @Nullable LocaleId locale);

    /**
     * Retrieves translation memory for a project
     *
     * @param projectSlug
     *          Project identifier
     * @param srcLocale
     *          Source locale
     * @param locale
     *          Translation locale
     */
    @GET
    @Path("projects/{projectSlug}")
    @TypeHint(StreamingOutput.class)
    public Response getProjectTranslationMemory(
            @PathParam("projectSlug") @Nonnull String projectSlug,
            @QueryParam("srcLocale") @Nullable LocaleId srcLocale,
            @QueryParam("locale") @Nullable LocaleId locale);

    /**
     * Retrieves translation memory for a project version
     *
     * @param projectSlug
     *          Project identifier
     * @param iterationSlug
     *          Project version identifier
     * @param srcLocale
     *          Source locale
     * @param locale
     *          Translation locale
     */
    @GET
    @Path("projects/{projectSlug}/iterations/{iterationSlug}")
    @TypeHint(StreamingOutput.class)
    public Response getProjectIterationTranslationMemory(
            @PathParam("projectSlug") @Nonnull String projectSlug,
            @PathParam("iterationSlug") @Nonnull String iterationSlug,
            @QueryParam("srcLocale") @Nullable LocaleId srcLocale,
            @QueryParam("locale") @Nullable LocaleId locale);

    /**
     * Retrieves a collection of translation memory
     *
     * @param slug
     *          Identifier for a translation memory collection
     */
    @GET
    @Path("{slug}")
    @TypeHint(StreamingOutput.class)
    public Response
            getTranslationMemory(@PathParam("slug") @Nonnull String slug);

    /**
     * Updates a collection of translation memory
     *
     * @param slug
     *          Identifier for a translation memory collection
     * @param input
     *          Input stream of TMX file
     */
    @POST
    @Path("{slug}")
    @Consumes(MediaType.WILDCARD)
    public Response updateTranslationMemory(@PathParam("slug") String slug,
            InputStream input) throws Exception;

    /**
     * Delete a collection of translation memory
     *
     * @param slug
     *          Identifier for a translation memory collection
     *
     * @return message of the operation
     */
    @DELETE
    @Path("{slug}/transunits")
    @TypeHint(String.class)
    public Object deleteTranslationUnits(@PathParam("slug") String slug);
}
