package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.jboss.errai.bus.server.annotations.ExposeEntity;

@ExposeEntity 
public class TransUnitUpdated implements SessionEventData, HasTransUnitUpdatedData {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private DocumentId documentId;
	private ContentState previousStatus;
	private ContentState newStatus;
	
	// for ExposeEntity
	public TransUnitUpdated() {
	}
	
	public TransUnitUpdated(DocumentId documentId, TransUnitId transUnitId, ContentState previousStatus, ContentState newStatus) {
		this.documentId = documentId;
		this.transUnitId = transUnitId;
		this.previousStatus = previousStatus;
		this.newStatus = newStatus;
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(DocumentId documentId) {
		this.documentId = documentId;
	}
	
	@Override
	public ContentState getNewStatus() {
		return newStatus;
	}
	
	public void setNewStatus(ContentState newStatus) {
		this.newStatus = newStatus;
	}
	
	@Override
	public ContentState getPreviousStatus() {
		return previousStatus;
	}
	
	public void setPreviousStatus(ContentState previousStatus) {
		this.previousStatus = previousStatus;
	}
	
	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
	public void setTransUnitId(TransUnitId transUnitId) {
		this.transUnitId = transUnitId;
	}
}
