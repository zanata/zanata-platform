package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.ResourceList;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@AutoCreate
@Scope(ScopeType.STATELESS)
@Name("DocumentServiceActionImpl")
public class DocumentServiceActionImpl implements DocumentServiceAction {

	@Logger 
    private Log log;

    // To access properties projectSlug, iterationSlug and request
    @In 
    private DocumentService documentService; 
    
    @In 
    private DocumentConverter documentConverter;

    @In
    private DocumentDAO documentDAO;
	
	@In
	private ProjectContainerDAO projectContainerDAO;
	
	@In
	private Session session;
	
    private HProjectContainer projectContainer;
	
    private String getDocumentId() {
    	return documentService.getDocumentId();
    }
    
	private String getIterationSlug() {
		return documentService.getIterationSlug();
	}

	private String getProjectSlug() {
		return documentService.getProjectSlug();
	}

	private URI getBaseUri() {
		return documentService.getUri().getBaseUri();
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
    
	public Response get(
			ContentQualifier resources) {
		
		HProjectContainer hProjectContainer = getContainer();
		if(hProjectContainer == null)
			return containerNotFound();
		String docId = URIHelper.convertFromDocumentURIId(getDocumentId());
		
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
		
    	URI baseUri = getBaseUri();
    	URI iterationUri = baseUri.resolve(URIHelper.getIteration(
    			getProjectSlug(), getIterationSlug()));
		URI docUri = baseUri.resolve(URIHelper.getDocument(
				getProjectSlug(), getIterationSlug(), doc.getId()));
		documentConverter.addLinks(doc, docUri, iterationUri);
		
		return Response.ok().entity(doc).tag("v-" + doc.getRevision()).build();
	}
	
	public Response put(Document document) throws URISyntaxException {
		
		String hDocId = URIHelper.convertFromDocumentURIId(getDocumentId());

		if(!document.getId().equals(hDocId)){
			return Response.status(Status.BAD_REQUEST).entity("Invalid document Id").build();
		}

		HProjectContainer hProjectContainer = getContainer();
		if(hProjectContainer == null)
			return containerNotFound();

		HDocument hDoc = documentDAO.getByDocId(hProjectContainer, hDocId);
		
		if(hDoc == null) { // it's a create operation
//			hDoc = documentConverter.create(document, hProjectContainer);
			// FIXME create hDoc, set its hProjectContainer
			log.debug("PUT creating new HDocument with id {0}", document.getId());
			hDoc = new HDocument(document);
			hDoc.setRevision(0);
			hDoc.setProject(hProjectContainer);

			
			documentConverter.copy(document, hDoc);
			hProjectContainer.getDocuments().put(hDoc.getDocId(), hDoc);
			session.save(hDoc);
			try{
				session.flush();
				return Response.created(getBaseUri().resolve(
						URIHelper.getDocument(
								getProjectSlug(), getIterationSlug(), getDocumentId()))).build();
			}
			catch(Exception e){
				log.error("Invalid document content", e);
				// TODO validation on the input data
				// this could also be a server error
				return Response.status(Status.BAD_REQUEST).entity("Invalid document content").build();
			}
		}
		else{ // it's an update operation
//			documentConverter.merge(document, hDoc);
			documentConverter.copy(document, hDoc);
			session.save(hDoc);
			session.flush();
			return Response.status(205).build();
		}
		
	}
	
	// FIXME implement DELETE
	
	public Response getContent(
			ContentQualifier qualifier,
			int levels){
		ResourceList resources = new ResourceList();
		return Response.ok().entity(resources).build();
	}

	public Response postContent(
			ResourceList content,
			ContentQualifier qualifier) {
		
		return Response.ok().build();
	}
	
	public Response putContent(
			ResourceList content,
			ContentQualifier qualifier) {
		
		return Response.ok().build();
	}

	public Response postContentByResourceId(
			DocumentResource resource,
			ContentQualifier qualifier,
			String resourceId) {
		
		return Response.ok().build();
	}
	
	public Response getContentByResourceId(
			ContentQualifier qualifier,
			String resourceId,
			int levels){
		return Response.ok().entity(new Container("id")).build();
	}
}
