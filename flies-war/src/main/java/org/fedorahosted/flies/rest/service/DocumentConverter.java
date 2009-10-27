package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.core.dao.ResourceDAO;
import org.fedorahosted.flies.core.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.repository.model.HContainer;
import org.fedorahosted.flies.repository.model.HDataHook;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HParentResource;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HReference;
import org.fedorahosted.flies.repository.model.HDocumentResource;
import org.fedorahosted.flies.repository.model.HSimpleComment;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowHistory;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.DataHook;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Reference;
import org.fedorahosted.flies.rest.dto.Relationships;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.SimpleComment;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
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
		toHDoc.setDocId(fromDoc.getId());
		toHDoc.setName(fromDoc.getName());
		toHDoc.setPath(fromDoc.getPath());
		toHDoc.setContentType(fromDoc.getContentType());
		toHDoc.setLocale(fromDoc.getLang());
//		toHDoc.setRevision(fromDoc.getRevision());  // TODO increment revision on modify only
		// TODO handle doc extensions, especially containers
		if (fromDoc.hasResources()) {
			List<DocumentResource> docResources = fromDoc.getResources();
			List<HDocumentResource> hResources;
			if (replaceResourceTree) {
				hResources = new ArrayList<HDocumentResource>(docResources.size());
				// this should cause any obsolete HResources (and their 
				// children) to be deleted when we save
				// TODO mark them obsolete instead
				toHDoc.setResources(hResources);
			} else {
				hResources = toHDoc.getResources();
			}
			for (DocumentResource res : docResources) {
				HDocumentResource hRes = null;
				if (session.contains(toHDoc))
					// FIXME make sure getById can find pre-existing docs (we broke the link from HDoc to its HResources above)
					hRes = resourceDAO.getById(toHDoc, res.getId());
				if (hRes == null) {
					hRes = HDocument.create(res);
				} else {
					// resurrect the resource
					hRes.setObsolete(false);
				}
				hResources.add(hRes);
				hRes.setDocument(toHDoc);
				hRes.setResId(res.getId());
				copy(res, hRes, toHDoc);
			}
		}
	}

	/**
	 * Creates an HDocument from the Document, with the field 'project' 
	 * pointing to container 
	 * @param document
	 * @param container
	 * @return
	 */
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
		
		if(document.hasResources()) {
			createChildren(document, hDocument, 1);
		}
		return hDocument;
	}
	
	/**
	 * Creates the children of the Document in the HDocument
	 * @param document
	 * @param hDocument
	 * @param newRevision
	 */
	public void createChildren(Document document, HDocument hDocument, int newRevision) {
		for(DocumentResource resource : document.getResources()) {
			HDocumentResource hResource = create(resource, hDocument, null);
			hDocument.getResources().add(hResource);
		}
	}
	
	public void merge(Document document, HDocument hDocument){
		int newRevision = hDocument.getRevision() +1;
		hDocument.setRevision(newRevision);
		hDocument.setDocId(document.getId());
		hDocument.setName(document.getName());
		hDocument.setPath(document.getPath());
		hDocument.setContentType(document.getContentType());
		hDocument.setLocale(document.getLang());
		
		if(document.hasResources() ) {
			mergeChildren(document, hDocument);
		}
		
	}

	public void mergeChildren(Document document, HDocument hDocument){

		Map<String, HDocumentResource> existingResources = toMap(hDocument.getResources());
		
		List<HDocumentResource> finalHResources = hDocument.getResources();
		finalHResources.clear();
		
		for(DocumentResource resource: document.getResources()){
			
			// check existing resources first
			HDocumentResource hResource = existingResources.remove(resource.getId());
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
			hResource = create(resource, hDocument, null);
			// finally insert
			finalHResources.add(hResource);
		}

		// clean up resources we didn't process in this 
		for(HDocumentResource hResource : existingResources.values()) {
			deleteOrObsolete(hResource);
		}
	}
	
	public void mergeChildren(Container container, HParentResource hParentResource, int newRevision){
		Map<String, HDocumentResource> existingResources = toMap(hParentResource.getResources());
		
		List<HDocumentResource> finalHResources = hParentResource.getResources();
		finalHResources.clear();
		
		for(DocumentResource resource: container.getResources()){
			
			// check existing resources first
			HDocumentResource hResource = existingResources.remove(resource.getId());
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
		for(HDocumentResource hResource : existingResources.values()) {
			deleteOrObsolete(hResource);
		}
		
	}
	
	private boolean areOfSameType(DocumentResource resource, HDocumentResource hResource){
		return 
		(resource instanceof TextFlow && hResource instanceof HTextFlow) ||
		(resource instanceof Container && hResource instanceof HContainer) ||
		(resource instanceof DataHook && hResource instanceof HDataHook) ||
		(resource instanceof Reference && hResource instanceof HReference);
	}
	public void merge(DocumentResource resource, HDocumentResource hResource, int newRevision){
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
			
			// save old version to history
			HTextFlowHistory history = new HTextFlowHistory(hTextFlow);
			hTextFlow.getHistory().put(hTextFlow.getRevision(), history);
			
			// make sure to set the status of any targets to NeedReview
			for(HTextFlowTarget target :hTextFlow.getTargets().values()){
				// TODO not sure if this is the correct state
				target.setState(ContentState.ForReview);
			}
			
			hTextFlow.setRevision(newRevision);
			hTextFlow.setContent(textFlow.getContent());
		}
		
		TextFlowTargets targets = textFlow.getTargets();
		if(targets != null) {
			for(TextFlowTarget textFlowTarget : targets.getTargets()) {
				if(hTextFlow.getTargets().containsKey(textFlowTarget.getLang())){
					merge(textFlowTarget, hTextFlow.getTargets().get(textFlowTarget.getLang()), newRevision);
				}
				else{
					HTextFlowTarget hTextFlowTarget = create(textFlowTarget, newRevision);
					hTextFlow.getTargets().put(textFlowTarget.getLang(), hTextFlowTarget);
				}
			}
		}
	}	
	
	public void merge(TextFlowTarget textFlowTarget, HTextFlowTarget hTextFlowTarget, int newRevision) {
		if( !hTextFlowTarget.getContent().equals(textFlowTarget.getContent())){
			
		}
	}
	
	public HTextFlowTarget create(TextFlowTarget textFlowTarget, int newRevision){
		return null;
	}
	
	public void merge(Container container, HContainer hContainer, int newRevision){
		mergeChildren(container, hContainer, newRevision);
		// if a child is updated, we update the container version as well
		for(HDocumentResource child: hContainer.getResources()) {
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
	
	/**
	 * Creates the Hibernate equivalent of the DocumentResource 'resource', 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 * @param resource
	 * @param hDocument
	 * @param parent
	 * @return
	 */
	public HDocumentResource create(DocumentResource resource, HDocument hDocument, HParentResource parent){
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
	
	/**
	 * Creates the Hibernate equivalent of the TextFlow, 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 */
	public HTextFlow create(TextFlow textFlow, HDocument hDocument, HParentResource parent){
		HTextFlow hTextFlow =  new HTextFlow();
		hTextFlow.setDocument(hDocument);
		hTextFlow.setParent(parent);
		hTextFlow.setResId(textFlow.getId());
		hTextFlow.setRevision(hDocument.getRevision());
		hTextFlow.setContent(textFlow.getContent());
		return hTextFlow;
	}
	
	/**
	 * Creates an HContainer for the Container, creates child 
	 * HDocumentResources for the Container's DocumentResources, and
	 * sets the HContainer's parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 * @param container
	 * @param hDocument
	 * @param parent
	 * @return
	 */
	public HContainer create(Container container, HDocument hDocument, HParentResource parent){
		HContainer hContainer = new HContainer();
		hContainer.setDocument(hDocument);
		hContainer.setParent(parent);
		hContainer.setResId(container.getId());
		hContainer.setRevision(hDocument.getRevision());

		createChildren(container, hDocument, hContainer);
		
		return hContainer;
	}
	
	/**
	 * creates child HDocumentResources for the Container's DocumentResources,
	 * and adds them to the parent (HContainer)
	 * @param container
	 * @param hDocument
	 * @param parent
	 */
	public void createChildren(Container container, HDocument hDocument, HParentResource parent) {
		for(DocumentResource resource : container.getResources()) {
			HDocumentResource hResource = create(resource, hDocument, parent);
			parent.getResources().add(hResource);
		}
	}
	
	/**
	 * Creates the Hibernate equivalent of the DataHook, 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 */
	public HDataHook create(DataHook dataHook, HDocument hDocument, HParentResource parent){
		HDataHook hDataHook = new HDataHook();
		hDataHook.setDocument(hDocument);
		hDataHook.setParent(parent);
		hDataHook.setResId(dataHook.getId());
		hDataHook.setRevision(hDocument.getRevision());
		return hDataHook;
	}

	/**
	 * Creates the Hibernate equivalent of the Reference, 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 */
	public HReference create(Reference reference, HDocument hDocument, HParentResource parent){
		HReference hReference = new HReference();
		hReference.setDocument(hDocument);
		hReference.setParent(parent);
		hReference.setResId(reference.getId());
		hReference.setRevision(hDocument.getRevision());
		hReference.setRef(reference.getRelationshipId());
		return hReference;
	}
	
	public void deleteOrObsolete(HDocumentResource hResource) {
		// process leafs first
		if(hResource instanceof HParentResource) {
			HParentResource hParentResource = (HParentResource) hResource;
			for(HDocumentResource hChildResource : hParentResource.getResources()) {
				deleteOrObsolete(hChildResource);
			}
			hParentResource.getResources().clear();
		}
			
		if(hResource instanceof HTextFlow) {
			// we only keep TextFlow obsoletes
			hResource.setObsolete(true);
			hResource.setParent(null);
		}
		else{
			session.delete(hResource);
		}
	}
	
	private static Map<String, HDocumentResource> toMap(List<HDocumentResource> resources) {
		Map<String, HDocumentResource> map = new HashMap<String, HDocumentResource>(resources.size());
		for(HDocumentResource res : resources) {
			map.put(res.getResId(), res);
		}
		return map;
	}
	
	// copy res to hRes recursively, maintaining docTargets
	private void copy(DocumentResource res, HDocumentResource hRes,
			HDocument hDoc) {
		hRes.setDocument(hDoc);
		if (res instanceof TextFlow) {
			copy((TextFlow)res, (HTextFlow)hRes);
		} else {
			// FIXME handle other Resource types
			throw new RuntimeException("Unknown Resource type "+res.getClass());
		}
	}

	private void copy(TextFlow tf, HTextFlow htf) {
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
								hTarget.setResourceRevision(htf.getRevision());
								hTarget.setState(target.getState());
//						hTarget.setRevision(revision); // TODO
								hTarget.setContent(target.getContent());
							}
							copy(target, hTarget, htf);
							htf.getTargets().put(target.getLang(), hTarget);
						}
					} else if (ext instanceof SimpleComment) {
						SimpleComment simpleComment = (SimpleComment) ext;
						HSimpleComment hComment = htf.getComment();
						if (hComment == null) {
							hComment = new HSimpleComment();
							htf.setComment(hComment);
						}
						hComment.setComment(simpleComment.getValue());
					} else {
						throw new RuntimeException("Unknown TextFlow extension "+ext.getClass());
					}
				}
			}
	    }

	private void copy(TextFlowTarget target, HTextFlowTarget hTarget,
			HTextFlow htf) {
		hTarget.setContent(target.getContent());
		hTarget.setLocale(target.getLang());
		hTarget.setResourceRevision(htf.getRevision());
		hTarget.setRevision(target.getRevision());
		hTarget.setState(target.getState());
		hTarget.setTextFlow(htf);
		if(target.hasComment()) {
			HSimpleComment hComment = hTarget.getComment();
			if (hComment == null) {
				hComment = new HSimpleComment();
				hTarget.setComment(hComment);
			}
			hComment.setComment(target.getComment().getValue());
		}
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
