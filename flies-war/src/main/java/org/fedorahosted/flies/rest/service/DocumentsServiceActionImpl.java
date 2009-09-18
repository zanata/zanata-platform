package org.fedorahosted.flies.rest.service;

import java.util.ArrayList;
import java.util.List;

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

    @Logger Log log;
    @In DocumentsService documentsService;
    
    @In ProjectContainerDAO projectContainerDAO;

    @In
    Session session;
	
    private HProjectContainer getContainer() {
	return projectContainerDAO.getBySlug(documentsService.getProjectSlug(), documentsService.getIterationSlug());
    }
    
    public Documents get() {
    	log.info("HTTP GET "+documentsService.getRequest().getRequestURL());
    	List<HDocument> hdocs = getContainer().getDocuments();
    	Documents result = new Documents();
	
    	for (HDocument hDocument : hdocs) {
    		result.getDocuments().add(hDocument.toDocument());
    	}
    	log.info("HTTP GET result :\n"+result);
    	return result;
    }
    
    public void post(Documents docs) {
    	log.info("HTTP POST "+documentsService.getRequest().getRequestURL()+" :\n"+docs);
    	for (Document doc: docs.getDocuments()) {
    		// FIXME if doc already exists, load it and update it, but don't create it
    		getContainer().getDocuments().add(new HDocument(doc));
    	}
    	session.flush();
    }
    
    
    public void put(Documents docs) {
    	log.info("HTTP PUT "+documentsService.getRequest().getRequestURL()+" :\n"+docs);
    	List<HDocument> hdocs = new ArrayList<HDocument>();

    	for (Document doc: docs.getDocuments()) {
    		// FIXME if doc already exists, load it and update it, but don't create it
    		hdocs.add(new HDocument(doc));
    	}
    	getContainer().setDocuments(hdocs);
    	session.flush();
    }

}
