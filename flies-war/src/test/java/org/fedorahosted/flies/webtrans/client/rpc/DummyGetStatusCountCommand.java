package org.fedorahosted.flies.webtrans.client.rpc;

import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetStatusCountCommand implements Command {

	private final GetStatusCount action;
	private final AsyncCallback<GetStatusCountResult> callback;

	public DummyGetStatusCountCommand(GetStatusCount action,
			AsyncCallback<GetStatusCountResult> callback) {
		this.action = action;
		this.callback = callback;
	}

	@Override
	public void execute() {
		callback.onSuccess(new GetStatusCountResult(
			action.getDocumentId(), 5, 6, 7));
	}

}
