package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class TransUnitUpdated implements SessionEventData, HasTransUnitUpdatedData {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private DocumentId documentId;
	private TransUnitStatus previousStatus;
	private TransUnitStatus newStatus;
	
	@SuppressWarnings("unused")
	private TransUnitUpdated() {
	}
	
	public TransUnitUpdated(DocumentId documentId, TransUnitId transUnitId, TransUnitStatus previousStatus, TransUnitStatus newStatus) {
		this.documentId = documentId;
		this.transUnitId = transUnitId;
		this.previousStatus = previousStatus;
		this.newStatus = newStatus;
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	@Override
	public TransUnitStatus getNewStatus() {
		return newStatus;
	}
	
	@Override
	public TransUnitStatus getPreviousStatus() {
		return previousStatus;
	}
	
	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
}
