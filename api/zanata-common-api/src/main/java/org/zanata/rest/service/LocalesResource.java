package org.zanata.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.LocaleMember;
import org.zanata.rest.dto.SourceLocaleDetails;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface LocalesResource extends Serializable {

    public static final String SERVICE_PATH = "/locales";

    /**
     * Retrieves a full list of locales enabled in Zanata.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@QueryParam("filter") String filter,
            @QueryParam("sort") String fields,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("sizePerPage") int sizePerPage);

    /**
     * Retrieve locale details
     */
    @GET
    @Path("/locale/{localeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(LocaleDetails.class)
    Response getDetails(@PathParam("localeId") String localeId);

    /**
     * Retrieve locale member list
     */
    @GET
    @Path("/locale/{localeId}/members")
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(LocaleMember[].class)
    Response getMembers(@PathParam("localeId") String localeId);

    /**
     * Retrieves a full list of localized locales for server.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a full list of locales. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/ui")
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(LocaleDetails[].class)
    Response getUITranslations();

    /**
     * Returns list of active source locales of all documents for all active projects.
     * Restricted to admin
     *
     * @return
     *    OK 200 containing the list of SourceLocaleDetails
     */
    @GET
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Content contains a list of source locale details")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/source")
    @TypeHint(SourceLocaleDetails[].class)
    Response getSourceLocales();


    /**
     * Retrieves a list of locales that is not added yet in the server.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a list of locales. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(LocaleDetails[].class)
    Response getNewLocales(@QueryParam("filter") String filter,
            @QueryParam("size") @DefaultValue("10") int size);

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
    @Produces(MediaType.APPLICATION_JSON)
    Response delete(@PathParam("localeId") String localeId);

    /**
     * Create a new language in Zanata
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(201) - Locale is added. <br>
     *         OK(200) - Locale is already exist. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @PUT
    @Path("/locale")
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(LocaleDetails.class)
    Response createLanguage(LocaleDetails localeDetails);
}
