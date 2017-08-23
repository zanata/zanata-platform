package org.zanata.rest.editor.service.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface StatisticResource extends Serializable {
    public static final String SERVICE_PATH = "/stats";

    /**
     * Retrieves word and message statistics of document.
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
     *         OK(200) - Response containing a list of statistics(word and messages). <br>
     *         NOT FOUND(404) - If a document could not be found for the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/project/{projectSlug}/version/{versionSlug}/doc/{docId}/locale/{localeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDocumentStatistics(
        @PathParam("projectSlug") String projectSlug,
        @PathParam("versionSlug") String versionSlug,
        @PathParam("docId") String docId,
        @PathParam("localeId") String localeId);
}
