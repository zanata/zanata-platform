package org.fedorahosted.flies.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoSuchEntityExceptionMapper implements
		ExceptionMapper<NoSuchEntityException> {

	@Override
	public Response toResponse(NoSuchEntityException exception) {
		return Response.status(Status.NOT_FOUND).entity(exception.getMessage()).build();
	}

}