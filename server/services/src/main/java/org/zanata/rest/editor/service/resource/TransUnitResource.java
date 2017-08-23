package org.zanata.rest.editor.service.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.editor.MediaTypes;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface TransUnitResource extends Serializable {

    public static final String SERVICE_PATH = "/source+trans/{localeId}";

    /**
     * Retrieves a list TextFlow with TextFlowTarget in given textFlow id and
     * localeId.
     *
     * @param localeId
     *            locale id of translation
     * @param ids
     *            list textFlow's id (comma separated)
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a list of TextFlow with
     *         TextFlowTarget. <br>
     *         Forbidden(403) - If ids list is too long<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_TRANS_UNIT_JSON,
            MediaType.APPLICATION_JSON })
    public Response get(@PathParam("localeId") String localeId,
            @QueryParam("ids") String ids);
}
