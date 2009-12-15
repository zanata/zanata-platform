package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class TransUnitEditing implements SessionEventData, HasTransUnitEditData {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private DocumentId documentId;
	private EditState preStatus;
	private EditState curStatus;
	
	@SuppressWarnings("unused")
	private TransUnitEditing() {
	}
	
	public TransUnitEditing(DocumentId documentId, TransUnitId transUnitId, EditState preStatus, EditState curStatus) {
		this.documentId = documentId;
		this.transUnitId = transUnitId;
		this.preStatus = preStatus;
		this.curStatus = curStatus;
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	@Override
	public EditState getPreStatus() {
		return preStatus;
	}
	
	@Override
	public EditState getCurStatus() {
		return curStatus;
	}

	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
}