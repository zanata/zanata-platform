package org.fedorahosted.flies.webtrans.client.rpc;

import org.fedorahosted.flies.gwt.rpc.GetWorkspaceContext;
import org.fedorahosted.flies.gwt.rpc.GetWorkspaceContextResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetWorkspaceContextCommand implements Command {

	private final GetWorkspaceContext action;
	private final AsyncCallback<GetWorkspaceContextResult> callback;

	public DummyGetWorkspaceContextCommand(GetWorkspaceContext gwcAction,
			AsyncCallback<GetWorkspaceContextResult> gwcCallback) {
		this.action = gwcAction;
		this.callback = gwcCallback;
	}

	@Override
	public void execute() {
		callback.onSuccess(
				new GetWorkspaceContextResult("DummyWorkspace"+action.getProjectContainerId().getId(), "zz"));
	}

}
