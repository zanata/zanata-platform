package org.zanata.rest;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>
{
   Log log = Logging.getLog(ConstraintViolationExceptionMapper.class);

   @Override
   public Response toResponse(ConstraintViolationException e)
   {
      Set<ConstraintViolation<?>> invalidValues = e.getConstraintViolations();
      for (ConstraintViolation<?> invalidValue : invalidValues)
      {
         log.error("Invalid state for leaf bean {0}: {1}", e, invalidValue.getLeafBean(), invalidValue);
      }
      return Response.status(Status.BAD_REQUEST).build();
   }

}
