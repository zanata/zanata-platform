package org.fedorahosted.flies.webtrans.client.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetProjectStatusCountCommand implements Command {

	private final GetProjectStatusCount action;
	private final AsyncCallback<GetProjectStatusCountResult> callback;

	public DummyGetProjectStatusCountCommand(GetProjectStatusCount action,
			AsyncCallback<GetProjectStatusCountResult> callback) {
		this.action = action;
		this.callback = callback;
	}

	@Override
	public void execute() {
		callback.onSuccess(new GetProjectStatusCountResult(
				action.getProjectContainerId(), 
				new ArrayList<DocumentStatus>()));
	}

}
