package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class AuthenticateResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private SessionId sessionId;
	private Person person;
	
	public static final AuthenticateResult FAILED = new AuthenticateResult(); 

	private AuthenticateResult() {
	}

	public AuthenticateResult(SessionId sessionId, Person person) {
		this.sessionId = sessionId;
		this.person = person;
	}
	
	public boolean isSuccess() {
		return sessionId != null;
	}
	
	public SessionId getSessionId() {
		return sessionId;
	}
	
	public Person getPerson() {
		return person;
	}

}
