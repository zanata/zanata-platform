package org.zanata.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.HibernateException;

@Provider
@Slf4j
public class HibernateExceptionMapper implements
        ExceptionMapper<HibernateException> {

    @Override
    public Response toResponse(HibernateException exception) {
        log.error("Hibernate Exception in REST request", exception);
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
