package org.fedorahosted.flies.gwt.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class GetTransUnits implements Action<GotTransUnits>, IsSerializable {

	private static final long serialVersionUID = -6560254574690557950L;

	private int offset;
	private int count;
	private int documentId;

	private GetTransUnits(){
	}
	
	public GetTransUnits(int documentId, int offset, int count) {
		this.documentId = documentId;
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

	public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	
}
