package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class TransUnitEditing implements SessionEventData, HasTransUnitEditData {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private DocumentId documentId;
	private EditState editStatus;
	
	@SuppressWarnings("unused")
	private TransUnitEditing() {
	}
	
	public TransUnitEditing(DocumentId documentId, TransUnitId transUnitId, EditState editStatus) {
		this.documentId = documentId;
		this.transUnitId = transUnitId;
		this.editStatus = editStatus;
	}

	@Override
	public DocumentId getDocumentId() {
		return documentId;
	}
	
	@Override
	public EditState getEditStatus() {
		return editStatus;
	}

	@Override
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
}