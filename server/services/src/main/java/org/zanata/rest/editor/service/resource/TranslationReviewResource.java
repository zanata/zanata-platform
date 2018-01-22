package org.zanata.rest.editor.service.resource;

import org.zanata.rest.editor.dto.ReviewData;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Earl Floden <a href="mailto:efloden@redhat.com">efloden@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface TranslationReviewResource {

    String SERVICE_PATH = "/review/trans/{localeId}";

    /**
     * Update/insert translation
     *
     * @param localeId
     *            locale id of translation
     * @param data
     *            information of updated translation
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Update translation success <br>
     *         Forbidden(403) - If user is not authorized to perform save.<br>
     *         NOT FOUND(404) - If a TextFlow not found.<br>
     *         Conflict(409) - If revision is not the current version on the
     *         server INTERNAL SERVER ERROR(500) - If there is an unexpected
     *         error in the server while performing this operation.
     */
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_JSON })
    Response put(@PathParam("localeId") String localeId,
            ReviewData data);
}
