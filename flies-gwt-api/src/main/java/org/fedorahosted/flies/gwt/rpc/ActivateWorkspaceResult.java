package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.auth.Identity;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;

public class ActivateWorkspaceResult implements Result {

	private static final long serialVersionUID = 1L;

	private WorkspaceContext workspaceContext;
	private Identity identity;
	
	@SuppressWarnings("unused")
	private ActivateWorkspaceResult() {
	}
	
	public ActivateWorkspaceResult(WorkspaceContext workspaceContext, Identity identity) {
		this.workspaceContext = workspaceContext;
		this.identity = identity;
	}

	public WorkspaceContext getWorkspaceContext() {
		return workspaceContext;
	}
	
	public Identity getIdentity() {
		return identity;
	}
}
