package org.fedorahosted.flies.webtrans.client.rpc;

import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.EnsureLoggedInAction;
import org.fedorahosted.flies.gwt.rpc.EnsureLoggedInResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyEnsureLoggedInCommand implements Command {

//	private final EnsureLoggedInAction action;
	private final AsyncCallback<EnsureLoggedInResult> callback;

	public DummyEnsureLoggedInCommand(EnsureLoggedInAction action,
			AsyncCallback<EnsureLoggedInResult> callback) {
//				this.action = action;
				this.callback = callback;
	}

	@Override
	public void execute() {
		callback.onSuccess(new EnsureLoggedInResult(
				new SessionId("sessionId"), 
				new Person(new PersonId("personId"), "Dummy User")));
	}

}
