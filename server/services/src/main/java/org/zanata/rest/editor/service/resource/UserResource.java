package org.zanata.rest.editor.service.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.editor.MediaTypes;
import org.zanata.rest.service.GlossaryService;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface UserResource extends Serializable {

    String SERVICE_PATH = "/user";

    /**
     * Retrieves user information of current authenticated user
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing user information <br>
     *         NOT FOUND(404) - If no authenticated user found. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_USER_JSON,
            MediaType.APPLICATION_JSON })
    Response getMyInfo();

    /**
     * Retrieves user information
     *
     * @param username username
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing user information <br>
     *         NOT FOUND(404) - If no authenticated user found. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_USER_JSON,
            MediaType.APPLICATION_JSON })
    @Path("/{username:[a-z\\d_]{3,20}}")
    Response getUserInfo(@PathParam("username") String username);


    /**
     * Retrieves account information of current authenticated user.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing account information {@link org.zanata.rest.dto.Account}<br>
     *         FORBIDDEN(403) - If no authenticated user found. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @Path("/myaccount")
    @GET
    Response getAccountDetails();


    /**
     * Get permission for Glossary interaction of current authenticated user.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing account information {@link org.zanata.rest.editor.dto.org.zanata.rest.editor.dto.Permission}<br>
     *         FORBIDDEN(403) - If no authenticated user found. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/permission/glossary")
    @GET
    Response getGlossaryPermission(
            @QueryParam("qualifiedName") @DefaultValue(GlossaryService.GLOBAL_QUALIFIED_NAME) String qualifiedName);

    /**
     * Get permission for Locales interaction of current authenticated user.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing permission information {@link org.zanata.rest.editor.dto.org.zanata.rest.editor.dto.Permission}<br>
     *         FORBIDDEN(403) - If no authenticated user found. <br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/permission/locales")
    @GET
    Response getLocalesPermission();
}

