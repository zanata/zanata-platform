package org.fedorahosted.flies.gwt.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class AuthenticateAction implements Action<AuthenticateResult> {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	
	@SuppressWarnings("unused")
	private AuthenticateAction() {
	}
	
	public AuthenticateAction(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}

}
