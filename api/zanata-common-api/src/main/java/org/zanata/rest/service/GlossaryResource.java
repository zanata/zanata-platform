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

import java.io.Serializable;
import java.util.List;

import javax.annotation.CheckForNull;
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

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.common.LocaleId;
import org.zanata.rest.GlossaryFileUploadForm;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryInfo;
import org.zanata.rest.dto.GlossaryResults;
import org.zanata.rest.dto.QualifiedName;
import org.zanata.rest.dto.ResultList;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Glossary management
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Path(GlossaryResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
        MediaType.APPLICATION_OCTET_STREAM })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@ResourceLabel("Glossary")
@StatusCodes({
        @ResponseCode(code = 500,
                condition = "If there is an unexpected error in the server while performing this operation")
})
public interface GlossaryResource extends Serializable {
    public static final String SERVICE_PATH = "/glossary";

    /**
     * Maximum result for per page.
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * Qualified name for Global/default glossary
     */
    public static final String GLOBAL_QUALIFIED_NAME = "global/default";

    /**
     * Return default global glossary qualifiedName
     */
    @GET
    @Path("/qualifiedName")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(QualifiedName.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Returns the qualified name " +
                    "for the system-wide glossary"),
    })
    public Response getQualifiedName();

    /**
     * Return source locales available for all glossary entries
     *
     * @param qualifiedName
     *          Qualified name of glossary, default to {@link #GLOBAL_QUALIFIED_NAME}
     */
    @GET
    @Path("/info")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(GlossaryInfo.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Returns the global glossary " +
                    "information"),
    })
    public Response getInfo(
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName);

    /**
     * Returns a subset of Glossary entries for the given source and translation
     * locale as indicated by the paging parameters.
     *
     * @see {@link org.zanata.rest.dto.GlossaryEntry} for details on the result
     * list's contents.
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
     * @param qualifiedName
     *            Qualified name of glossary, default to {@link #GLOBAL_QUALIFIED_NAME}
     */
    @GET
    @Path("/entries")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_JSON })
    @TypeHint(ResultList.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Response containing Glossary " +
                    "entries for the given locale."),
            @ResponseCode(code = 400, condition = "If page or sizePerPage are " +
                    "negative, or sizePerPage is greater than 1000"),
    })
    public Response getEntries(
            @DefaultValue("en-US") @QueryParam("srcLocale") LocaleId srcLocale,
            @QueryParam("transLocale") LocaleId transLocale,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("1000") @QueryParam("sizePerPage") int sizePerPage,
            @QueryParam("filter") String filter,
            @QueryParam("sort") String fields,
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName);

    /**
     * Returns Glossary entries based on a fuzzy text search.
     *
     * @see {@link org.zanata.rest.dto.GlossaryEntry} for details on the result
     * list's contents.
     *
     * @param srcLocale
     *            Source locale
     * @param transLocale
     *            Translation locale
     * @param maxResults
     *            Maximum results for global and project queries. May return
     *            up to double this number. Default: 20
     * @param searchText
     *            Text containing terms to match in the search.
     * @param projectSlug
     *            (optional) Project slug if a project glossary should be searched
     *            in addition to the global glossary.
     */
    @GET
    @Path("/search")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_JSON })
    @TypeHint(ResultList.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Response containing Glossary " +
                    "entries for the given search parameters."),
            @ResponseCode(code = 400, condition = "When maxResults is not strictly positive or is more than 1000"),
            @ResponseCode(code = 400, condition = "When searchText is missing"),
            @ResponseCode(code = 400, condition = "When transLocale is missing"),
            @ResponseCode(code = 400, condition = "When there is an error parsing the searchText"),
    })
    Response search(
            @DefaultValue("en-US") @QueryParam("srcLocale") LocaleId srcLocale,
            @CheckForNull @QueryParam("transLocale") LocaleId transLocale,
            @DefaultValue("20") @QueryParam("maxResults") int maxResults,
            @CheckForNull @QueryParam("searchText") String searchText,
            @CheckForNull @QueryParam("project") String projectSlug);

    /**
     * Get the details for a set of glossary terms.
     *
     * Includes source details, and details from the given locale.
     *
     * @param locale include locale-specific detail for this locale
     * @param termIds id for glossary terms in the default locale, found in
     *                results of {@link #search(LocaleId, LocaleId, int, String, String)}
     * @return Source and target glossary details.
     */
    @GET
    @Path("/details/{locale}")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_JSON })
    // TODO when GWT is removed, move the GlossaryDetails class to this module
    //      and add the type hint.
    // @TypeHint(GlossaryDetails[].class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Details successfully found")
    })
    Response getDetails(
            @CheckForNull @PathParam("locale") LocaleId locale,
            @CheckForNull @QueryParam("termIds") List<Long> termIds);

    /**
     * Download all glossary entries as a file
     *
     * @param fileType 'po' or 'csv' (case insensitive) are currently supported
     * @param locales optional comma separated list of languages required.
     * @param qualifiedName
     *            Qualified name of glossary, default to {@link #GLOBAL_QUALIFIED_NAME}
     * @return A file stream with the glossary contents in the specified format
     */
    @GET
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "File is successfully built " +
                    "and served")
    })
    public Response downloadFile(
        @DefaultValue("csv") @QueryParam("fileType") String fileType,
        @QueryParam("locales") String locales,
        @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName);

    /**
     * Create or update glossary entries.
     * Glossary Terms with a locale different from the given locale parameter
     * will be ignored.
     *
     * @param glossaryEntries The glossary entries to create/update
     * @param qualifiedName
     *            Qualified name of glossary, defaults to {@link #GLOBAL_QUALIFIED_NAME}
     * @param locale
     *            Locale to which the given glossary entries belong
     */
    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/entries")
    @TypeHint(GlossaryResults.class)
    @StatusCodes({
        @ResponseCode(code = 200, condition = "The glossary entries were " +
                "successfully created or updated"),
        @ResponseCode(code = 401, condition = "The user does not have the proper" +
                " permissions to perform this operation")
    })
    public Response post(List<GlossaryEntry> glossaryEntries,
            @QueryParam("locale") String locale,
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName);

    /**
     * Upload glossary file (currently supported formats: po, csv)
     *
     *
     * @param form Multi-part form with the following named parts: <br>
     *             file: The file contents <br>
     *             srcLocale: Source locale for the glossary entries <br>
     *             transLocale: Translation locale for the glossary entries <br>
     *             fileName: The name of the file being uploaded<br>
     *             qualifiedName: The qualified name for the glossary<br>
     */
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @TypeHint(GlossaryResults.class)
    @StatusCodes({
            @ResponseCode(code = 201, condition = "Files successfully uploaded"),
            @ResponseCode(code = 401, condition = "The user does not have the proper" +
                    " permissions to perform this operation")
    })
    public Response upload(@MultipartForm GlossaryFileUploadForm form);

    /**
     *
     * Delete a glossary entry.
     *
     * @param id id for source glossary term
     * @param qualifiedName
     *      Qualified name of glossary, defaults to {@link #GLOBAL_QUALIFIED_NAME}
     * @return the removed glossary entry
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/entries/{id}")
    @TypeHint(GlossaryEntry.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The glossary entry was successfully deleted"),
            @ResponseCode(code = 401, condition = "The user does not have the proper" +
                    " permissions to perform this operation")
    })
    public Response deleteEntry(@PathParam("id") Long id,
        @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName);

    /**
     * Delete all entries in a glossary.
     *
     * @param qualifiedName
     *            Qualified name of glossary, defaults to {@link #GLOBAL_QUALIFIED_NAME}
     *
     * @return The number of deleted glossary entries
     *
     * @responseExample
     *
     * TODO Need to define a 'produces' header
     */
    @DELETE
    @TypeHint(Integer.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The glossary was deleted"),
            @ResponseCode(code = 401, condition = "The user does not have the proper" +
                    " permissions to perform this operation")
    })
    public Response deleteAllEntries(
            @DefaultValue(GLOBAL_QUALIFIED_NAME) @QueryParam("qualifiedName") String qualifiedName);

}
