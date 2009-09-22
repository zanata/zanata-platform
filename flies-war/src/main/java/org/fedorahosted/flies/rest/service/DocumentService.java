package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.repository.model.HContainer;
import org.fedorahosted.flies.repository.model.HDataHook;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HParentResource;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HReference;
import org.fedorahosted.flies.repository.model.HResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.AbstractBaseResource;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.DataHook;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Reference;
import org.fedorahosted.flies.rest.dto.Relationships;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.ResourceList;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.validator.InvalidStateException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.touri.ObjectToURI;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

@Name("documentService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/documents/d/{documentId}")
public class DocumentService {
	
	@PathParam("projectSlug")
	private String projectSlug;
	
	@PathParam("iterationSlug")
	private String iterationSlug;

	@PathParam("documentId")
	private String documentId;
	
	@In
	DocumentDAO documentDAO;
	
	@In
	ProjectContainerDAO projectContainerDAO;
	
	@In
	Session session;
	
	@Context
	UriInfo uri;
	
	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON, 
				MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response get(
			@QueryParam("resources") @DefaultValue("") ContentQualifier resources) {
		
		HProjectContainer hProjectContainer = getContainerOrFail();
		String docId = URIHelper.convertFromDocumentURIId(documentId);
		
		HDocument hDoc = documentDAO.getByDocId(hProjectContainer, docId);
		
		if(hDoc == null) {
			return Response.status(Status.NOT_FOUND).entity("Document not found").build();
		}
		
		Document doc = new Document(hDoc.getDocId(), hDoc.getName(), hDoc.getPath(), hDoc.getContentType(), hDoc.getRevision(), hDoc.getLocale());
		
		// add self relation
		Link link = new Link(uri.getRequestUri(), Relationships.SELF); 
		doc.getLinks().add(link);

		// add container relation
		link = new Link(
				uri.getBaseUri().resolve(URIHelper.getIteration(projectSlug, iterationSlug)), 
				Relationships.DOCUMENT_CONTAINER, 
				MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML);
		doc.getLinks().add(link);

		// add content if requested
		if( !resources.isNone()){
			Set<LocaleId> requestedLanguages = resources.getLanguages();
			if(resources.isAll()) {
				requestedLanguages = hDoc.getTargets().keySet(); 
			}

			List<HResource> hResources = hDoc.getResourceTree();
			List<Resource> rootResources = doc.getResources(true);
			populateResources(rootResources, hResources, requestedLanguages, true);
		}
		
		return Response.ok().entity(doc).tag("v-" + doc.getVersion()).build();
	}
	
