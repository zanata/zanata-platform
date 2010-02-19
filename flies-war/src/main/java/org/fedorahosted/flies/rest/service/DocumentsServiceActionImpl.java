package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentResource;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowHistory;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.hibernate.Session;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@AutoCreate
@Scope(ScopeType.STATELESS)
@Name("DocumentsServiceActionImpl")
public class DocumentsServiceActionImpl implements DocumentsServiceAction {

    @Logger 
    private Log log;
    
    // To access properties projectSlug, iterationSlug and request
    @In 
    private DocumentsService documentsService; 
    
    @In 
    private DocumentDAO documentDAO;
    
    @In 
    private ProjectContainerDAO projectContainerDAO;
    @In 
    private DocumentConverter documentConverter;
    @In 
    private Session session;
    
    private HProjectContainer projectContainer;
	
	private String getIterationSlug() {
		return documentsService.getIterationSlug();
	}

	private String getProjectSlug() {
		return documentsService.getProjectSlug();
	}
	
	private URI getBaseUri() {
		return documentsService.getUri().getBaseUri();
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
    
	@Override
    public Response get() {
    	log.debug("HTTP GET {0}", documentsService.getRequest().getRequestURL());
    	URI baseUri = getBaseUri();
    	URI iterationUri = baseUri.resolve(URIHelper.getIteration(
    			getProjectSlug(), getIterationSlug()));
    	if (getContainer() == null)
    		return containerNotFound();
    	Collection<HDocument> hdocs = getContainer().getDocuments().values();
    	Documents result = new Documents();
	
    	for (HDocument hDocument : hdocs) {
    		Document doc = hDocument.toDocument(true);
			
			URI docUri = baseUri.resolve(URIHelper.getDocument(
					getProjectSlug(), getIterationSlug(), doc.getId()));
			documentConverter.addLinks(doc, docUri, iterationUri);
			
			result.getDocuments().add(doc);
    	}
    	log.debug("HTTP GET result :\n"+result);
    	return Response.ok(result).build();
    }

	@Override
    public Response post(Documents docs) {
    	log.debug("HTTP POST {0} : \n{1}",documentsService.getRequest().getRequestURL(), docs);
    	HProjectContainer hContainer = getContainer();
    	if (hContainer == null)
    		return containerNotFound();
    	Map<String, HDocument> docMap = hContainer.getDocuments();
    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = docMap.get(doc.getId());
    		if (hDoc == null) {
    			log.info("POST creating new HDocument with id {0}", doc.getId());
    			hDoc = new HDocument(doc);
    			hDoc.setRevision(0);
    			hDoc.setProject(hContainer);
    		} else {
    			log.info("POST updating HDocument with id {0}", doc.getId());
    		}
    		
			docMap.put(hDoc.getDocId(), hDoc);
			// TODO handle invalid data.  See put()
    		session.save(hDoc);
    		documentConverter.copy(doc, hDoc);
    	}
    	session.flush();
    	return Response.ok().build();
    }
    
	@Override
    public Response put(Documents docs) {
    	log.debug("HTTP PUT {0} : \n{1}",documentsService.getRequest().getRequestURL(), docs);
    	HProjectContainer hContainer = getContainer();
    	if (hContainer == null)
    		return containerNotFound();
    	Map<String, HDocument> docMap = hContainer.getDocuments();
    	// any Docs still in this set at the end will be marked obsolete
    	Set<HDocument> obsoleteDocs = new HashSet<HDocument>(docMap.values());
    	ClassValidator<HDocument> docValidator = new ClassValidator<HDocument>(HDocument.class);
    	StringBuilder sb = new StringBuilder();

    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = documentDAO.getByDocId(hContainer, doc.getId()); 
    		if (hDoc == null) {
    			log.debug("PUT creating new HDocument with id {0}", doc.getId());
    			hDoc = new HDocument(doc);
    			hDoc.setRevision(0);
    			hDoc.setProject(hContainer);
    		} else {
    			log.debug("PUT updating HDocument with id {0}", doc.getId());
    			obsoleteDocs.remove(hDoc);
    			hDoc.setObsolete(false);
    		}
    		docMap.put(hDoc.getDocId(), hDoc);
    		try {
				documentConverter.copy(doc, hDoc);
				InvalidValue[] invalidValues = docValidator.getInvalidValues(hDoc);
				if (invalidValues.length != 0) {
					String message = "Document with id '"+doc.getId()+"' is invalid: "+Arrays.asList(invalidValues);
					obsoleteDocs.add(hDoc);
					log.error(message);
					sb.append(message);
					sb.append('\n');
				} else {
					session.save(hDoc);
				}
			} catch (InvalidStateException e) {
				String message = "Document with id '"+doc.getId()+"' is invalid: "+Arrays.asList(e.getInvalidValues());
				log.error(message+'\n'+doc, e);
				return Response.status(Status.BAD_REQUEST).entity(message).build();
			}
//			session.save(hDoc);
    	}
    	for (HDocument hDoc: obsoleteDocs) {
    		// mark document resources as obsolete 
    		for (HDocumentResource resource : hDoc.getResources()) {
    			// copy to TFHistory table under old revision
    			if (resource instanceof HTextFlow) {
					HTextFlow htf = (HTextFlow) resource;
					HTextFlowHistory history = new HTextFlowHistory(htf);
					htf.getHistory().put(htf.getRevision(), history);
				}
    			resource.setObsolete(true);
    		}
    		hDoc.setObsolete(true);
    		hDoc.setRevision(hDoc.getRevision()+1);
    		docMap.remove(hDoc.getId());
    	}
    	session.flush();
    	return Response.ok(sb.toString()).build();
    }

}
