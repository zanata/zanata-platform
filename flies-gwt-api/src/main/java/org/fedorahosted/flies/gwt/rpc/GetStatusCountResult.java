package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.gwt.model.DocumentId;

public class GetStatusCountResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private DocumentId documentId;
	private TransUnitCount count;
	
	@SuppressWarnings("unused")
	private GetStatusCountResult()	{
	}
	
	public GetStatusCountResult(DocumentId documentId, TransUnitCount count) {
		this.documentId = documentId;
		this.count = count;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
	}

	public TransUnitCount getCount() {
		return count;
	}
	
}
