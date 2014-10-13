package org.zanata.rest.editor.service.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.service.RestConstants;
import org.zanata.rest.editor.MediaTypes;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface ProjectVersionResource {
    public static final String VERSION_SLUG_TEMPLATE = "/{versionSlug:"
            + RestConstants.SLUG_PATTERN + "}";

    public static final String SERVICE_PATH = ProjectResource.SERVICE_PATH
            + ProjectResource.PROJECT_SLUG_TEMPLATE
            + "/version";

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
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_VERSION_JSON,
            MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE)
    public Response getVersion(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug);

    /**
     * Retrieves a full list of locales enabled in project version.
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
    public Response getDocuments(@PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug);

    /**
     * Retrieves a list translation unit with status in a document.
     *
     * @param projectSlug
     *            Project identifier
     * @param versionSlug
     *            Project version identifier
     * @param docId
     *            The document identifier. Some document ids could have forward
     *            slashes ('/') in them which would cause conflicts with the
     *            browser's own url interpreter. For this reason, the supplied
     *            id must have all its '/' characters replaced with commas
     *            (',').
     * @param localeId
     *            target locale
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         NOT FOUND(404) - If a document or locale could not be found for
     *         the given parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_TRANS_UNIT_RESOURCE_JSON,
            MediaType.APPLICATION_JSON })
    @Path(VERSION_SLUG_TEMPLATE + "/doc/{docId}/status/{localeId}")
    public Response getTransUnitStatus(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @PathParam("docId") String docId,
            @PathParam("localeId") String localeId);

}
