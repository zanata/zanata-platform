package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.dao.ResourceDAO;
import org.fedorahosted.flies.core.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentTarget;
import org.fedorahosted.flies.repository.model.HResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Relationships;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@AutoCreate
@Scope(ScopeType.STATELESS)
@Name("documentConverter")
public class DocumentConverter {

    @In 
    private ResourceDAO resourceDAO;
    @In 
    private TextFlowTargetDAO textFlowTargetDAO;
    @In 
    private Session session;

    /**
     * Recursively copies from the source Document to the destination HDocument
     * @param fromDoc source Document
     * @param toHDoc destination HDocument
     * @param replaceResourceTree should probably always be true
     */
	public void copy(Document fromDoc, HDocument toHDoc, boolean replaceResourceTree) {
//		copyMetaData(fromDoc, toHDoc);
		toHDoc.setDocId(fromDoc.getId());
		toHDoc.setName(fromDoc.getName());
		toHDoc.setPath(fromDoc.getPath());
		toHDoc.setContentType(fromDoc.getContentType());
		toHDoc.setLocale(fromDoc.getLang());
//		toHDoc.setRevision(fromDoc.getVersion());  // TODO check version/revision!
		// TODO handle doc extensions
		if (fromDoc.hasResources()) {
			List<Resource> docResources = fromDoc.getResources();
			Map<LocaleId, HDocumentTarget> docTargets = toHDoc.getTargets();
			List<HResource> hResources;
			if (replaceResourceTree) {
				hResources = new ArrayList<HResource>(docResources.size());
				// this should cause any obsolete HResources (and their 
				// children) to be deleted when we save
				toHDoc.setResourceTree(hResources);
			} else {
				hResources = toHDoc.getResourceTree();
			}
			for (Resource res : docResources) {
				HResource hRes = null;
				if (session.contains(toHDoc))
					// FIXME make sure getById can find pre-existing docs (we broke the link from HDoc to its HResources above)
					hRes = resourceDAO.getById(toHDoc, res.getId());
				if (hRes == null)
					hRes = HDocument.create(res);
				hResources.add(hRes);
				hRes.setDocument(toHDoc);
				hRes.setResId(res.getId());
				session.save(hRes);
				copy(res, hRes, toHDoc, docTargets);
			}
		}
		session.save(toHDoc);
	}

	// copy res to hRes recursively, maintaining docTargets
	private void copy(Resource res, HResource hRes,
			HDocument hDoc, Map<LocaleId, HDocumentTarget> docTargets) {
		hRes.setDocument(hDoc);
		if (res instanceof TextFlow) {
			copy((TextFlow)res, (HTextFlow)hRes, docTargets);
		} else {
			// FIXME handle other Resource types
			throw new RuntimeException("Unknown Resource type "+res.getClass());
		}
	}

	private void copy(TextFlow tf, HTextFlow htf, Map<LocaleId, HDocumentTarget> docTargets) {
			htf.setContent(tf.getContent());
			List<Object> extensions = tf.getExtensions();
			if (extensions != null) {
				for (Object ext : extensions) {
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
							session.save(hTarget);
						}
					} else {
						throw new RuntimeException("Unknown TextFlow extension "+ext.getClass());
					}
				}
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
	
	public void addLinks(Document doc, URI docUri, URI iterationUri) {
		// add self relation
		Link link = new Link(docUri, Relationships.SELF); 
		doc.getLinks().add(link);

		// add container relation
		link = new Link(
				iterationUri, 
				Relationships.DOCUMENT_CONTAINER, 
				MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML);
		doc.getLinks().add(link);
	}
	

	
}
