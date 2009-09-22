package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.dao.ResourceDAO;
import org.fedorahosted.flies.core.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.repository.model.HContainer;
import org.fedorahosted.flies.repository.model.HDataHook;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentTarget;
import org.fedorahosted.flies.repository.model.HParentResource;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HReference;
import org.fedorahosted.flies.repository.model.HResource;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.DataHook;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Reference;
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

	public HDocument create(Document document, HProjectContainer container){
		HDocument hDocument = new HDocument();
		hDocument.setDocId(document.getId());
		hDocument.setDocId(document.getId());
		hDocument.setName(document.getName());
		hDocument.setPath(document.getPath());
		hDocument.setContentType(document.getContentType());
		hDocument.setLocale(document.getLang());
		hDocument.setRevision(1);
		hDocument.setProject(container);
		session.save(hDocument);
		
		if(document.hasResources()) {
			createChildren(document, hDocument, 1);
		}
		return hDocument;
	}
	
	public void createChildren(Document document, HDocument hDocument, int newRevision) {
		for(Resource resource : document.getResources()) {
			HResource hResource = create(resource, hDocument, null);
			hDocument.getResourceTree().add(hResource);
		}
		session.update(hDocument);
	}
	
	public void merge(Document document, HDocument hDocument){
		int newRevision = hDocument.getRevision() +1;
		hDocument.setRevision(newRevision);
		hDocument.setDocId(document.getId());
		hDocument.setName(document.getName());
		hDocument.setPath(document.getPath());
		hDocument.setContentType(document.getContentType());
		hDocument.setLocale(document.getLang());
		session.update(hDocument);
		
		if(document.hasResources() ) {
			mergeChildren(document, hDocument);
		}
		
	}

	public void mergeChildren(Document document, HDocument hDocument){

		Map<String, HResource> existingResources = toMap(hDocument.getResourceTree());
		
		List<HResource> finalHResources = hDocument.getResourceTree();
		finalHResources.clear();
		
		for(Resource resource: document.getResources()){
			
			// check existing resources first
			HResource hResource = existingResources.remove(resource.getId());
			if(hResource == null) {
				hResource = resourceDAO.getObsoleteById(hDocument, resource.getId());	
			}
			if(hResource != null) {
				// need to delete and recreate if same ids but conflicting types
				if(!areOfSameType(resource, hResource)){
					if(hResource instanceof HTextFlow){
						session.delete(hResource);
					}
					else{
						deleteOrObsolete(hResource);
					}
					hResource = create(resource, hResource.getDocument(), null);
				}
				hResource.setObsolete(false);
				merge(resource, hResource, hDocument.getRevision());
				finalHResources.add(hResource);
				continue;
			}
			
			
			// finally insert
			finalHResources.add( create(resource, hDocument, null));
		}

		// clean up resources we didn't process in this 
		for(HResource hResource : existingResources.values()) {
			deleteOrObsolete(hResource);
		}
	}
	
	public void mergeChildren(Container container, HParentResource hParentResource, int newRevision){
		Map<String, HResource> existingResources = toMap(hParentResource.getChildren());
		
		List<HResource> finalHResources = hParentResource.getChildren();
		finalHResources.clear();
		
		for(Resource resource: container.getContent()){
			
			// check existing resources first
			HResource hResource = existingResources.remove(resource.getId());
			if(hResource == null) {
				hResource = resourceDAO.getObsoleteById(hParentResource.getDocument(), resource.getId());	
			}
			if(hResource != null) {
				// need to delete and recreate if same ids but conflicting types
				if(!areOfSameType(resource, hResource)){
					if(hResource instanceof HTextFlow){
						session.delete(hResource);
					}
					else{
						deleteOrObsolete(hResource);
					}
					hResource = create(resource, hResource.getDocument(), hParentResource);
				}
				hResource.setObsolete(false);
				merge(resource, hResource, newRevision);
				finalHResources.add(hResource);
				continue;
			}
			
			
			// finally insert
			finalHResources.add( create(resource, hResource.getDocument(), hParentResource));
		}

		// clean up resources we didn't process in this 
		for(HResource hResource : existingResources.values()) {
			deleteOrObsolete(hResource);
		}
		
	}
	
	private boolean areOfSameType(Resource resource, HResource hResource){
		return 
		(resource instanceof TextFlow && hResource instanceof HTextFlow) ||
		(resource instanceof Container && hResource instanceof HContainer) ||
		(resource instanceof DataHook && hResource instanceof HDataHook) ||
		(resource instanceof Reference && hResource instanceof HReference);
	}
	public void merge(Resource resource, HResource hResource, int newRevision){
		if(!areOfSameType(resource, hResource))
			throw new IllegalArgumentException("Resource and HResource must be of same type");
		if(resource instanceof TextFlow) 
			merge( (TextFlow) resource, (HTextFlow) hResource, newRevision);
		else if(resource instanceof Container) 
			merge( (Container) resource, (HContainer) hResource, newRevision);
		else if(resource instanceof DataHook) 
			merge( (DataHook) resource, (HDataHook) hResource, newRevision);
		else if(resource instanceof Reference) 
			merge( (Reference) resource, (HReference) hResource, newRevision);
		else
			throw new RuntimeException("missing type - programming error");
		
	}
	public void merge(TextFlow textFlow, HTextFlow hTextFlow, int newRevision){
		if(!hTextFlow.getContent().equals(textFlow.getContent())){
			hTextFlow.setRevision(newRevision);
			hTextFlow.setContent(textFlow.getContent());
		}
	}	
	public void merge(Container container, HContainer hContainer, int newRevision){
		mergeChildren(container, hContainer, newRevision);
		// if a child is updated, we update the container version as well
		for(HResource child: hContainer.getChildren()) {
			if(newRevision == child.getRevision()){
				hContainer.setRevision(newRevision);
			}
		}
	}
	
	public void merge(DataHook dataHook, HDataHook hDataHook, int newRevision){
	}	
	
	public void merge(Reference reference, HReference hReference, int newRevision){
		if(!hReference.getRef().equals(reference.getRelationshipId())){
			hReference.setRevision(newRevision);
			hReference.setRef(reference.getRelationshipId());
		}
	}	
	
	public HResource create(Resource resource, HDocument hDocument, HParentResource parent){
		if(resource instanceof TextFlow) 
			return create( (TextFlow) resource, hDocument, parent);
		else if(resource instanceof Container) 
			return create( (Container) resource, hDocument, parent);
		else if(resource instanceof DataHook) 
			return create( (DataHook) resource, hDocument, parent);
		else if(resource instanceof Reference) 
			return create( (Reference) resource, hDocument, parent);
		else
			throw new RuntimeException("missing type - programming error");
	}
	public HTextFlow create(TextFlow textFlow, HDocument hDocument, HParentResource parent){
		HTextFlow hTextFlow =  new HTextFlow();
		hTextFlow.setDocument(hDocument);
		hTextFlow.setParent(parent);
		hTextFlow.setResId(textFlow.getId());
		hTextFlow.setRevision(hDocument.getRevision());
		hTextFlow.setContent(textFlow.getContent());
		return hTextFlow;
	}
	public HContainer create(Container container, HDocument hDocument, HParentResource parent){
		HContainer hContainer = new HContainer();
		hContainer.setDocument(hDocument);
		hContainer.setParent(parent);
		hContainer.setResId(container.getId());
		hContainer.setRevision(hDocument.getRevision());

		createChildren(container, hDocument, parent);
		
		return hContainer;
	}
	
	public void createChildren(Container container, HDocument hDocument, HParentResource parent) {
		for(Resource resource : container.getContent()) {
			HResource hResource = create(resource, hDocument, parent);
			parent.getChildren().add(hResource);
		}
	}
	
	public HDataHook create(DataHook dataHook, HDocument hDocument, HParentResource parent){
		HDataHook hDataHook = new HDataHook();
		hDataHook.setDocument(hDocument);
		hDataHook.setParent(parent);
		hDataHook.setResId(dataHook.getId());
		hDataHook.setRevision(hDocument.getRevision());
		return hDataHook;
	}
	public HReference create(Reference reference, HDocument hDocument, HParentResource parent){
		HReference hReference = new HReference();
		hReference.setDocument(hDocument);
		hReference.setParent(parent);
		hReference.setResId(reference.getId());
		hReference.setRevision(hDocument.getRevision());
		hReference.setRef(reference.getRelationshipId());
		return hReference;
	}
	
	public void deleteOrObsolete(HResource hResource) {
		// process leafs first
		if(hResource instanceof HParentResource) {
			HParentResource hParentResource = (HParentResource) hResource;
			for(HResource hChildResource : hParentResource.getChildren()) {
				deleteOrObsolete(hChildResource);
			}
			hParentResource.getChildren().clear();
		}
			
		if(hResource instanceof HTextFlow) {
			// we only keep TextFlow obsoletes
			hResource.setObsolete(true);
			session.update(hResource);
		}
		else{
			session.delete(hResource);
		}
	}
	
	private static Map<String, HResource> toMap(List<HResource> resources) {
		Map<String, HResource> map = new HashMap<String, HResource>(resources.size());
		for(HResource res : resources) {
			map.put(res.getResId(), res);
		}
		return map;
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
