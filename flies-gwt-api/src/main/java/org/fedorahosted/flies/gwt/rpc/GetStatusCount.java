package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetStatusCount implements WorkspaceAction<GetStatusCountResult> {

	private static final long serialVersionUID = -1218943735746130251L;

	private DocumentId documentId;
	private WorkspaceId workspaceId;
	
	@SuppressWarnings("unused")
	private GetStatusCount(){
	}
	
	public GetStatusCount(DocumentId id, WorkspaceId workspaceId) {
		this.documentId = id;
		this.workspaceId = workspaceId;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}

}
