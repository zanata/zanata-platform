package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.DocumentId;

public class GetTransUnitsStates extends AbstractWorkspaceAction<GetTransUnitsStatesResult> {

	private static final long serialVersionUID = 1L;

	private int offset;
	private int count;
	private DocumentId documentId;
	private ContentState state;
	private boolean reverse;

	@SuppressWarnings("unused")
	private GetTransUnitsStates(){
	}
	
	public GetTransUnitsStates(DocumentId id, int offset, int count, boolean reverse, ContentState state) {
		this.documentId = id;
		this.offset = offset;
		this.count = count;
		this.state = state;
		this.setReverse(reverse);
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

	public void setState(ContentState state) {
		this.state = state;
	}

	public ContentState getState() {
		return state;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public boolean isReverse() {
		return reverse;
	}
}