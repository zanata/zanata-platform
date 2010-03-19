package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.Collections;

import org.fedorahosted.flies.gwt.auth.Identity;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;
import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.novanic.eventservice.client.event.domain.DomainFactory;

public class DummyActivateWorkspaceCommand implements Command {

	private final ActivateWorkspaceAction action;
	private final AsyncCallback<ActivateWorkspaceResult> callback;

	public DummyActivateWorkspaceCommand(ActivateWorkspaceAction gwcAction,
			AsyncCallback<ActivateWorkspaceResult> gwcCallback) {
		this.action = gwcAction;
		this.callback = gwcCallback;
	}

	@Override
	public void execute() {
		WorkspaceContext context = 
			new WorkspaceContext(action.getWorkspaceId(), "Dummy Workspace", "Mock Sweedish", DomainFactory.getDomain("Dummy"));
		
		Identity identity = new Identity(
				new SessionId("123456"), 
				new Person( new PersonId("bob"), "Bob The Builder"), 
				Collections.EMPTY_SET,
				Collections.EMPTY_SET);
		callback.onSuccess(
				new ActivateWorkspaceResult(context, identity));
	}

}
