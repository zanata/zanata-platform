package org.fedorahosted.flies.webtrans.client.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.TransUnitCount;
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
		TransUnitCount count = new TransUnitCount();
		count.set(ContentState.Approved, 34);
		count.set(ContentState.NeedReview, 23);
		count.set(ContentState.New, 43);
		callback.onSuccess(new GetStatusCountResult(
			action.getDocumentId(), count));
	}

}
