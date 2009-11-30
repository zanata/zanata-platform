package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.core.dao.ResourceDAO;
import org.fedorahosted.flies.core.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.repository.model.HContainer;
import org.fedorahosted.flies.repository.model.HDataHook;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentResource;
import org.fedorahosted.flies.repository.model.HParentResource;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.model.HReference;
import org.fedorahosted.flies.repository.model.HSimpleComment;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowHistory;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.DataHook;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Reference;
import org.fedorahosted.flies.rest.dto.Relationships;
import org.fedorahosted.flies.rest.dto.SimpleComment;
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
@Name("documentConverter")
public class DocumentConverter {

    @Logger 
    private Log log;
    
    @In 
    private ResourceDAO resourceDAO;
    @In 
    private TextFlowTargetDAO textFlowTargetDAO;
    @In 
    private Session session;

    /**
     * Recursively copies from the source Document to the destination HDocument.
     * Increments toHDoc's revision number if any resources were changed
     * @param fromDoc source Document
     * @param toHDoc destination HDocument
     */
    public void copy(Document fromDoc, HDocument toHDoc) {
    	boolean docChanged = false;
    	int nextDocRev = 1;
    	if (!session.contains(toHDoc)) {
    		// new document
    		docChanged = true;
    		log.debug("CHANGED: Document {0} is new", toHDoc.getDocId());
    	} else {
    		nextDocRev = toHDoc.getRevision()+1;
    	}
    	// changing these attributes probably shouldn't 
    	// invalidate existing translations, so we don't
    	// bother incrementing the doc rev
    	toHDoc.setDocId(fromDoc.getId());
    	toHDoc.setName(fromDoc.getName());
    	toHDoc.setPath(fromDoc.getPath());
    	toHDoc.setContentType(fromDoc.getContentType());
    	toHDoc.setLocale(fromDoc.getLang());
    	// don't copy revision; we don't accept revision from the client
    	List<DocumentResource> fromDocResources = Collections.emptyList();
    	if (fromDoc.hasResources()) {
    		fromDocResources = fromDoc.getResources();
    	}
    	List<HDocumentResource> hResources;
    	Map<String, HDocumentResource> oldResourceMap = new HashMap<String, HDocumentResource>();
    	List<HDocumentResource> oldResources = toHDoc.getResources();
    	for(HDocumentResource oldResource : oldResources) {
    		oldResourceMap.put(oldResource.getResId(), oldResource);
    	}
    	hResources = new ArrayList<HDocumentResource>(fromDocResources.size());
    	// We create an empty list for HDocument.resources, and build it up
    	// in the order of fromDoc's resources.  This ensures that we preserve
    	// the order of the list.
    	toHDoc.setResources(hResources);
    	for (DocumentResource fromRes : fromDocResources) {
    		HDocumentResource hRes = null;
    		if (session.contains(toHDoc))
    			hRes = resourceDAO.getById(toHDoc, fromRes.getId());
    		boolean resChanged = false;
    		if (hRes == null) {
    			resChanged = true; // this will cause res.revision to be set below
    			hRes = toHDoc.create(fromRes, nextDocRev); // 
    		} else {
    			// resurrect the resource
    			hRes.setObsolete(false);
    		}
    		hResources.add(hRes);

    		resChanged |= copy(fromRes, hRes, toHDoc, nextDocRev);
    		if (resChanged) {
    			hRes.setRevision(nextDocRev);
    			docChanged = true;
    		}
    		if (oldResourceMap.remove(fromRes.getId()) == null) {
    			docChanged = true;
    			log.debug("CHANGED: Resource {0}:{1} was added", toHDoc.getDocId(), hRes.getResId());
    		}
    	}
    	// even if we just move around resources without changing them,
    	// the document is considered changed
    	if(!oldResources.equals(hResources))
    		docChanged = true;
    	// mark any removed resources as obsolete
    	for(HDocumentResource oldResource : oldResourceMap.values()) {
    		deleteOrObsolete(oldResource);
    		docChanged = true;
    		log.debug("CHANGED: Resource {0}:{1} was removed", toHDoc.getDocId(), oldResource.getResId());
    	}
    	if (docChanged)
    		toHDoc.setRevision(nextDocRev);
    }

