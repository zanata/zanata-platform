package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class TransUnitUpdated implements SessionEventData {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private DocumentId documentId;
	private TransUnitStatus previousStatus;
	private TransUnitStatus newStatus;
	private int sequence;
	
	@SuppressWarnings("unused")
	private TransUnitUpdated() {
	}
	
	public TransUnitUpdated(DocumentId documentId, TransUnitId transUnitId, TransUnitStatus previousStatus, TransUnitStatus newStatus) {
		this.documentId = documentId;
		this.transUnitId = transUnitId;
		this.previousStatus = previousStatus;
		this.newStatus = newStatus;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	public TransUnitStatus getNewStatus() {
		return newStatus;
	}
	
	public TransUnitStatus getPreviousStatus() {
		return previousStatus;
	}
	
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
}
