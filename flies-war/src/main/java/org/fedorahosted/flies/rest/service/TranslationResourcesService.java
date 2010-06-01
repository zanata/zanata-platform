package org.fedorahosted.flies.rest.service;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.fedorahosted.flies.dao.AccountDAO;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.PersonDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HPerson;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.fedorahosted.flies.model.po.HPoTargetHeader;
import org.fedorahosted.flies.rest.LanguageQualifier;
import org.fedorahosted.flies.rest.LocaleIdSet;
import org.fedorahosted.flies.rest.NoSuchEntityException;
import org.fedorahosted.flies.rest.StringSet;
import org.fedorahosted.flies.rest.dto.v1.MultiTargetTextFlow;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
import org.fedorahosted.flies.rest.dto.v1.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.v1.ResourceMeta;
import org.fedorahosted.flies.rest.dto.v1.TargetResource;
import org.fedorahosted.flies.rest.dto.v1.TextFlowTargetWithId;
import org.fedorahosted.flies.rest.dto.v1.TranslationResource;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

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
	private TextFlowTargetDAO textFlowTargetDAO;
	
	@In
	private ResourceUtils resourceUtils;
	
	@In
	private PersonDAO personDAO;
	
	public TranslationResourcesService() {
	}
	
	public TranslationResourcesService(ProjectIterationDAO projectIterationDAO, 
			DocumentDAO documentDAO, PersonDAO personDAO, TextFlowTargetDAO textFlowTargetDAO, ResourceUtils resourceUtils) {
		this.projectIterationDAO = projectIterationDAO;
		this.documentDAO = documentDAO;
		this.personDAO = personDAO;
		this.textFlowTargetDAO = textFlowTargetDAO;
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

		validateExtensions();

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
	@Path("/r/{id}/targets/{locales}")
	public Response doTargetsGet(
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
		
		TargetResource entity = new TargetResource();
		
		resourceUtils.transfer(doc, entity.getExtensions(), extensions);
		resourceUtils.transfer(doc, entity.getExtensions(), extensions, locales);
		
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
			entity.getTextFlows().add(tf);
		}

		return Response.ok().entity(entity).tag(etag).lastModified(doc.getLastChanged()).build();
		// TODO fix last modified
		
	}

	@GET
	@Path("/r/{id}/translations/{locale}")
	public Response doTranslationsGet(
		@PathParam("id") String id,
		@PathParam("locale") LocaleId locale,
		@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {
		
		HProjectIteration hProjectIteration = retrieveIteration();

		validateExtensions();

		// TODO create valid etag
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);
		
		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}

		HDocument document = documentDAO.getByDocId(hProjectIteration, id);
		if( document.isObsolete() ) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		List<HTextFlowTarget> hTargets = textFlowTargetDAO.findAllTranslations(document, locale);

		TranslationResource translationResource = new TranslationResource();
		resourceUtils.transfer(document, translationResource.getExtensions(), extensions, locale);
		
		if(hTargets.isEmpty() && translationResource.getExtensions().isEmpty() ) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		for(HTextFlowTarget hTarget : hTargets) {
			TextFlowTargetWithId target = new TextFlowTargetWithId(hTarget.getTextFlow().getResId());
			resourceUtils.transfer(hTarget, target);
			resourceUtils.transfer(hTarget, target.getExtensions(), extensions);
			translationResource.getTextFlowTargets().add(target);
		}
		
		// TODO lastChanged
		return Response.ok().entity(translationResource).tag(etag).build();
		
	}	
	
	@DELETE
	@Path("/r/{id}/translations/{locale}")
	public Response doTranslationsDelete(
		@PathParam("id") String id,
		@PathParam("locale") LocaleId locale,
		@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch) {
		
		HProjectIteration hProjectIteration = retrieveIteration();
		
		// TODO find correct etag
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);

		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}
		
		HDocument document = documentDAO.getByDocId(hProjectIteration, id);
		if( document.isObsolete() ) {
			return Response.status(Status.NOT_FOUND).build();
		}		
		List<HTextFlowTarget> targets = textFlowTargetDAO.findAllTranslations(document, locale);
		
		for(HTextFlowTarget target : targets) {
			target.clear();
		}
		
		// we also need to delete the extensions here
		document.getPoTargetHeaders().remove(locale);
		textFlowTargetDAO.flush();
		
		return Response.ok().build();
		
	}	
	
	@PUT
	@Path("/r/{id}/translations/{locale}")
	public Response doTranslationsPut(
		@PathParam("id") String id,
		@PathParam("locale") LocaleId locale,
		@HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch,
		InputStream messageBody) {

		HProjectIteration hProjectIteration = retrieveIteration();

		validateExtensions();

		// TODO create valid etag
		EntityTag etag = documentDAO.getETag(hProjectIteration, id, extensions);
		
		if(etag == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}

		HDocument document = documentDAO.getByDocId(hProjectIteration, id);
		if( document.isObsolete() ) {
			return Response.status(Status.NOT_FOUND).build();
		}

		TranslationResource entity = RestUtils.unmarshall(TranslationResource.class, messageBody, requestContentType, headers.getRequestHeaders());
		RestUtils.validateEntity(entity);

		boolean changed = false;
		
		// handle extensions
		changed |= resourceUtils.transfer(entity.getExtensions(), document, extensions, locale);
		
		List<HPerson> newPeople = new ArrayList<HPerson>();
		List<HTextFlowTarget> newTargets = new ArrayList<HTextFlowTarget>();
		List<HTextFlowTarget> changedTargets = new ArrayList<HTextFlowTarget>();
		List<HTextFlowTarget> removedTargets = new ArrayList<HTextFlowTarget>();
		
		Iterator<TextFlowTargetWithId> iter = entity.getTextFlowTargets().iterator();
		TextFlowTargetWithId current = null;
		for(HTextFlow textFlow : document.getTextFlows()) {
			if(current == null) {
				if(iter.hasNext()) {
					current = iter.next();
				}
				else {
					HTextFlowTarget hTarget = textFlow.getTargets().get(locale);
					if(hTarget != null) {
						removedTargets.add(hTarget);
					}
					continue;
				}
			}
			
			if(textFlow.getResId().equals(current.getResId())) {
				// transfer
				
				HTextFlowTarget hTarget = textFlow.getTargets().get(locale);
				boolean targetChanged = false;
				if(hTarget == null) {
					targetChanged = true;
					hTarget = new HTextFlowTarget(textFlow, locale);
					textFlow.getTargets().put(locale, hTarget);
					newTargets.add(hTarget);
					targetChanged |= resourceUtils.transfer(current, hTarget);
					targetChanged |= resourceUtils.transfer(current.getExtensions(),hTarget, extensions);
				}
				else {
					targetChanged |= resourceUtils.transfer(current, hTarget);
					targetChanged |= resourceUtils.transfer(current.getExtensions(),hTarget, extensions);
					if(targetChanged) {
						changedTargets.add(hTarget);
					}
				}
				
				// update translation information if applicable
				if(targetChanged && current.getTranslator() != null) {
					String email = current.getTranslator().getEmail();
					HPerson hPerson = personDAO.findByEmail(email);
					if(hPerson == null) {
						hPerson = new HPerson();
						hPerson.setEmail(email);
						hPerson.setName(current.getTranslator().getName());
						newPeople.add(hPerson);
					}
					hTarget.setLastModifiedBy(hPerson);
				}
				
				
				current = null;
			}
			else {
				HTextFlowTarget hTarget = textFlow.getTargets().get(locale);
				if(hTarget != null) {
					removedTargets.add(hTarget);
				}
			}
			
		}
		
		if(iter.hasNext()) {
			return Response.status(Status.BAD_REQUEST).entity("Unexpected target: " + iter.next().getResId()).build();
		}
		else if ( changed || !newTargets.isEmpty() || !changedTargets.isEmpty() || !removedTargets.isEmpty() ) {

			for(HPerson person : newPeople) {
				personDAO.makePersistent(person);
			}
			personDAO.flush();

			for(HTextFlowTarget target : newTargets) {
				textFlowTargetDAO.makePersistent(target);
			}
			textFlowTargetDAO.flush();
			
			for(HTextFlowTarget target : removedTargets) {
				target.clear();
			}
			textFlowTargetDAO.flush();
			
			documentDAO.flush();

			// TODO create valid etag
			etag = documentDAO.getETag(hProjectIteration, id, extensions);
		}

		// TODO lastChanged
		return Response.ok().tag(etag).build();
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