	/**
	 * Creates an HDocument from the Document, with the field 'project' 
	 * pointing to container 
	 * @param fromDocument
	 * @param container
	 * @return
	 */
	public HDocument _create(Document fromDocument, HProjectContainer container){
		HDocument hDocument = new HDocument();
		hDocument.setDocId(fromDocument.getId());
		hDocument.setDocId(fromDocument.getId());
		hDocument.setName(fromDocument.getName());
		hDocument.setPath(fromDocument.getPath());
		hDocument.setContentType(fromDocument.getContentType());
		hDocument.setLocale(fromDocument.getLang());
		hDocument.setRevision(1);
		hDocument.setProject(container);
		
		if(fromDocument.hasResources()) {
			createChildren(fromDocument, hDocument, 1);
		}
		return hDocument;
	}

	public void _merge(Document fromDocument, HDocument hDocument){
		int newRevision = hDocument.getRevision() +1;
		hDocument.setRevision(newRevision);
		hDocument.setDocId(fromDocument.getId());
		hDocument.setName(fromDocument.getName());
		hDocument.setPath(fromDocument.getPath());
		hDocument.setContentType(fromDocument.getContentType());
		hDocument.setLocale(fromDocument.getLang());
		
		if(fromDocument.hasResources() ) {
			mergeChildren(fromDocument, hDocument, newRevision);
		}
		
	}

	
	/**
	 * Creates the children of the Document in the HDocument
	 * @param fromDocument
	 * @param hDocument
	 * @param newRevision
	 */
	private void createChildren(Document fromDocument, HDocument hDocument, int newRevision) {
		for(DocumentResource resource : fromDocument.getResources()) {
			HDocumentResource hResource = create(resource, hDocument, null, newRevision);
			hDocument.getResources().add(hResource);
		}
	}
	
	private void mergeChildren(Document fromDocument, HDocument hDocument, int nextDocRev){

		Map<String, HDocumentResource> existingResources = toMap(hDocument.getResources());
		
		List<HDocumentResource> finalHResources = hDocument.getResources();
		finalHResources.clear();
		
		for(DocumentResource fromResource: fromDocument.getResources()){
			
			// check existing resources first
			HDocumentResource hResource = existingResources.remove(fromResource.getId());
			if(hResource == null) {
				hResource = resourceDAO.getObsoleteById(hDocument, fromResource.getId());	
			}
			if(hResource != null) {
				// need to delete and recreate if same ids but conflicting types
				if(!areOfSameType(fromResource, hResource)){
					if(hResource instanceof HTextFlow){
						session.delete(hResource);
					}
					else{
						deleteOrObsolete(hResource);
					}
					hResource = create(fromResource, hResource.getDocument(), null, nextDocRev);
				}
				hResource.setObsolete(false);
				merge(fromResource, hResource, nextDocRev);
				finalHResources.add(hResource);
				continue;
			}
			hResource = create(fromResource, hDocument, null, nextDocRev);
			// finally insert
			finalHResources.add(hResource);
		}

		// clean up resources we didn't process in this 
		for(HDocumentResource hResource : existingResources.values()) {
			deleteOrObsolete(hResource);
		}
	}
	

	/**
	 * Copies fromRes to hRes recursively, maintaining docTargets.
	 * Returns true if hRes was changed.
	 * @param nextDocRev 
	 */
	private boolean copy(DocumentResource fromRes, HDocumentResource hRes,
			HDocument hDoc, int nextDocRev) {
		hRes.setDocument(hDoc);
		boolean resChanged;
		if (fromRes instanceof TextFlow) {
			resChanged = copy((TextFlow)fromRes, (HTextFlow)hRes, nextDocRev);
		} else if (fromRes instanceof DataHook) {
			resChanged = copy((DataHook)fromRes, (HDataHook)hRes, nextDocRev);
		} else if (fromRes instanceof Reference) {
			resChanged = copy((Reference)fromRes, (HReference)hRes, nextDocRev);
		} else if (fromRes instanceof Container) {
			resChanged = copy((Container)fromRes, (HContainer)hRes, nextDocRev);
		} else {
			// FIXME handle other Resource types
			throw new RuntimeException("Unknown Resource type "+fromRes.getClass());
		}
		return resChanged;
	}
	
