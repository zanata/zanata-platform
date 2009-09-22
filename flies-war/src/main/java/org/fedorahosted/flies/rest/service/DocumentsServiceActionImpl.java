package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
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
@Name("DocumentsServiceActionImpl")
public class DocumentsServiceActionImpl implements DocumentsServiceAction {

    @Logger 
    private Log log;
    
    // To access properties projectSlug, iterationSlug and request
    @In 
    private DocumentsService documentsService; 
    
//    @In 
//    private DocumentDAO documentDAO;
    @In 
    private ProjectContainerDAO projectContainerDAO;
    @In 
    private DocumentConverter documentConverter;
    @In 
    private Session session;
	
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
		HProjectContainer result = projectContainerDAO.getBySlug(
				getProjectSlug(), 
				getIterationSlug());
		if (result == null) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity("Container not found").build());
		}
		return result;
	}
    
    public Documents get() {
    	log.debug("HTTP GET {0}", documentsService.getRequest().getRequestURL());
    	Collection<HDocument> hdocs = getContainer().getDocuments().values();
    	Documents result = new Documents();
	
    	for (HDocument hDocument : hdocs) {
    		Document doc = hDocument.toDocument(true);
			result.getDocuments().add(doc);
			
			URI docUri = getBaseUri().resolve(URIHelper.getDocument(
					getProjectSlug(), getIterationSlug(), doc.getId()));
			URI iterationUri = getBaseUri().resolve(URIHelper.getIteration(
					getProjectSlug(), getIterationSlug()));
			documentConverter.addLinks(doc, docUri, iterationUri );
    	}
    	log.info("HTTP GET result :\n"+result);
    	return result;
    }

    public void post(Documents docs) {
    	log.debug("HTTP POST {0} : \n{1}",documentsService.getRequest().getRequestURL(), docs);
    	HProjectContainer hContainer = getContainer();
    	Map<String, HDocument> docMap = hContainer.getDocuments();
    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = docMap.get(doc.getId());
    		if (hDoc == null) {
    			log.info("POST creating new HDocument with id {0}", doc.getId());
    			hDoc = new HDocument(doc);
    			hDoc.setRevision(1);
    			hDoc.setProject(hContainer);
    		} else {
    			log.info("POST updating HDocument with id {0}", doc.getId());
    		}
    		
			docMap.put(hDoc.getDocId(), hDoc);
    		session.save(hDoc);
    		documentConverter.copy(doc, hDoc, true);
    	}
    	session.flush();
    }
    
    
    public void put(Documents docs) {
    	log.debug("HTTP PUT {0} : \n{1}",documentsService.getRequest().getRequestURL(), docs);
    	HProjectContainer hContainer = getContainer();
//    	Map<String, HDocument> docMap = new HashMap<String, HDocument>(); 
//    	hContainer.setDocuments(docMap);
    	Map<String, HDocument> docMap = hContainer.getDocuments();
    	Map<String, HDocument> oldMap = new HashMap<String, HDocument>(docMap); 
    	docMap.clear();

    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
//    		HDocument hDoc = oldMap.get(doc.getId());
    		HDocument hDoc = oldMap.remove(doc.getId());
    		if (hDoc == null) {
    			log.debug("PUT creating new HDocument with id {0}", doc.getId());
    			hDoc = new HDocument(doc);
    			hDoc.setRevision(1);
    			hDoc.setProject(hContainer);
    		} else {
    			log.debug("PUT updating HDocument with id {0}", doc.getId());
    		}
    		docMap.put(hDoc.getDocId(), hDoc);
    		session.save(hDoc);
    		documentConverter.copy(doc, hDoc, true);
    	}
    	
    	// why aren't implicit deletes working?  we already cleared docMap
    	// ensure omitted docs get deleted by Hibernate
//    	for (HDocument hDoc : oldMap.values()) {
//			session.delete(hDoc);
//		}
    	session.flush();
    	log.debug("final state :\n{0}", docs);
    }

}
