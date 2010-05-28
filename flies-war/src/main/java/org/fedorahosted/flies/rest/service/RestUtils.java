package org.fedorahosted.flies.rest.service;

import java.io.InputStream;
import java.io.Serializable;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;

public class RestUtils {

	/**
	 * Validate Hibernate Validator based constraints.
	 * 
	 * If validation fails a WebApplicationException with status BAD_REQUEST 
	 * is thrown, with a message describing the validation errors.
	 * 
	 * @param <T> class of entity to validate
	 * @param entity Hibernate-validator annotated entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> void validateEntity(T entity) {
		ClassValidator<T> validator = new ClassValidator(entity.getClass());
		if(validator.hasValidationRules() ) {
			InvalidValue[] invalidValues = validator.getInvalidValues(entity);
			if(invalidValues.length != 0) {
				StringBuilder message = new StringBuilder();
				message.append("Request body contains invalid values:\n");
				for(InvalidValue invalidValue : invalidValues) {
					message.append(invalidValue.getPropertyPath());
					message.append(">");
					message.append(invalidValue.getPropertyName());
					message.append(": ");
					message.append(invalidValue.getMessage());
					message.append("\n");
				}
				throw new WebApplicationException(
						Response.status(Status.BAD_REQUEST).entity(message.toString()).build());
			}
		}
	}
	
	public  static <T> T unmarshall(Class<T> entityClass, InputStream is, MediaType requestContentType, MultivaluedMap<String,String> requestHeaders) {
		MessageBodyReader<T> reader = SeamResteasyProviderFactory.getInstance()
				.getMessageBodyReader(entityClass, entityClass,
						entityClass.getAnnotations(), requestContentType);
		if (reader == null) {
			throw new RuntimeException(
					"Unable to find MessageBodyReader for content type "
							+ requestContentType);
		}
		T entity;
		try {
			entity = reader.readFrom(entityClass, entityClass, entityClass
					.getAnnotations(), requestContentType, requestHeaders, is);
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Unable to read request body").build());
		}
		
		return entity;
	}
	
	
}
