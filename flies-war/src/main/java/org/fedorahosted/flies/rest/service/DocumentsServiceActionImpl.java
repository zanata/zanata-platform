package org.fedorahosted.flies.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
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
    
    @In
    ProjectIterationDAO projectIterationDAO;

    private HProjectContainer getContainer() {
	return projectIterationDAO.getBySlug(documentsService.getProjectSlug(), documentsService.getIterationSlug()).
	getContainer();
    }
    
    public Documents get() {
	log.info("get");
//	Query query = session.createQuery("TODO");
//	documentsService.documentDAO.
//	query.set
//	List<HDocument> docs = query.list();
//	Documents result = new Documents();
//	for (HDocument hDocument : docs) {
//	    result.getDocuments().add(hDocument.toDocument());
//	    
//	}
//	return result;
	List<HDocument> hdocs = getContainer().getDocuments();
	Documents result = new Documents();
	
	for (HDocument hDocument : hdocs) {
	    result.getDocuments().add(hDocument.toDocument());
	}
	return result;
    }
    
    public void post(Documents docs) {
	log.info("post");
	List<HDocument> hdocs = new ArrayList<HDocument>();

	for (Document doc: docs.getDocuments()) {
	    hdocs.add(new HDocument(doc));
	}
	getContainer().getDocuments().addAll(hdocs);
    }
    
    
    public void put(Documents docs) {
	log.info("put");
	List<HDocument> hdocs = new ArrayList<HDocument>();

	for (Document doc: docs.getDocuments()) {
	    // FIXME if doc already exists, load it, don't create it
	    hdocs.add(new HDocument(doc));
	}
	getContainer().setDocuments(hdocs);
    }

}
