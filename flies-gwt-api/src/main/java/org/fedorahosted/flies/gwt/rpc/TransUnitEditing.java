package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class TransUnitEditing implements SessionEventData, HasTransUnitEditData {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private DocumentId documentId;
	private String sessionId;
	
	@SuppressWarnings("unused")
	private TransUnitEditing() {
	}
	
	public TransUnitEditing(DocumentId documentId, TransUnitId transUnitId, String sessionId) {
		this.documentId = documentId;
		this.transUnitId = transUnitId;
		this.sessionId = sessionId;
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
}