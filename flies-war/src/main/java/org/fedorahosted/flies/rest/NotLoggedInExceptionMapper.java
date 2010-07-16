package org.fedorahosted.flies.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.seam.security.NotLoggedInException;

@Provider
public class NotLoggedInExceptionMapper implements ExceptionMapper<NotLoggedInException>
{

   @Override
   public Response toResponse(NotLoggedInException exception)
   {
      return Response.status(Status.UNAUTHORIZED).build();
   }

}