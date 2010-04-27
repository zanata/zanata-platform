package org.fedorahosted.flies.rest.service;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.TextFlow;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;

@Name("translationResourcesService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/resources")
public class TranslationResourcesService {

	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@In
	ProjectContainerDAO projectContainerDAO;

	@In
	DocumentDAO documentDAO;
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private Headers headers;
	
	@HeaderParam("Content-Type")
	private MediaType requestContentType;

	@In
	Session session;
	
	@Context
	private UriInfo uri;

	
	@GET
	@Produces
	public Response get() {
		HProjectContainer hProjectContainer = projectContainerDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectContainer == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok().build();
		
	}

	@POST
	public Response post() {
		return Response.ok().build();
	}

	@Logger 
	Log log;
	
	@PUT
	@Path("/r/{id}/source")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Restrict("#{identity.loggedIn}")
	public Response putSource(@PathParam("id") String id, @HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch, InputStream messageBody) {

		HProjectContainer hProjectContainer = projectContainerDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectContainer == null) {
			return Response.status(Status.NOT_FOUND).entity("container not found").build();
		}
		
		EntityTag etag = documentDAO.getETag(hProjectContainer, id);

		HDocument document;
		
		if(etag == null) {
			// this has to be a create operation

			if(ifMatch != null) {
				// the caller expected an existing resource at this location 
				return Response.status(Status.NOT_FOUND).build();
			}

			document = new HDocument();
			document.setProject(hProjectContainer);
			SourceResource entity = unmarshallEntity(SourceResource.class, messageBody);
			transfer(entity, document);
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}
		else {
			SourceResource entity = unmarshallEntity(SourceResource.class, messageBody);
			document = documentDAO.getByDocId(hProjectContainer, id);
			transfer(entity, document);
		}
	
		
		try {
			ResponseBuilder response;
			if (!session.contains(document)) {
				session.save(document);
				session.flush();
				response =  Response.created( uri.getAbsolutePath() );
			} else {
				response = Response.ok();
			}
			session.flush();
			etag = documentDAO.getETag(hProjectContainer, id);
			return response.tag(etag).build();
			
		} catch (InvalidStateException e) {
			String message = 
				String.format(
					"Document '%s' (%s:%s) is invalid: \n %s", 
					id, projectSlug, iterationSlug, StringUtils.join(e.getInvalidValues(),"\n"));
			log.warn(message + '\n' + document);
			log.debug(e,e);
			return Response.status(Status.BAD_REQUEST).entity(message)
					.build();
		} catch (HibernateException e) {
			log.error("Hibernate exception", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
		}
	}
	
	@GET
	@Path("/r/{id}/source")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getSource(@PathParam("id") String id, @HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		HProjectContainer hProjectContainer = projectContainerDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectContainer == null) {
			return Response.status(Status.NOT_FOUND).entity("container not found").build();
		}

		EntityTag etag = documentDAO.getETag(hProjectContainer, id);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		HDocument doc = documentDAO.getByDocId(hProjectContainer, id);

		if(doc == null) {
			return Response.status(Status.NOT_FOUND).entity("document not found").build();
		}

		SourceResource entity = new SourceResource(doc.getDocId());
		transfer(doc, entity);

		for(HTextFlow htf : doc.getTextFlows()) {
			TextFlow tf = new TextFlow(htf.getResId());
			transfer(htf, tf);
			entity.getTextFlows().add(tf);
		}
		
		return Response.ok().entity(entity).tag(etag).build();
	}

	public static void transfer(SourceResource from, HDocument to) {
		to.setName(from.getName());
		to.setLocale(from.getLang());
		to.setContentType(from.getContentType());
	}

	public static void transfer(HDocument from, SourceResource to) {
		to.setName(from.getName());
		to.setLang(from.getLocale());
		to.setContentType(from.getContentType());
	}
	
	public static void transfer(HTextFlow from, TextFlow to) {
		to.setContent(from.getContent());
		// TODO
		//to.setLang(from.get)
	}
	
	private <T> T unmarshallEntity(Class<T> entityClass, InputStream is) {
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
					.getAnnotations(), requestContentType, headers, is);
		} catch (Exception e) {
			throw new RuntimeException("Unable to unmarshall request body");
		}
		return entity;
	}
}
