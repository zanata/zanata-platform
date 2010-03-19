package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

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
