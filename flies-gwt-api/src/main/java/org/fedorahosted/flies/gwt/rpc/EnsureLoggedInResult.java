package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class EnsureLoggedInResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private SessionId sessionId;
	private Person person;
	
	public static final EnsureLoggedInResult FAILED = new EnsureLoggedInResult(); 

	private EnsureLoggedInResult() {
	}

	public EnsureLoggedInResult(SessionId sessionId, Person person) {
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
