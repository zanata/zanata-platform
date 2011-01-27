package net.openl10n.flies.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.HibernateException;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Provider
public class HibernateExceptionMapper implements ExceptionMapper<HibernateException>
{

   Log log = Logging.getLog(HibernateExceptionMapper.class);

   @Override
   public Response toResponse(HibernateException exception)
   {
      log.error("Hibernate Exception in REST request", exception);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
   }

}