	/**
	 * Creates the Hibernate equivalent of the DocumentResource 'resource', 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 * @param fromResource
	 * @param hDocument
	 * @param parent
	 * @return
	 */
	private HDocumentResource create(DocumentResource fromResource, HDocument hDocument, HParentResource parent, int nextDocRev){
		if(fromResource instanceof TextFlow) 
			return create( (TextFlow) fromResource, hDocument, parent, nextDocRev);
		else if(fromResource instanceof Container) 
			return create( (Container) fromResource, hDocument, parent, nextDocRev);
		else if(fromResource instanceof DataHook) 
			return create( (DataHook) fromResource, hDocument, parent, nextDocRev);
		else if(fromResource instanceof Reference) 
			return create( (Reference) fromResource, hDocument, parent, nextDocRev);
		else
			throw new RuntimeException("missing type - programming error");
	}

	private void merge(DocumentResource fromResource, HDocumentResource hResource, int newRevision){
		if(!areOfSameType(fromResource, hResource))
			throw new IllegalArgumentException("Resource and HResource must be of same type");
		if(fromResource instanceof TextFlow) 
			merge((TextFlow) fromResource, (HTextFlow) hResource, newRevision);
		else if(fromResource instanceof Container) 
			merge((Container) fromResource, (HContainer) hResource, newRevision);
		else if(fromResource instanceof DataHook) 
			merge((DataHook) fromResource, (HDataHook) hResource, newRevision);
		else if(fromResource instanceof Reference) 
			merge((Reference) fromResource, (HReference) hResource, newRevision);
		else
			throw new RuntimeException("missing type - programming error");
	}
	
	/**
	 * Returns true if the content (or a comment) of htf was changed
	 */
	private boolean copy(TextFlow fromTf, HTextFlow htf, int nextDocRev) {
		// TODO record in history table: see merge(TextFlow, HTextFlow, int)
		boolean changed = false;
		if (!fromTf.getContent().equals(htf.getContent())) {
			changed = true;
			log.debug("CHANGED: TextFlow {0}:{1} content changed", htf.getDocument().getDocId(), htf.getResId());
		}
	
		htf.setContent(fromTf.getContent());
		List<Object> extensions = fromTf.getExtensions();
		if (extensions != null) {
			for (Object ext : extensions) {
				if (ext instanceof TextFlowTargets) {
					// do targets last: if the comment changes, the resourceRev will have to be incremented
				} else if (ext instanceof SimpleComment) {
					SimpleComment simpleComment = (SimpleComment) ext;
					HSimpleComment hComment = htf.getComment();
					if (hComment == null) {
						changed = true;
						log.debug("CHANGED: TextFlow {0}:{1} comment changed", htf.getDocument().getDocId(), htf.getResId());
						hComment = new HSimpleComment();
						htf.setComment(hComment);
					} else {
						if (!hComment.getComment().equals(simpleComment.getValue()))
							changed = true;
					}
					hComment.setComment(simpleComment.getValue());
				} else {
					throw new RuntimeException("Unknown TextFlow extension "+ext.getClass());
				}
			}
			TextFlowTargets targets = fromTf.getTargets();
			if (targets != null) {
				for (TextFlowTarget target : targets.getTargets()) {
					HTextFlowTarget hTarget = null;
					if (session.contains(htf)) {
						hTarget = textFlowTargetDAO.getByNaturalId(htf, target.getLang());
					}
					if (hTarget == null) {
						hTarget = new HTextFlowTarget();
						hTarget.setLocale(target.getLang());
						hTarget.setTextFlow(htf);
						Integer resourceRev;
						
						if (changed || htf.getRevision() == null)
							resourceRev = nextDocRev;
						else
							resourceRev = htf.getRevision();
							
						hTarget.setState(target.getState());
	//				hTarget.setRevision(revision); // TODO
						hTarget.setContent(target.getContent());
						copy(target, hTarget, htf);
						hTarget.setResourceRevision(resourceRev);
						hTarget.setRevision(1);
					} else {
						copy(target, hTarget, htf);
					}
					htf.getTargets().put(target.getLang(), hTarget);
				}
			}
		}
		return changed;
    }
	
