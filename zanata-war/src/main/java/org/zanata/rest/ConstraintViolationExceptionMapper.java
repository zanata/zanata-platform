package org.zanata.rest;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class ConstraintViolationExceptionMapper implements
        ExceptionMapper<ConstraintViolationException> {
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
