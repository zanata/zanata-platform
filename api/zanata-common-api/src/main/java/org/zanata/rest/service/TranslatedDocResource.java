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

import static org.zanata.rest.service.SourceDocResource.DOCID_RESOURCE_PATH;
import static org.zanata.rest.service.SourceDocResource.RESOURCE_SLUG_TEMPLATE;

import java.io.Serializable;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Translated document API. This API uses format-independent data structures. For
 * format specific source document access see {@link FileResource}
 *
 * <br>projectSlug: Project Identifier.
 * <br>iterationSlug: Project Iteration identifier.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Path(TranslatedDocResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Translated Documents")
@StatusCodes({
        @ResponseCode(code = 500,
                condition = "If there is an unexpected error in the server while performing this operation")
})
public interface TranslatedDocResource extends Serializable {
    @SuppressWarnings("deprecation")
    public static final String SERVICE_PATH = ProjectIterationResource.SERVICE_PATH;

    /**
     * Retrieves a set of translations for a given locale.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param locale
     *            The locale for which to get translations.
     * @param extensions
     *            The translation extensions to retrieve (e.g. "comment"). This
     *            parameter allows multiple values.
     * @param skeletons
     *            Indicates whether to generate untranslated entries or not.
     * @param eTag
     *            An Entity tag identifier. Based on this identifier (if
     *            provided), the server will decide if it needs to send a
     *            response to the client or not (See return section).
     * Deprecated. Use {@link #getTranslationsWithDocId}
     */
    @GET
    @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
    // /r/{id}/translations/{locale}
    @TypeHint(TranslationsResource.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Successfully retrieved translations. " +
                    "The translation data will be contained in the response."),
            @ResponseCode(code = 404, condition = "The project, version, or document could" +
                    " not be found with the given parameters. Also, if no translations are" +
                    " found for the given document and locale."),
            @ResponseCode(code = 304, condition = "If the provided ETag matches the server's" +
                    " stored ETag, indicating that the last received response is still valid" +
                    " and should be reused."),
    })
    @Deprecated
    public
    Response getTranslations(@PathParam("id") String idNoSlash,
            @PathParam("locale") LocaleId locale,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("skeletons") boolean skeletons,
            @HeaderParam(HttpHeaderNames.IF_NONE_MATCH) String eTag);

    /**
     * Retrieves a set of translations for a given locale.
     *
     * @param docId
     *            The document identifier.
     * @param locale
     *            The locale for which to get translations.
     * @param extensions
     *            The translation extensions to retrieve (e.g. "comment"). This
     *            parameter allows multiple values.
     * @param createSkeletons
     *            Indicates whether to generate untranslated entries or not.
     * @param eTag
     *            An Entity tag identifier. Based on this identifier (if
     *            provided), the server will decide if it needs to send a
     *            response to the client or not (See return section).
     */
    @GET
    @Path(DOCID_RESOURCE_PATH + "/translations/{locale}")
    @TypeHint(TranslationsResource.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Successfully retrieved translations. " +
                    "The translation data will be contained in the response."),
            @ResponseCode(code = 404, condition = "The project, version, or document could" +
                    " not be found with the given parameters. Also, if no translations are" +
                    " found for the given document and locale."),
            @ResponseCode(code = 304, condition = "If the provided ETag matches the server's" +
                    " stored ETag, indicating that the last received response is still valid" +
                    " and should be reused."),
    })
    public Response getTranslationsWithDocId(
            @PathParam("locale") LocaleId locale,
            @QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("skeletons") boolean createSkeletons,
            @HeaderParam(HttpHeaderNames.IF_NONE_MATCH) String eTag);

    /**
     * Deletes a set of translations for a given locale. Also deletes any
     * extensions recorded for the translations in question. The system will
     * keep history of the translations.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param locale
     *            The locale for which to get translations.
     * Deprecated. Use {@link #deleteTranslationsWithDocId}
     */
    @Deprecated
    @DELETE
    @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
    @TypeHint(TypeHint.NO_CONTENT.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Successfully deleted the translations."),
            @ResponseCode(code = 404, condition = "If a project, project iteration or document" +
                    " could not be found with the given parameters."),
            @ResponseCode(code = 401, condition = "If the user does not have the proper" +
                    " permissions to perform this operation."),
    })
    // /r/{id}/translations/{locale}
    public
    Response deleteTranslations(@PathParam("id") String idNoSlash,
            @PathParam("locale") LocaleId locale);

    /**
     * Deletes a set of translations for a given locale. Also deletes any
     * extensions recorded for the translations in question. The system will
     * keep history of the translations.
     *
     * @param docId
     *            The document identifier.
     * @param locale
     *            The locale for which to get translations.
     */
    @DELETE
    @Path(DOCID_RESOURCE_PATH + "/translations/{locale}")
    @TypeHint(TypeHint.NO_CONTENT.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Successfully deleted the translations."),
            @ResponseCode(code = 404, condition = "If a project, project iteration or document" +
                    " could not be found with the given parameters."),
            @ResponseCode(code = 401, condition = "If the user does not have the proper" +
                    " permissions to perform this operation."),
    })
    public Response deleteTranslationsWithDocId(
            @PathParam("locale") LocaleId locale,
            @QueryParam("docId") @DefaultValue("")  String docId);

    /**
     * Updates the translations for a document and a locale.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param locale
     *            The locale for which to get translations.
     * @param messageBody
     *            The translations to modify.
     * @param extensions
     *            The translation extension types to modify (e.g. "comment").
     *            This parameter allows multiple values.
     * @param merge
     *            Indicates how to deal with existing translations (valid
     *            options: 'auto', 'import'). Import will overwrite all current
     *            values with the values being pushed (even empty ones), while
     *            Auto will check the history of your translations and will not
     *            overwrite any translations for which it detects a previous
     *            value is being pushed.
     * Deprecated. Use {@link #putTranslationsWithDocId}
     */
    @Deprecated
    @PUT
    @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Translations were successfully updated."),
            @ResponseCode(code = 404, condition = "If a project, project iteration or document" +
                    " could not be found with the given parameters."),
            @ResponseCode(code = 401, condition = "If the user does not have the proper" +
                    " permissions to perform this operation."),
            @ResponseCode(code = 400, condition = "If there are problems with the passed parameters." +
                    " e.g. Merge type is not one of the accepted types. This response should have a" +
                    " content message indicating a reason.",
                    type = @TypeHint(String.class))
    })
    // /r/{id}/translations/{locale}
    public
    Response putTranslations(@PathParam("id") String idNoSlash,
            @PathParam("locale") LocaleId locale,
            TranslationsResource messageBody,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("merge") @DefaultValue("auto") String merge);

    /**
     * Updates the translations for a document and a locale.
     *
     * @param docId
     *            The document identifier.
     * @param locale
     *            The locale for which to get translations.
     * @param messageBody
     *            The translations to modify.
     * @param extensions
     *            The translation extension types to modify (e.g. "comment").
     *            This parameter allows multiple values.
     * @param merge
     *            Indicates how to deal with existing translations (valid
     *            options: 'auto', 'import'). Import will overwrite all current
     *            values with the values being pushed (even empty ones), while
     *            Auto will check the history of your translations and will not
     *            overwrite any translations for which it detects a previous
     *            value is being pushed.
     */
    @PUT
    @Path(DOCID_RESOURCE_PATH + "/translations/{locale}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Translations were successfully updated."),
            @ResponseCode(code = 404, condition = "If a project, project iteration or document" +
                    " could not be found with the given parameters."),
            @ResponseCode(code = 401, condition = "If the user does not have the proper" +
                    " permissions to perform this operation."),
            @ResponseCode(code = 400, condition = "If there are problems with the passed parameters." +
                    " e.g. Merge type is not one of the accepted types. This response should have a" +
                    " content message indicating a reason.",
                    type = @TypeHint(String.class))
    })
    public Response putTranslationsWithDocId(
            @PathParam("locale") LocaleId locale,
            TranslationsResource messageBody,
            @QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("merge") @DefaultValue("auto") String merge);

}
