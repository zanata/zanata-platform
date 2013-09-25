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

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.zanata.common.LocaleId;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.resource.TranslationsResource;

import static org.zanata.rest.service.SourceDocResource.RESOURCE_SLUG_TEMPLATE;

/**
 * projectSlug: Project Identifier.
 * iterationSlug: Project Iteration identifier.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface TranslatedDocResource
{
   public static final String SERVICE_PATH = ProjectIterationResource.SERVICE_PATH + "/r";

   /**
    * Retrieves a set of translations for a given locale.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param locale The locale for which to get translations.
    * @param extensions The translation extensions to retrieve (e.g. "comment"). This parameter
    * allows multiple values.
    * @param skeletons Indicates whether to generate untranslated entries or not.
    * @param eTag An Entity tag identifier. Based on this identifier (if provided), the server will decide if it needs
    *             to send a response to the client or not (See return section).
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Successfully retrieved translations. The data will be contained in the response.<br>
    * NOT FOUND(404) - If a project, project iteration or document could not be found with the given parameters. Also
    *                  if no translations are found for the given document and locale.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.<br/>
    * NOT_MODIFIED(304) - If the provided ETag matches the server's stored ETag, it will reply with this code, indicating
    *                     that the last received response is still valid and should be reused.
    */
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   @TypeHint(TranslationsResource.class)
   public Response getTranslations(
         @PathParam("id") String idNoSlash,
         @PathParam("locale") LocaleId locale,
         @QueryParam("ext") Set<String> extensions,
         @QueryParam("skeletons") boolean createSkeletons,
         @HeaderParam(HttpHeaderNames.IF_NONE_MATCH) String eTag
         );

   /**
    * Deletes a set of translations for a given locale. Also deletes any extensions recorded for the translations in
    * question. The system will keep history of the translations.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param locale The locale for which to get translations.
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Successfully deleted the translations.<br>
    * NOT FOUND(404) - If a project, project iteration or document could not be found with the given parameters.
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response deleteTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale);

   /**
    * Updates the translations for a document and a locale.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param locale The locale for which to get translations.
    * @param messageBody The translations to modify.
    * @param extensions The translation extension types to modify (e.g. "comment"). This parameter
    * allows multiple values.
    * @param merge Indicates how to deal with existing translations (valid options: 'auto', 'import'). Import will
    * overwrite all current values with the values being pushed (even empty ones), while Auto will check the history
    * of your translations and will not overwrite any translations for which it detects a previous value is being pushed.
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Translations were successfully updated.<br>
    * NOT FOUND(404) - If a project, project iteration or document could not be found with the given parameters.<br>
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * BAD REQUEST(400) - If there are problems with the parameters passed. i.e. Merge type is not one of the accepted
    * types. This response should have a content message indicating a reason.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response putTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, TranslationsResource messageBody, @QueryParam("ext") Set<String> extensions, @QueryParam("merge") @DefaultValue("auto") String merge);

}
