package org.fedorahosted.flies.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static final Log log = Logging.getLog(RuntimeExceptionMapper.class);

	@Override
	public Response toResponse(RuntimeException exception) {
		log.error("unhandled exception", exception);
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

}