package org.fedorahosted.flies.rest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.core.dao.ResourceDAO;
import org.fedorahosted.flies.core.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentTarget;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
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
    
    @In DocumentDAO documentDAO;
    @In ProjectContainerDAO projectContainerDAO;
    @In ResourceDAO resourceDAO;
    @In TextFlowTargetDAO textFlowTargetDAO;

    @In
    Session session;
	
    private HProjectContainer getContainer() {
	return projectContainerDAO.getBySlug(documentsService.getProjectSlug(), documentsService.getIterationSlug());
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
    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = documentDAO.getByDocId(hContainer, doc.getId());
    		if (hDoc == null) {
    			hDoc = new HDocument(doc);
    			hDoc.setProject(hContainer);
    		}
    		getContainer().getDocuments().put(hDoc.getDocId(), hDoc);
    		session.save(hDoc);
    		copy(doc, hDoc, false);
    	}
    	session.flush();
    }
    
    
    public void put(Documents docs) {
    	log.info("HTTP PUT "+documentsService.getRequest().getRequestURL()+" :\n"+docs);
    	HProjectContainer hContainer = getContainer();
    	for (Document doc: docs.getDocuments()) {
			// if doc already exists, load it and update it, but don't create it
    		HDocument hDoc = documentDAO.getByDocId(hContainer, doc.getId());
    		if (hDoc == null) {
    			hDoc = new HDocument(doc);
    			hDoc.setProject(hContainer);
    		}
    		hContainer.getDocuments().put(hDoc.getDocId(), hDoc);
    		session.save(hDoc);
    		copy(doc, hDoc, true);
    	}
    	// TODO ensure omitted docs get deleted by Hibernate
    	session.flush();
    }
    
    private void copy(Document docInfo, HDocument hDoc, boolean put) {
		hDoc.setDocId(docInfo.getId());
		hDoc.setName(docInfo.getName());
		hDoc.setPath(docInfo.getPath());
		hDoc.setContentType(docInfo.getContentType());
		hDoc.setLocale(docInfo.getLang());
		hDoc.setRevision(docInfo.getVersion());  // TODO check this!
		Map<LocaleId, HDocumentTarget> docTargets = hDoc.getTargets();
		List<HResource> hResources;
		if (put) {
			int size = docInfo.hasResources() ? docInfo.getResources().size() : 0;
			hResources = new ArrayList<HResource>(size);
			// this should cause any unreferenced HResources to be deleted when we save
			hDoc.setResourceTree(hResources);
		} else {
			hResources = hDoc.getResourceTree();
		}
		for (Resource res : docInfo.getResources()) {
			HResource hRes = null;
			if (session.contains(hDoc))
				// FIXME test this! (we broke the link from HDoc to its HResources above)
				hRes = resourceDAO.getById(hDoc, res.getId());
			if (hRes == null)
				hRes = HDocument.create(res);
			hResources.add(hRes);
			hRes.setDocument(hDoc);
			hRes.setResId(res.getId());
			session.save(hRes);
			// TODO copy res to hRes recursively, maintaining docTargets
			copy(res, hRes, hDoc, docTargets);
		}
    }
    
    private void copy(Resource res, HResource hRes,
    		HDocument hDoc, Map<LocaleId, HDocumentTarget> docTargets) {
    	hRes.setDocument(hDoc);
		if (res instanceof TextFlow) {
			copy((TextFlow)res, (HTextFlow)hRes, docTargets);
		} // TODO else?
	}

	private void copy(TextFlow tf, HTextFlow htf, Map<LocaleId, HDocumentTarget> docTargets) {
		htf.setContent(tf.getContent());
		for (Object ext : tf.getExtensions()) {
			if (ext instanceof TextFlowTargets) {
				TextFlowTargets targets = (TextFlowTargets) ext;
				for (TextFlowTarget target : targets.getTargets()) {
					HTextFlowTarget hTarget = null;
					if (session.contains(htf)) {
						hTarget = textFlowTargetDAO.getByNaturalId(htf, target.getLang());
					}
					if (hTarget == null) {
						hTarget = new HTextFlowTarget();
						hTarget.setLocale(target.getLang());
						hTarget.setTextFlow(htf);
						hTarget.setState(target.getState());
//						hTarget.setRevision(revision);
						hTarget.setContent(target.getContent());
						HDocumentTarget docTarget = docTargets.get(target.getLang());
						if (docTarget == null) {
							docTarget = new HDocumentTarget(htf.getDocument(), target.getLang());
							docTargets.put(target.getLang(), docTarget);
							session.save(docTarget);
						}
						hTarget.setDocumentTarget(docTarget);
						docTarget.getTargets().add(hTarget);
						session.save(hTarget);
					}
					copy(target, hTarget, htf, docTargets);
					htf.getTargets().put(target.getLang(), hTarget);
				}
			} //TODO else?
		}
    }

	private void copy(TextFlowTarget target, HTextFlowTarget hTarget,
			HTextFlow htf, Map<LocaleId, HDocumentTarget> docTargets) {
		hTarget.setContent(target.getContent());
		hTarget.setLocale(target.getLang());
		hTarget.setRevision(target.getVersion());
		hTarget.setState(target.getState());
		hTarget.setTextFlow(htf);
	}

}
