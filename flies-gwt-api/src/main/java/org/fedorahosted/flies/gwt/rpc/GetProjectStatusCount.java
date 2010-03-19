package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetProjectStatusCount implements WorkspaceAction<GetProjectStatusCountResult>{
	
	private static final long serialVersionUID = -143162507696820592L;

	private WorkspaceId workspaceId;

	@SuppressWarnings("unused")
	private GetProjectStatusCount(){
	}
	
	public GetProjectStatusCount(WorkspaceId workspaceId) {
		this.workspaceId = workspaceId;
	}

	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}

}