	private void populateResources(List<Resource> resources, List<HResource> hResources, Set<LocaleId> includedTargets, int levels){
		for(HResource hResource : hResources) {
			if(hResource instanceof HContainer) {
				HContainer hContainer = (HContainer) hResource;
				Container container = new Container(hContainer.getResId());
				resources.add(container);
				if(levels != 0) {
					populateResources(container.getContent(), hContainer.getChildren(), includedTargets, --levels);
				}
			}
			else if (hResource instanceof HDataHook) {
				HDataHook hDataHook = (HDataHook) hResource;
				DataHook dataHook = new DataHook(hDataHook.getResId());
				resources.add(dataHook);
			}
			else if (hResource instanceof HTextFlow) {
				HTextFlow hTextFlow = (HTextFlow) hResource;
				TextFlow textFlow = new TextFlow(hTextFlow.getResId());
				textFlow.setContent(hTextFlow.getContent());
				//textFlow.setLang(hTextFlow.get) TODO language
				resources.add(textFlow);
				
				for (LocaleId locale : includedTargets) {
					HTextFlowTarget hTextFlowTarget = hTextFlow.getTargets().get(locale);
					if(hTextFlowTarget != null) {
						TextFlowTarget textFlowTarget = new TextFlowTarget(textFlow, locale);
						textFlowTarget.setContent(hTextFlowTarget.getContent());
						textFlowTarget.setState(hTextFlowTarget.getState());
						textFlow.addTarget(textFlowTarget);
					}
				}
			}
			else if(hResource instanceof HReference) {
				HReference hReference = (HReference) hResource;
				Reference reference = new Reference(hReference.getResId());
				reference.setRelationshipId(hReference.getRef());
				resources.add(reference);
			}
			else{
				throw new RuntimeException("Invalid resource - programming error");
			}
		}
		
	}	
	private void populateResources(List<Resource> resources, List<HResource> hResources, Set<LocaleId> includedTargets, boolean recursive){
		populateResources(resources, hResources, includedTargets, recursive ? -1 : 0);
	}

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON,
				MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(Document document) throws URISyntaxException {
		
		String hDocId = URIHelper.convertFromDocumentURIId(documentId);

		if(!document.getId().equals(hDocId)){
			Response.notAcceptable(Collections.EMPTY_LIST).build();
		}

		HProjectContainer hProjectContainer = getContainerOrFail();

		HDocument hDoc = documentDAO.getByDocId(hProjectContainer, hDocId);
		
		if(hDoc == null) { // it's a create operation
			hDoc = new HDocument(document);
			hDoc.setRevision(1);
			hProjectContainer.getDocuments().put(hDoc.getDocId(), hDoc);
			hDoc.setProject(hProjectContainer);
			try{
				session.flush();
				if(document.hasResources()){
					for(Resource res : document.getResources()) {
						HResource hRes = HDocument.create(res);
						hRes.setDocument(hDoc);
						hDoc.getResourceTree().add(hRes);
						session.flush();
					}
				}
				return Response.created( uri.getBaseUri().resolve(URIHelper.getDocument(projectSlug, iterationSlug, documentId))).build();
			}
			catch(Exception e){
				return Response.notAcceptable(Collections.EMPTY_LIST).build();
			}
		}
		else{ // it's an update operation
			copyMetaData(document, hDoc);
			if(!document.getResources().isEmpty()){
				
			}
			try{
				session.flush();
				return Response.ok().build();
			}
			catch(Exception e){
				return Response.notAcceptable(Collections.EMPTY_LIST).build();
			}
		}
		
	}

	private void copyMetaData(Document from, HDocument to){
		final String fromName = from.getName();
		if(fromName == null && to.getName() != null || !fromName.equals(to.getName())) {
			to.setName(fromName);
		}
		final String fromPath = from.getPath();
		if(fromPath == null && to.getPath() != null || !fromPath.equals(to.getPath())) {
			to.setPath(from.getPath());
		}
	}
	
	@GET
	@Path("content/{qualifier}")
	public Response getContent(
			@PathParam("qualifier") ContentQualifier qualifier,
			@QueryParam("levels") @DefaultValue("1") int levels){
		ResourceList resources = new ResourceList();
		return Response.ok().entity(resources).build();
	}

	@POST
	@Path("content/{qualifier}")
	@Restrict("#{identity.loggedIn}")
	public Response postContent(
			ResourceList content,
			@PathParam("qualifier") ContentQualifier qualifier) {
		
		return Response.ok().build();
	}
	
	@PUT
	@Path("content/{qualifier}")
	public Response putContent(
			ResourceList content,
			@PathParam("qualifier") ContentQualifier qualifier) {
		
		return Response.ok().build();
	}

	@POST
	@Path("content/{qualifier}/{resourceId}")
	@Restrict("#{identity.loggedIn}")
	public Response postContentByResourceId(
			Resource resource,
			@PathParam("qualifier") ContentQualifier qualifier,
			@PathParam("resourceId") String resourceId) {
		
		return Response.ok().build();
	}
	
	@GET
	@Path("content/{qualifier}/{resourceId}")
	public Response getContentByResourceId(
			@PathParam("qualifier") ContentQualifier qualifier,
			@PathParam("resourceId") String resourceId,
			@QueryParam("levels") @DefaultValue("1") int levels){
		return Response.ok().entity(new Container("id")).build();
	}
	
	private HProjectContainer getContainerOrFail(){
		HProjectContainer hProjectContainer = projectContainerDAO.getBySlug(projectSlug, iterationSlug); 
		
		if(hProjectContainer == null)
			throw new WebApplicationException(
					Response.status(Status.NOT_FOUND).entity("Project Container not found").build());
		
		return hProjectContainer;
	}

}
