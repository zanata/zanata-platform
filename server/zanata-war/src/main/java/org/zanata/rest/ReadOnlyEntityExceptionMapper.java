package org.zanata.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ReadOnlyEntityExceptionMapper implements ExceptionMapper<ReadOnlyEntityException>
{

   @Override
   public Response toResponse(ReadOnlyEntityException exception)
   {
      return Response.status(Status.FORBIDDEN).entity(exception.getMessage()).build();
   }

}