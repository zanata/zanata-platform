package org.fedorahosted.flies.webtrans.client.rpc;



import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyDispatchAsync extends SeamDispatchAsync {
	public DummyDispatchAsync() {
		Log.info("DummyDispatchAsync()");
	}

	@Override
	public <A extends Action<R>, R extends Result> void execute(A action,
			AsyncCallback<R> callback) {
		if (action instanceof GetTransUnits) {
			GetTransUnits gtuAction = (GetTransUnits) action;
			AsyncCallback<GetTransUnitsResult> gtuCallback = (AsyncCallback<GetTransUnitsResult>) callback;
			DeferredCommand.addCommand(new DummyGetTransUnitCommand(gtuAction, gtuCallback));
		} else if (action instanceof GetDocsList) {
			final GetDocsList gdlAction = (GetDocsList) action;
			AsyncCallback<GetDocsListResult> gdlCallback = (AsyncCallback<GetDocsListResult>) callback;
			DeferredCommand.addCommand(new DummyGetDocsListCommand(gdlAction, gdlCallback));
		} else if (action instanceof ActivateWorkspaceAction) {
			final ActivateWorkspaceAction gwcAction = (ActivateWorkspaceAction) action;
			AsyncCallback<ActivateWorkspaceResult> gwcCallback = (AsyncCallback<ActivateWorkspaceResult>) callback;
			DeferredCommand.addCommand(new DummyActivateWorkspaceCommand(gwcAction, gwcCallback));
		} else {
			Log.info("DummyDispatchAsync: ignoring action of "+action.getClass());
//			callback.onFailure(new RuntimeException());
		}
	}

}
