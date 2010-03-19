package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class GetTransUnits implements WorkspaceAction<GetTransUnitsResult> {

	private static final long serialVersionUID = 1L;

	private WorkspaceId workspaceId;
	private int offset;
	private int count;
	private DocumentId documentId;

	@SuppressWarnings("unused")
	private GetTransUnits(){
	}
	
	public GetTransUnits(WorkspaceId workspaceId, DocumentId id, int offset, int count) {
		this.workspaceId = workspaceId;
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

	@Override
	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}
}
