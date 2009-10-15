package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class GetTransUnits implements Action<GetTransUnitsResult>, IsSerializable {

	private static final long serialVersionUID = -6560254574690557950L;

	private int offset;
	private int count;
	private DocumentId documentId;

	private GetTransUnits(){
	}
	
	public GetTransUnits(DocumentId id, int offset, int count) {
		this.documentId = id;
		this.offset = offset;
		this.count = count;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(DocumentId documentId) {
		this.documentId = documentId;
	}
	
}
