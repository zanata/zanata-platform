package org.fedorahosted.flies.gwt.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class AuthenticateResult implements Result, IsSerializable {

	private static final long serialVersionUID = 1L;
	
	private String sessionId;
	
	public static final AuthenticateResult FAILED = new AuthenticateResult(); 
	
	private AuthenticateResult() {
	}

	public AuthenticateResult(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public boolean isSuccess() {
		return sessionId != null;
	}
	
	public String getSessionId() {
		return sessionId;
	}

}
