package org.zanata.rest;

import org.hibernate.HibernateException;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class HibernateExceptionMapper implements
        ExceptionMapper<HibernateException> {

    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(HibernateExceptionMapper.class);

    @Override
    public Response toResponse(HibernateException exception) {
        log.error("Hibernate Exception in REST request", exception);
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
