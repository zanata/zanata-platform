package org.zanata.rest.editor.service.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.editor.MediaTypes;
import org.zanata.rest.editor.dto.TranslationData;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface TranslationResource extends Serializable {

    public static final String SERVICE_PATH = "/trans/{localeId}";

    /**
     * Retrieves a list TextFlowTarget in given textFlow id and localeId.
     *
     * @param localeId
     *            locale id of translation
     *
     * @param ids
     *            list textFlow's id (comma separated)
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of TextFlowTarget. <br>
     *         Forbidden(403) - If ids list is too long<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_TRANSLATION_JSON,
            MediaType.APPLICATION_JSON })
    public Response get(@PathParam("localeId") String localeId,
            @QueryParam("ids") String ids);

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
    @Consumes({ MediaTypes.APPLICATION_ZANATA_TRANSLATION_DATA_JSON,
            MediaType.APPLICATION_JSON })
    public Response put(@PathParam("localeId") String localeId,
            TranslationData data);
}
