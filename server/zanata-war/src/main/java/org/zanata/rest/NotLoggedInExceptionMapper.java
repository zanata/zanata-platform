package org.zanata.rest;

import org.zanata.exception.NotLoggedInException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotLoggedInExceptionMapper implements
        ExceptionMapper<NotLoggedInException> {

    @Override
    public Response toResponse(NotLoggedInException exception) {
        return Response.status(Status.UNAUTHORIZED).build();
    }

}
