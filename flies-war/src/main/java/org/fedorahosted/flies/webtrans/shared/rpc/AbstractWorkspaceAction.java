package org.fedorahosted.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.webtrans.shared.auth.SessionId;
import org.fedorahosted.flies.webtrans.shared.common.WorkspaceId;

public abstract class AbstractWorkspaceAction<R extends Result> implements DispatchAction<R> {

	private static final long serialVersionUID = 1L;

	private WorkspaceId workspaceId;
	private SessionId sessionId;
	
	public final void setWorkspaceId(WorkspaceId workspaceId) {
		this.workspaceId = workspaceId;
	}
	
	public final void setSessionId(SessionId sessionId) {
		this.sessionId = sessionId;
	}
	
	public final WorkspaceId getWorkspaceId() {
		return workspaceId;
	}
	
	public final SessionId getSessionId() {
		return sessionId;
	}
	
}
