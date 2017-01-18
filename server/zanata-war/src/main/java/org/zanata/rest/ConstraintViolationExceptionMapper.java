package org.zanata.rest;

import org.slf4j.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

@Provider
public class ConstraintViolationExceptionMapper implements
        ExceptionMapper<ConstraintViolationException> {
    private static final Logger log = org.slf4j.LoggerFactory
            .getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> invalidValues = e.getConstraintViolations();
        for (ConstraintViolation<?> invalidValue : invalidValues) {
            log.error("Invalid state for leaf bean {}: {}", e,
                    invalidValue.getLeafBean(), invalidValue);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

}
