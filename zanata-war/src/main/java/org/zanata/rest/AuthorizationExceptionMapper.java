package org.zanata.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.seam.security.AuthorizationException;

@Provider
public class AuthorizationExceptionMapper extends
        RateLimitingAwareExceptionMapper implements ExceptionMapper<AuthorizationException> {

    @Override
    public Response toResponse(AuthorizationException exception) {
        releaseSemaphoreBeforeReturnResponse();
        return Response.status(Status.UNAUTHORIZED).build();
    }

}
