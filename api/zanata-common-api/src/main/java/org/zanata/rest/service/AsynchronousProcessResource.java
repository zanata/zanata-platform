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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Represents a resource for an asynchronous (i.e. background) process. Only
 * certain types of processes are exposed as asynchronous resources.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path(AsynchronousProcessResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Asynchronous Process")
@StatusCodes({
        @ResponseCode(code = 500,
                condition = "If there is an unexpected error in the server while performing this operation")
})
public interface AsynchronousProcessResource extends Serializable {
    public static final String SERVICE_PATH = "/async";

    /**
     * Attempts to start the creation of a source document. NOTE: Still
     * experimental.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
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
     */
    @POST
    @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/r")
    /* Same as SourceDocResourceService.SERVICE_PATH */
    @TypeHint(ProcessStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The contents of the response will indicate the process" +
                    " identifier which may be used to query for its status or a message" +
                    " indicating what happened.")
    })
    @Deprecated
    public ProcessStatus startSourceDocCreation(
            @PathParam("id") String idNoSlash,
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            Resource resource, @QueryParam("ext") Set<String> extensions,
            @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

    /**
     * Attempts to starts the creation or update of a source document. NOTE:
     * Still experimental.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
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
     *
     * Deprecated. Use {@link #startSourceDocCreationOrUpdateWithDocId}
     */
    @Deprecated
    @PUT
    @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}"
            + SourceDocResource.RESOURCE_SLUG_TEMPLATE)
    /* Same as SourceDocResourceService.SERVICE_PATH */
    @TypeHint(ProcessStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The contents of the response will indicate the process" +
                    " identifier which may be used to query for its status or a message" +
                    " indicating what happened.")
    })
    public ProcessStatus startSourceDocCreationOrUpdate(
            @PathParam("id") String idNoSlash,
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            Resource resource, @QueryParam("ext") Set<String> extensions,
            @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

    /**
     * Attempts to starts the creation or update of a source document. NOTE:
     * Still experimental.
     *
     * @param docId
     *            The document identifier.
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
     * @param resource
     *            The document information.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     */
    @PUT
    @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/resource")
    @TypeHint(ProcessStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The contents of the response will indicate the process" +
                    " identifier which may be used to query for its status or a message" +
                    " indicating what happened.")
    })
    public ProcessStatus startSourceDocCreationOrUpdateWithDocId(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            Resource resource, @QueryParam("ext") Set<String> extensions,
            @QueryParam("docId") @DefaultValue("") String docId);

    /**
     * Attempts to start the translation of a document. NOTE: Still
     * experimental.
     *
     * @param idNoSlash
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
     * @param locale
     *            The locale for which to get translations.
     * @param translatedDoc
     *            The translations to modify.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @param merge
     *            Indicates how to deal with existing translations (valid
     *            options: 'auto', 'import'). Import will overwrite all current
     *            values with the values being pushed (even empty ones), while
     *            Auto will check the history of your translations and will not
     *            overwrite any translations for which it detects a previous
     *            value is being pushed.
     * @param assignCreditToUploader
     *            The translator field for all uploaded translations will
     *            be set to the user who performs the upload.
     *
     * Deprecated. Use {@link #startTranslatedDocCreationOrUpdateWithDocId}
     */
    @Deprecated
    @PUT
    @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/r/{id}/translations/{locale}")
    /* Same as TranslatedDocResource.putTranslations */
    @TypeHint(ProcessStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The contents of the response will indicate the process" +
                    " identifier which may be used to query for its status or a message" +
                    " indicating what happened.")
    })
    public
            ProcessStatus startTranslatedDocCreationOrUpdate(
                    @PathParam("id") String idNoSlash,
                    @PathParam("projectSlug") String projectSlug,
                    @PathParam("iterationSlug") String iterationSlug,
                    @PathParam("locale") LocaleId locale,
                    TranslationsResource translatedDoc,
                    @QueryParam("ext") Set<String> extensions,
                    @QueryParam("merge") String merge,
                    @QueryParam("assignCreditToUploader") @DefaultValue("false") boolean assignCreditToUploader);

    /**
     * Attempts to start the translation of a document. NOTE: Still
     * experimental.
     *
     * @param docId
     *            The document identifier.
     * @param projectSlug
     *            Project identifier.
     * @param iterationSlug
     *            Project Iteration identifier.
     * @param locale
     *            The locale for which to get translations.
     * @param translatedDoc
     *            The translations to modify.
     * @param extensions
     *            The document extensions to save with the document (e.g.
     *            "gettext", "comment"). This parameter allows multiple values
     *            e.g. "ext=gettext&ext=comment".
     * @param merge
     *            Indicates how to deal with existing translations (valid
     *            options: 'auto', 'import'). Import will overwrite all current
     *            values with the values being pushed (even empty ones), while
     *            Auto will check the history of your translations and will not
     *            overwrite any translations for which it detects a previous
     *            value is being pushed.
     * @param assignCreditToUploader
     *            The translator field for all uploaded translations will
     *            be set to the user who performs the upload.
     */
    @PUT
    @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/resource/translations/{locale}")
    /* Same as TranslatedDocResource.putTranslations */
    @TypeHint(ProcessStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "The contents of the response will indicate the process" +
                    " identifier which may be used to query for its status or a message" +
                    " indicating what happened.")
    })
    public ProcessStatus startTranslatedDocCreationOrUpdateWithDocId(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("iterationSlug") String iterationSlug,
            @PathParam("locale") LocaleId locale,
            TranslationsResource translatedDoc,
            @QueryParam("docId") @DefaultValue("") String docId,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("merge") String merge,
            @QueryParam("assignCreditToUploader") @DefaultValue("false") boolean assignCreditToUploader);

    /**
     * Obtains the status of a previously started process.
     *
     * @param processId
     *            The process Id (as returned by one of the endpoints that
     *            starts an async process).
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         NOT FOUND(404) - If such a process Id is not found on the server. <br>
     *         OK(200) - On normal circumstances. The response data will have
     *         all information about the status of the running process. INTERNAL
     *         SERVER ERROR(500) - If there is an unexpected error in the server
     *         while performing this operation.
     */
    @GET
    @Path("/{processId}")
    @TypeHint(ProcessStatus.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition =
                    "On normal circumstances. The response data will have" +
                            " all information about the status of the running process"),
            @ResponseCode(code = 404, condition = "If such a process Id is not found on the server.")
    })
    public ProcessStatus getProcessStatus(
            @PathParam("processId") String processId);

}
