package org.zanata.rest.editor.service.resource;

import org.zanata.rest.editor.MediaTypes;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Earl Floden <a href="mailto:efloden@redhat.com">efloden@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface TransUnitHistoryResource {

    String SERVICE_PATH = "/transhist/{localeId}/{transUnitId}/{projectSlug}";

    /**
     * Retrieves a list TextFlowHistory in given textFlow id and localeId.
     *
     * @param localeId
     *            locale id of translation
     *
     * @param transUnitId
     *            textFlow id
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of TextFlowHistory.<br>
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_TRANS_UNIT_JSON,
            MediaType.APPLICATION_JSON })
    Response get(@PathParam("localeId") String localeId,
            @PathParam("transUnitId") Long transUnitId,
            @PathParam("projectSlug") String projectSlug,
            @QueryParam("versionSlug") String versionSlug);
}
