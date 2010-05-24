package org.fedorahosted.flies.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.HibernateException;

@Provider
public class HibernateExceptionMapper implements
		ExceptionMapper<HibernateException> {

	@Override
	public Response toResponse(HibernateException exception) {
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

}
