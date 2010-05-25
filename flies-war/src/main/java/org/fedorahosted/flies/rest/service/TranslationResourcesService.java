package org.fedorahosted.flies.rest.service;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.rest.NoSuchEntityException;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
import org.fedorahosted.flies.rest.dto.v1.TranslationResource;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;

@Name("translationResourcesService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/resources")
public class TranslationResourcesService {

	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@QueryParam("ext")
	private Set<String> extensions;
	
	@HeaderParam("Content-Type")
	private MediaType requestContentType;

	@Context
	private UriInfo uriInfo;
	
	@Context
	private HttpHeaders headers;
	
	@In
	private ProjectIterationDAO projectIterationDAO;

	@In
	private DocumentDAO documentDAO;

	@In
	private DocumentUtils documentUtils;
	
	public TranslationResourcesService() {
	}
	
	public TranslationResourcesService(ProjectIterationDAO projectIterationDAO, DocumentDAO documentDAO, DocumentUtils documentUtils) {
		this.projectIterationDAO = projectIterationDAO;
		this.documentDAO = documentDAO;
		this.documentUtils = documentUtils;
	}
	
	
	/**
	 * Retrieve the List of Resources
	 *  
	 * @return Response.ok with ResourcesList or Response(404) if not found 
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response doGet() {
		HProjectIteration hProjectIteration = retrieveIteration();
		
		ResourcesList resources = new ResourcesList();
		
		for(HDocument doc : hProjectIteration.getDocuments().values() ) {
			if(!doc.isObsolete()) {
				TranslationResource resource = new TranslationResource();
				documentUtils.transfer(doc, resource);
				resources.add(resource);
			}
		}

		return Response.ok().entity(resources).build();
		
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Restrict("#{identity.loggedIn}")
	public Response doPost(InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();

		HDocument document = new HDocument();
		document.setProjectIteration(hProjectIteration);
		SourceResource entity = unmarshallEntity(SourceResource.class, messageBody);
		RestUtils.validateEntity(entity);
		
		documentUtils.transfer(entity, document);
		
		documentDAO.makePersistent(document);
		documentDAO.flush();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, document.getDocId());
		
		return Response.created(URI.create("r/"+documentUtils.encodeDocId(document.getDocId())))
			.tag(etag).build();
	}

	@GET
	@Path("/r/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response doResGet(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		HProjectIteration hProjectIteration = retrieveIteration();

		EntityTag etag = documentDAO.getETag(hProjectIteration, id);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

		if(doc == null) {
			return Response.status(Status.NOT_FOUND).entity("document not found").build();
		}

		SourceResource entity = new SourceResource(doc.getDocId());
		documentUtils.transfer(doc, entity);

		for(HTextFlow htf : doc.getTextFlows()) {
			SourceTextFlow tf = new SourceTextFlow(htf.getResId());
			documentUtils.transfer(htf, tf);
			entity.getTextFlows().add(tf);
		}
		
		return Response.ok().entity(entity).tag(etag).lastModified(doc.getLastChanged()).build();
	}

	@PUT
	@Path("/r/{id}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Restrict("#{identity.loggedIn}")
	public Response doResPut(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch, 
			InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id);

		HDocument document;
		
		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}
		else {
			SourceResource entity = unmarshallEntity(SourceResource.class, messageBody);
			document = documentDAO.getByDocId(hProjectIteration, id);
			documentUtils.transfer(entity, document);
		}
		
		documentDAO.flush();
		etag = documentDAO.getETag(hProjectIteration, id);
		return Response.ok().tag(etag).build();
			
	}

	@DELETE
	@Path("/r/{id}")
	public Response doResDelete(			
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch 
			) {
		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id);

		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}

		HDocument document = documentDAO.getByDocId(hProjectIteration, id);
		document.setObsolete(true);
		documentDAO.makePersistent(document);
		documentDAO.flush();
		return Response.ok().build();
	}

	@GET
	@Path("/r/{id}/meta")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response doResMetaGet(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {
		
		HProjectIteration hProjectIteration = retrieveIteration();

		EntityTag etag = documentDAO.getETag(hProjectIteration, id);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

		if(doc == null) {
			return Response.status(Status.NOT_FOUND).entity("document not found").build();
		}

		TranslationResource entity = new TranslationResource(doc.getDocId());
		documentUtils.transfer(doc, entity);

		return Response.ok().entity(entity).tag(etag).build();
	}
	
	@PUT
	@Path("/r/{id}/meta")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response doResMetaPut(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch, 
			InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id);

		HDocument document;
		
		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}
		else {
			TranslationResource entity = unmarshallEntity(TranslationResource.class, messageBody);
			document = documentDAO.getByDocId(hProjectIteration, id);
			documentUtils.transfer(entity, document);
		}
		
		documentDAO.flush();
		etag = documentDAO.getETag(hProjectIteration, id);
		return Response.ok().tag(etag).lastModified(document.getLastChanged()).build();
			
	}
	
	
	@GET
	@Path("/r/{id}/target/{locale}")
	public Response doResTargetGet(
		@PathParam("id") String id,
		@PathParam("locale") Set<LocaleId> locales,
		@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		return Response.ok().build();
	}
	
	@PUT
	@Path("/r/{id}/target/{locale}")
	public Response doResTargetPut(
		@PathParam("id") String id,
		@PathParam("locale") Set<LocaleId> locales,
		@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch) {

		return Response.ok().build();
	}

	@GET
	@Path("/r/{id}/target-as-source/{locale}")
	public Response doResTargetAsSourceGet(
		@PathParam("id") String id,
		@PathParam("locale") LocaleId locale,
		@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		return Response.ok().build();
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
					.getAnnotations(), requestContentType, headers.getRequestHeaders(), is);
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Unable to read request body").build());
		}
		
		return entity;
	}
	
	private HProjectIteration retrieveIteration() {
		HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectIteration != null) {
			return hProjectIteration;
		}
		
		throw new NoSuchEntityException("Project Iteration '" + projectSlug+":"+iterationSlug+"' not found.");
	}
	
}