	/**
	 * Creates the Hibernate equivalent of the TextFlow, 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 */
	private HTextFlow create(TextFlow fromTextFlow, HDocument hDocument, HParentResource parent, int nextDocRev){
		HTextFlow hTextFlow =  new HTextFlow();
		hTextFlow.setDocument(hDocument);
		hTextFlow.setParent(parent);
		hTextFlow.setResId(fromTextFlow.getId());
		hTextFlow.setRevision(nextDocRev);
		hTextFlow.setContent(fromTextFlow.getContent());
		return hTextFlow;
	}

	private void merge(TextFlow fromTextFlow, HTextFlow hTextFlow, int newRevision){
		if(!hTextFlow.getContent().equals(fromTextFlow.getContent())){
			
			// save old version to history
			HTextFlowHistory history = new HTextFlowHistory(hTextFlow);
			hTextFlow.getHistory().put(hTextFlow.getRevision(), history);
			
			// make sure to set the status of any targets to NeedReview
			for(HTextFlowTarget target :hTextFlow.getTargets().values()){
				// TODO not sure if this is the correct state
				target.setState(ContentState.NeedReview);
			}
			
			hTextFlow.setRevision(newRevision);
			hTextFlow.setContent(fromTextFlow.getContent());
		}
		
		TextFlowTargets targets = fromTextFlow.getTargets();
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

	
	private void copy(TextFlowTarget target, HTextFlowTarget hTarget,
			HTextFlow htf) {
		boolean changed = false;
		changed |= !target.getContent().equals(hTarget.getContent());
		hTarget.setContent(target.getContent());
		changed |= !target.getLang().equals(hTarget.getLocale());
		hTarget.setLocale(target.getLang());
		hTarget.setResourceRevision(htf.getRevision());
		changed |= !target.getState().equals(hTarget.getState());
		hTarget.setState(target.getState());
		hTarget.setTextFlow(htf);
		if(target.hasComment()) {
			HSimpleComment hComment = hTarget.getComment();
			if (hComment == null) {
				hComment = new HSimpleComment();
				hTarget.setComment(hComment);
			}
			changed |= !target.getComment().equals(hComment.getComment());
			hComment.setComment(target.getComment().getValue());
		} else {
			changed |= (hTarget.getComment() != null);
		}
		if (changed)
			hTarget.setRevision(hTarget.getRevision()+1);
	}
	
	private HTextFlowTarget create(TextFlowTarget textFlowTarget, int newRevision){
		return null;
	}
	
	private void merge(TextFlowTarget textFlowTarget, HTextFlowTarget hTextFlowTarget, int newRevision) {
		if( !hTextFlowTarget.getContent().equals(textFlowTarget.getContent())){
			
		}
	}
	
	private boolean copy(Container fromContainer, HContainer hContainer, int nextDocRev) {
		merge(fromContainer, hContainer, nextDocRev);
		return true; // TODO
	}

	/**
	 * Creates an HContainer for the Container, creates child 
	 * HDocumentResources for the Container's DocumentResources, and
	 * sets the HContainer's parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 * @param fromContainer
	 * @param hDocument
	 * @param parent
	 * @return
	 */
	private HContainer create(Container fromContainer, HDocument hDocument, HParentResource parent, int nextDocRev){
		HContainer hContainer = new HContainer();
		hContainer.setDocument(hDocument);
		hContainer.setParent(parent);
		hContainer.setResId(fromContainer.getId());
		hContainer.setRevision(nextDocRev);

		createChildren(fromContainer, hDocument, hContainer, nextDocRev);
		
		return hContainer;
	}

	/**
	 * creates child HDocumentResources for the Container's DocumentResources,
	 * and adds them to the parent (HContainer)
	 * @param fromContainer
	 * @param hDocument
	 * @param parent
	 */
	private void createChildren(Container fromContainer, HDocument hDocument, HParentResource parent, int nextDocRev) {
		for(DocumentResource resource : fromContainer.getResources()) {
			HDocumentResource hResource = create(resource, hDocument, parent, nextDocRev);
			parent.getResources().add(hResource);
		}
	}
	
	private void merge(Container fromContainer, HContainer hContainer, int newRevision){
		mergeChildren(fromContainer, hContainer, newRevision);
		// if a child is updated, we update the container version as well
		for(HDocumentResource child: hContainer.getResources()) {
			if(newRevision == child.getRevision()){//FIXME
				hContainer.setRevision(newRevision);
			}
		}
	}
	
	private void mergeChildren(Container fromContainer, HParentResource hParentResource, int newRevision){
		Map<String, HDocumentResource> existingResources = toMap(hParentResource.getResources());
		
		List<HDocumentResource> finalHResources = hParentResource.getResources();
		finalHResources.clear();
		
		for(DocumentResource fromResource: fromContainer.getResources()){
			
			// check existing resources first
			HDocumentResource hResource = existingResources.remove(fromResource.getId());
			if(hResource == null) {
				hResource = resourceDAO.getObsoleteById(hParentResource.getDocument(), fromResource.getId());	
			}
			if(hResource != null) {
				// need to delete and recreate if same ids but conflicting types
				if(!areOfSameType(fromResource, hResource)){
					if(hResource instanceof HTextFlow){
						session.delete(hResource);
					}
					else{
						deleteOrObsolete(hResource);
					}
					hResource = create(fromResource, hResource.getDocument(), hParentResource, newRevision);
				}
				hResource.setObsolete(false);
				merge(fromResource, hResource, newRevision);
				finalHResources.add(hResource);
				continue;
			}
			
			
			// finally insert
			finalHResources.add( create(fromResource, hParentResource.getDocument(), hParentResource, newRevision));
		}

		// clean up resources we didn't process in this 
		for(HDocumentResource hResource : existingResources.values()) {
			deleteOrObsolete(hResource);
		}
		
	}
		
	
	private boolean copy(DataHook fromDH, HDataHook hDH, int nextDocRev) {
		merge(fromDH, hDH, nextDocRev);
		return true; // TODO
	}
	
	/**
	 * Creates the Hibernate equivalent of the DataHook, 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 */
	private HDataHook create(DataHook fromDataHook, HDocument hDocument, HParentResource parent, int nextDocRev){
		HDataHook hDataHook = new HDataHook();
		hDataHook.setDocument(hDocument);
		hDataHook.setParent(parent);
		hDataHook.setResId(fromDataHook.getId());
		hDataHook.setRevision(nextDocRev);
		return hDataHook;
	}
	
	private void merge(DataHook fromDataHook, HDataHook hDataHook, int newRevision){
	}	
	

	private boolean copy(Reference fromRef, HReference hRef, int nextDocRev) {
		merge(fromRef, hRef, nextDocRev);
		return true; // TODO
	}
	
	/**
	 * Creates the Hibernate equivalent of the Reference, 
	 * setting parent to 'parent', setting document to hDocument, 
	 * inheriting hDocument's revision.
	 */
	private HReference create(Reference fromReference, HDocument hDocument, HParentResource parent, int nextDocRev){
		HReference hReference = new HReference();
		hReference.setDocument(hDocument);
		hReference.setParent(parent);
		hReference.setResId(fromReference.getId());
		hReference.setRevision(nextDocRev);
		hReference.setRef(fromReference.getRelationshipId());
		return hReference;
	}
	
	private void merge(Reference fromReference, HReference hReference, int newRevision){
		if(!hReference.getRef().equals(fromReference.getRelationshipId())){
			hReference.setRevision(newRevision);
			hReference.setRef(fromReference.getRelationshipId());
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
	
	private boolean areOfSameType(DocumentResource resource, HDocumentResource hResource){
		return 
		(resource instanceof TextFlow && hResource instanceof HTextFlow) ||
		(resource instanceof Container && hResource instanceof HContainer) ||
		(resource instanceof DataHook && hResource instanceof HDataHook) ||
		(resource instanceof Reference && hResource instanceof HReference);
	}

	private void deleteOrObsolete(HDocumentResource hResource) {
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
}
