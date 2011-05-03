package org.zanata.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Provider
public class InvalidStateExceptionMapper implements ExceptionMapper<InvalidStateException>
{
   Log log = Logging.getLog(InvalidStateExceptionMapper.class);

   @Override
   public Response toResponse(InvalidStateException e)
   {
      InvalidValue[] invalidValues = e.getInvalidValues();
      for (InvalidValue invalidValue : invalidValues)
      {
         log.error("Invalid state for bean {0}: {1}", e, invalidValue.getBean(), invalidValue);
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
   }

}
