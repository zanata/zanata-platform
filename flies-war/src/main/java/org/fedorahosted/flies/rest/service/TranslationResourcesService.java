package org.fedorahosted.flies.rest.service;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.LanguageQualifier;
import org.fedorahosted.flies.rest.LocaleIdSet;
import org.fedorahosted.flies.rest.NoSuchEntityException;
import org.fedorahosted.flies.rest.StringSet;
import org.fedorahosted.flies.rest.dto.v1.MultiTargetTextFlow;
import org.fedorahosted.flies.rest.dto.v1.MultiTargetTextFlowList;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
import org.fedorahosted.flies.rest.dto.v1.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.v1.ResourceMeta;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import com.google.common.collect.ImmutableSet;

@Name("translationResourcesService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/resources")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class TranslationResourcesService {

	public static final Set<String> EXTENSIONS = ImmutableSet.of(PoHeader.ID);
	
	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@QueryParam("ext") @DefaultValue("")
	private StringSet extensions;
	
	@HeaderParam("Content-Type")
	private MediaType requestContentType;
	
	@Context
	private HttpHeaders headers;
	
	@In
	private ProjectIterationDAO projectIterationDAO;

	@In
	private DocumentDAO documentDAO;

	@In
	private ResourceUtils resourceUtils;
	
	public TranslationResourcesService() {
	}
	
	public TranslationResourcesService(ProjectIterationDAO projectIterationDAO, DocumentDAO documentDAO, ResourceUtils resourceUtils) {
		this.projectIterationDAO = projectIterationDAO;
		this.documentDAO = documentDAO;
		this.resourceUtils = resourceUtils;
	}
	
	/**
	 * Retrieve the List of Resources
	 *  
	 * @return Response.ok with ResourcesList or Response(404) if not found 
	 */
	@GET
	public Response doGet(
			@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch		
				) {
		
		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = projectIterationDAO.getResourcesETag(hProjectIteration);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		
		ResourcesList resources = new ResourcesList();
		
		for(HDocument doc : hProjectIteration.getDocuments().values() ) {
			if(!doc.isObsolete()) {
				ResourceMeta resource = new ResourceMeta();
				resourceUtils.transfer(doc, resource);
				resources.add(resource);
			}
		}

		return Response.ok().entity(resources).tag(etag).build();
		
	}
	
	@POST
	@Restrict("#{identity.loggedIn}")
	public Response doPost(InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();

		validateExtensions();

		SourceResource entity = RestUtils.unmarshall(SourceResource.class, messageBody, requestContentType, headers.getRequestHeaders());
		RestUtils.validateEntity(entity);
		
		HDocument document = documentDAO.getByDocId(hProjectIteration, entity.getName()); 
		if(document != null) {
			if( !document.isObsolete() ) {
				// updates happens through PUT on the actual resource
				return Response.status(Status.CONFLICT)
					.entity("A document with name " + entity.getName() +" already exists.")
					.build();
			}
			// a deleted document is being created again 
			document.setObsolete(false);
		}
		else {
			document = new HDocument(entity.getName(),entity.getContentType());
			document.setProjectIteration(hProjectIteration);
		}
		
		resourceUtils.transfer(entity, document);

		document = documentDAO.makePersistent(document);
		documentDAO.flush();

		// handle extensions
		if ( resourceUtils.transfer(entity.getExtensions(), document, extensions) ) {
			documentDAO.flush();
		}
		
		// TODO include extensions in etag generation
		EntityTag etag = documentDAO.getETag(hProjectIteration, document.getDocId(), extensions);
		
		return Response.created(URI.create("r/"+resourceUtils.encodeDocId(document.getDocId())))
			.tag(etag).build();
	}

	@GET
	@Path("/r/{id}")
	public Response doResourceGet(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		HProjectIteration hProjectIteration = retrieveIteration();

		validateExtensions();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

		if(doc == null || doc.isObsolete() ) {
			return Response.status(Status.NOT_FOUND).entity("document not found").build();
		}

		SourceResource entity = new SourceResource(doc.getDocId());
		resourceUtils.transfer(doc, entity);
		
		for(HTextFlow htf : doc.getTextFlows()) {
			SourceTextFlow tf = new SourceTextFlow(htf.getResId());
			resourceUtils.transfer(htf, tf);
			entity.getTextFlows().add(tf);
		}

		// handle extensions
		resourceUtils.transfer(doc, entity.getExtensions(), extensions);
		
		return Response.ok().entity(entity).tag(etag).lastModified(doc.getLastChanged()).build();
	}

	@PUT
	@Path("/r/{id}")
	@Restrict("#{identity.loggedIn}")
	public Response doResourcePut(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch, 
			InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);
		
		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}

		SourceResource entity = RestUtils.unmarshall(SourceResource.class, messageBody, requestContentType, headers.getRequestHeaders());
		RestUtils.validateEntity(entity);

		HDocument document = documentDAO.getByDocId(hProjectIteration, id);
		if( document.isObsolete() ) {
			return Response.status(Status.NOT_FOUND).build();
		}

		boolean changed = resourceUtils.transfer(entity, document);
		
		// handle extensions
		changed |= resourceUtils.transfer(entity.getExtensions(), document, extensions);
		
		
		if ( changed ) {
			etag = documentDAO.getETag(hProjectIteration, id, extensions);
			documentDAO.flush();
		}

		return Response.ok().tag(etag).build();
			
	}

	@DELETE
	@Path("/r/{id}")
	public Response doResourceDelete(			
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch 
			) {
		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);

		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}

		HDocument document = documentDAO.getByDocId(hProjectIteration, id);
		document.setObsolete(true);
		documentDAO.flush();
		return Response.ok().build();
	}

	@GET
	@Path("/r/{id}/meta")
	public Response doResourceMetaGet(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {
		
		HProjectIteration hProjectIteration = retrieveIteration();

		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

		if(doc == null) {
			return Response.status(Status.NOT_FOUND).entity("document not found").build();
		}

		ResourceMeta entity = new ResourceMeta(doc.getDocId());
		resourceUtils.transfer(doc, entity);
		
		// transfer extensions
		resourceUtils.transfer(doc, entity.getExtensions(), extensions);

		return Response.ok().entity(entity).tag(etag).build();
	}
	
	@PUT
	@Path("/r/{id}/meta")
	public Response doResourceMetaPut(
			@PathParam("id") String id, 
			@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch, 
			InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();
		
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);

		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}

		ResourceMeta entity = RestUtils.unmarshall(ResourceMeta.class, messageBody, requestContentType, headers.getRequestHeaders());
		RestUtils.validateEntity(entity);
		
		HDocument document = documentDAO.getByDocId(hProjectIteration, id);

		if(document.isObsolete()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		boolean changed = resourceUtils.transfer(entity, document);
		
		// handle extensions
		changed |= resourceUtils.transfer(entity.getExtensions(), document, extensions);
		
		if(changed) {
			documentDAO.flush();
			etag = documentDAO.getETag(hProjectIteration, id, extensions);
		}
		
		return Response.ok().tag(etag).lastModified(document.getLastChanged()).build();
			
	}
	
	
	@GET
	@Path("/r/{id}/translations/{locales}")
	public Response doTranslationsGet(
		@PathParam("id") String id,
		@PathParam("locales") LanguageQualifier languageQualifier,
		@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		HProjectIteration hProjectIteration = retrieveIteration();

		validateExtensions();
		
		// TODO fix etag
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).entity("document not found").build();

		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		// TODO eager loading
		HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

		if(doc == null || doc.isObsolete() ) {
			return Response.status(Status.NOT_FOUND).entity("document not found").build();
		}

		Collection<LocaleId> locales =  languageQualifier.getLanguages();
		if(languageQualifier.isAll()) {
			locales = documentDAO.getTargetLocales(doc);
		}
		
		MultiTargetTextFlowList entity = new MultiTargetTextFlowList();
		
		for(HTextFlow htf : doc.getTextFlows()) {
			MultiTargetTextFlow tf = new MultiTargetTextFlow();
			resourceUtils.transfer(htf, tf);
			resourceUtils.transfer(htf, tf.getExtensions(), extensions);
			for(LocaleId locale : locales) {
				HTextFlowTarget htft = htf.getTargets().get(locale);
				if(htft != null) {
					TextFlowTarget target = new TextFlowTarget();
					resourceUtils.transfer(htft, target);
					resourceUtils.transfer(htft, tf.getExtensions(), extensions);
					tf.getTargets().put(locale, target);
				}
			}
			entity.add(tf);
		}

		return Response.ok().entity(entity).tag(etag).lastModified(doc.getLastChanged()).build();
		// TODO fix last modified
		
	}
	
	@PUT
	@Path("/r/{id}/translations/{locales}")
	public Response doTranslationsPut(
		@PathParam("id") String id,
		@PathParam("locales") LocaleIdSet locales,
		@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch) {

		return Response.ok().build();
	}

	@GET
	@Path("/r/{id}/translation-as-source/{locale}")
	public Response doTranslationAsSourceGet(
		@PathParam("id") String id,
		@PathParam("locale") LocaleId locale,
		@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch,
		@QueryParam("onlyApproved") @DefaultValue("true") boolean onlyApproved,
		@QueryParam("useSource") @DefaultValue("true") boolean useSource) {

		return Response.ok().build();
	}
	
	private HProjectIteration retrieveIteration() {
		HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(
				projectSlug, iterationSlug);

		if (hProjectIteration != null) {
			return hProjectIteration;
		}
		
		throw new NoSuchEntityException("Project Iteration '" + projectSlug+":"+iterationSlug+"' not found.");
	}

	private void validateExtensions() {
		Set<String> invalidExtensions = null;
		for(String ext : extensions) {
			if( ! EXTENSIONS.contains(ext)) {
				if(invalidExtensions == null) {
					invalidExtensions = new HashSet<String>();
				}
				invalidExtensions.add(ext);
			}
		}
		
		if(invalidExtensions != null) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
						.entity("Unsupported Extensions: " + StringUtils.join(invalidExtensions, ",")).build());
			
		}
	}
}
