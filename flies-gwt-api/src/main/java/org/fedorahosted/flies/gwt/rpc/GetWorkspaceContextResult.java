package org.fedorahosted.flies.gwt.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class GetWorkspaceContextResult implements Result {

	private static final long serialVersionUID = 1L;

	private String workspaceName;
	private String localeName;
	
	@SuppressWarnings("unused")
	private GetWorkspaceContextResult() {
	}
	
	public GetWorkspaceContextResult(String workspaceName, String localeName) {
		this.workspaceName = workspaceName;
		this.localeName = localeName;
	}
	
	public String getLocaleName() {
		return localeName;
	}
	
	public String getWorkspaceName() {
		return workspaceName;
	}
	
}
