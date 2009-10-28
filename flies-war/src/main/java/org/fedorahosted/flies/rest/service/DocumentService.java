package org.fedorahosted.flies.rest.service;

import java.net.URISyntaxException;
import java.util.Set;

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
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.ResourceList;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

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
    private DocumentConverter documentConverter;

    @In
	DocumentDAO documentDAO;
	
	@In
	ProjectContainerDAO projectContainerDAO;
	
	@In
	Session session;
	
	@Context
	UriInfo uri;
	
    @Logger 
    private Log log;
    
	
    private HProjectContainer projectContainer;
	
	private String getIterationSlug() {
		return iterationSlug;
	}

	private String getProjectSlug() {
		return projectSlug;
	}

	private HProjectContainer getContainer() {
		if (projectContainer != null)
			return projectContainer;
		projectContainer = projectContainerDAO.getBySlug(
				getProjectSlug(), 
				getIterationSlug());
		return projectContainer;
	}

	private Response containerNotFound() {
		return Response.status(Status.NOT_FOUND).entity("Project Container not found").build();
	}
    
	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON, 
				MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response get(
			@QueryParam("resources") @DefaultValue("") ContentQualifier resources) {
		
		HProjectContainer hProjectContainer = getContainer();
		if(hProjectContainer == null)
			return containerNotFound();
		String docId = URIHelper.convertFromDocumentURIId(documentId);
		
		HDocument hDoc = documentDAO.getByDocId(hProjectContainer, docId);
		
		if(hDoc == null) {
			return Response.status(Status.NOT_FOUND).entity("Document not found").build();
		}
		
		Set<LocaleId> requestedLanguages = resources.getLanguages();
		if (resources.isAll()) {
			requestedLanguages = documentDAO.getTargetLocales(hDoc); 
		}
		
		int requestedLevels = resources.isNone() ? 0 : Integer.MAX_VALUE;
		Document doc = hDoc.toDocument(requestedLevels); 
		documentConverter.addLinks(doc,uri.getRequestUri(), 
				uri.getBaseUri().resolve(URIHelper.getIteration(projectSlug, iterationSlug)));
		
		return Response.ok().entity(doc).tag("v-" + doc.getRevision()).build();
	}
	
	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_DOCUMENT_XML, MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON,
				MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(Document document) throws URISyntaxException {
		
		String hDocId = URIHelper.convertFromDocumentURIId(documentId);

		if(!document.getId().equals(hDocId)){
			return Response.status(Status.BAD_REQUEST).entity("Invalid document Id").build();
		}

		HProjectContainer hProjectContainer = getContainer();
		if(hProjectContainer == null)
			return containerNotFound();

		HDocument hDoc = documentDAO.getByDocId(hProjectContainer, hDocId);
		
		if(hDoc == null) { // it's a create operation
			hDoc = documentConverter.create(document, hProjectContainer);
			hProjectContainer.getDocuments().put(hDoc.getDocId(), hDoc);
			session.save(hDoc);
			try{
				session.flush();
				return Response.created( uri.getBaseUri().resolve(URIHelper.getDocument(projectSlug, iterationSlug, documentId))).build();
			}
			catch(Exception e){
				log.error("Invalid document content", e);
				// TODO validation on the input data
				// this could also be a server error
				return Response.status(Status.BAD_REQUEST).entity("Invalid document content").build();
			}
		}
		else{ // it's an update operation
			documentConverter.merge(document, hDoc);
			session.save(hDoc);
			session.flush();
			return Response.status(205).build();
		}
		
	}
	
	// FIXME implement DELETE
	
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
			DocumentResource resource,
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
	

}
