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
public interface ProjectResource {

    public static final String PROJECT_SLUG_TEMPLATE = "/{projectSlug:"
            + RestConstants.SLUG_PATTERN + "}";

    public static final String SERVICE_PATH = "/project";

    /**
     * Returns data for a single Project.
     *
     * @param projectSlug project identifier
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Containing the Project data.<br>
     *         NOT FOUND(404) - If a Project could not be found for the given
     *         parameters.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */

    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_JSON })
    @Path(PROJECT_SLUG_TEMPLATE)
    public Response getProject(@PathParam("projectSlug") String projectSlug);
}
