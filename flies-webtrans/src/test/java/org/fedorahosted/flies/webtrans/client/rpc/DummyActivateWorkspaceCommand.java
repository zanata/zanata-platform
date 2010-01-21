package org.fedorahosted.flies.webtrans.client.rpc;

import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
		callback.onSuccess(
				new ActivateWorkspaceResult("DummyWorkspace"+action.getProjectContainerId().getId(), "zz"));
	}

}
