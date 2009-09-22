package org.fedorahosted.flies.rest.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.DocumentDAO;
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
    
    @In 
    private DocumentDAO documentDAO;
    @In 
    private ProjectContainerDAO projectContainerDAO;
    @In 
    private DocumentConverter documentConverter;
    @In 
    private Session session;
	
	private HProjectContainer getContainer() {
		HProjectContainer result = projectContainerDAO.getBySlug(
				documentsService.getProjectSlug(), 
				documentsService.getIterationSlug());
		if (result == null) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity("Container not found").build());
		}
		return result;
	}
    
    public Documents get() {
    	log.info("HTTP GET "+documentsService.getRequest().getRequestURL());
    	Collection<HDocument> hdocs = getContainer().getDocuments().values();
    	Documents result = new Documents();
	
    	for (HDocument hDocument : hdocs) {
    		result.getDocuments().add(hDocument.toDocument());
    	}
    	log.info("HTTP GET result :\n"+result);
    	return result;
    }
    
    public void post(Documents docs) {
    	log.info("HTTP POST "+documentsService.getRequest().getRequestURL()+" :\n"+docs);
    	HProjectContainer hContainer = getContainer();
    	Map<String, HDocument> docMap = hContainer.getDocuments();
    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = documentDAO.getByDocId(hContainer, doc.getId());
    		if (hDoc == null) {
    			hDoc = new HDocument(doc);
    			hDoc.setProject(hContainer);
    		}
			docMap.put(hDoc.getDocId(), hDoc);
    		session.save(hDoc);
    		documentConverter.copy(doc, hDoc, true);
    	}
    	session.flush();
    }
    
    
    public void put(Documents docs) {
    	log.info("HTTP PUT "+documentsService.getRequest().getRequestURL()+" :\n"+docs);
    	HProjectContainer hContainer = getContainer();
    	Map<String, HDocument> docMap = new HashMap<String, HDocument>(); 
    	hContainer.setDocuments(docMap);

    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = documentDAO.getByDocId(hContainer, doc.getId());
    		if (hDoc == null) {
    			hDoc = new HDocument(doc);
    			hDoc.setProject(hContainer);
    		}
    		docMap.put(hDoc.getDocId(), hDoc);
    		session.save(hDoc);
    		documentConverter.copy(doc, hDoc, true);
    	}
    	// TODO ensure omitted docs get deleted by Hibernate
    	session.flush();
    }

}
