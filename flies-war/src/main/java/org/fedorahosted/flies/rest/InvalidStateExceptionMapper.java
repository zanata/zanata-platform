package org.fedorahosted.flies.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.validator.InvalidStateException;

@Provider
public class InvalidStateExceptionMapper implements ExceptionMapper<InvalidStateException>
{

   @Override
   public Response toResponse(InvalidStateException exception)
   {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
   }

}
