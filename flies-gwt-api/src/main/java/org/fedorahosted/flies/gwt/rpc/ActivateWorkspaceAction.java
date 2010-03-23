package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.common.WorkspaceId;

public class ActivateWorkspaceAction implements DispatchAction<ActivateWorkspaceResult> {
	
	private static final long serialVersionUID = 1L;

	private WorkspaceId workspaceId;
	
	@SuppressWarnings("unused")
	private ActivateWorkspaceAction() {
	}

	public ActivateWorkspaceAction(WorkspaceId workspaceId) {
		this.workspaceId = workspaceId;
	}

	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}
}
