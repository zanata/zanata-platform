package org.zanata.rest.editor.service.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.editor.MediaTypes;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface LocalesResource {

    public static final String SERVICE_PATH = "/locales";

    /**
     * Retrieves a full list of locales enabled in Zanata. The result is
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_LOCALES_JSON,
            MediaType.APPLICATION_JSON })
    public Response get(@QueryParam("filter") String filter,
            @QueryParam("sort") String fields,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("sizePerPage") int sizePerPage);

    /**
     * Retrieve locale details
     */
    @GET
    @Path("/locale/{localeId}")
    @Produces({ MediaTypes.APPLICATION_ZANATA_LOCALES_JSON,
        MediaType.APPLICATION_JSON })
    public Response getDetails(@PathParam("localeId") String localeId);

    /**
     * Retrieves a full list of localized locales for server. The result is
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/ui")
    @Produces({ MediaTypes.APPLICATION_ZANATA_LOCALES_JSON,
        MediaType.APPLICATION_JSON })
    Response getUITranslations();

    /**
     * Delete a locale in Zanata
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Locale is deleted. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @DELETE
    @Path("/locale/{localeId}")
    @Produces({ MediaTypes.APPLICATION_ZANATA_LOCALES_JSON,
        MediaType.APPLICATION_JSON })
    public Response delete(@PathParam("localeId") String localeId);

    /**
     * Create new language in Zanata
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(201) - Locale is added. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @POST
    @Path("/locale")
    @Produces({ MediaTypes.APPLICATION_ZANATA_LOCALES_JSON,
        MediaType.APPLICATION_JSON })
    public Response createLanguage(LocaleDetails localeDetails);
}
