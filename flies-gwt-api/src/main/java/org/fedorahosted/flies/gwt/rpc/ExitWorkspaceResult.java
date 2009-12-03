package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

public class ExitWorkspaceResult implements Result{

	private static final long serialVersionUID = 1L;

	private String userName;
	
	@SuppressWarnings("unused")
	private ExitWorkspaceResult() {
	}
	
	public ExitWorkspaceResult(String userName) {
		this.userName = userName;
	}
	
	public String getuserName() {
		return userName;
	}
}
