package org.zanata.rest.service;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import javax.inject.Inject;
import javax.inject.Named;

import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kotlin.Pair;

@Dependent
@Named("restUtils")
public class RestUtils {
    private static final Logger log = LoggerFactory.getLogger(RestUtils.class);

    @Inject
    Validator validator;

    public static ServerResponse copyIfNotServerResponse(Response response) {
        if (response instanceof ServerResponse) {
            return (ServerResponse) response;
        }
        Object entity = response.getEntity();
        int status = response.getStatus();
        Headers<Object> metadata = new Headers<>();
        if (response.getMetadata() != null) {
            metadata.putAll(response.getMetadata());
        }
        return new ServerResponse(entity, status, metadata);
    }

    /**
     * Validate Hibernate Validator based constraints.
     *
     * If validation fails a WebApplicationException with status BAD_REQUEST is
     * thrown, with a message describing the validation errors.
     *
     * @param <T>
     *            class of entity to validate
     * @param entity
     *            Hibernate-validator annotated entity
     */
    public <T> void validateEntity(T entity) {
        validator.getConstraintsForClass(entity.getClass());
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Request body contains invalid values:\n");
            for (ConstraintViolation<T> violation : violations) {
                message.append(violation.getPropertyPath());
                message.append(": ");
                message.append(violation.getMessage());
                message.append("\n");
            }
            log.debug("Bad Request: {}", message);
            throw new NoLogWebApplicationException(Response
                    .status(Status.BAD_REQUEST).entity(message.toString())
                    .build());
        }
    }

    @Nullable
    public static Response checkParams(List<Tuple2<Object, String>> params) {
        StringBuilder msg = new StringBuilder("Null or empty parameters: ");
        boolean error = false;
        for (Tuple2<Object, String> param : params) {
            if (param.v1 == null || param.v1 instanceof String &&
                    ((String) param.v1).isEmpty()) {
                msg.append(param.v2);
                msg.append(" ");
                error = true;
            }
        }
        if (error) {
            log.debug("Bad Request: {}", msg);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg.toString())
                    .build();
        }
        return null;
    }
}
