package org.fedorahosted.flies.webtrans.client.rpc;



import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.EnsureLoggedInAction;
import org.fedorahosted.flies.gwt.rpc.EnsureLoggedInResult;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.gwt.rpc.GetTransUnits;
import org.fedorahosted.flies.gwt.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.gwt.rpc.GetTranslatorList;
import org.fedorahosted.flies.gwt.rpc.GetTranslatorListResult;

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
		} else if (action instanceof EnsureLoggedInAction) {
			final EnsureLoggedInAction _action = (EnsureLoggedInAction) action;
			AsyncCallback<EnsureLoggedInResult> _callback = (AsyncCallback<EnsureLoggedInResult>) callback;
			DeferredCommand.addCommand(new DummyEnsureLoggedInCommand(_action, _callback));
		} else if (action instanceof GetTranslatorList) {
			final GetTranslatorList _action = (GetTranslatorList) action;
			AsyncCallback<GetTranslatorListResult> _callback = (AsyncCallback<GetTranslatorListResult>) callback;
			DeferredCommand.addCommand(new DummyGetTranslatorListCommand(_action, _callback));
		} else if (action instanceof GetProjectStatusCount) {
			final GetProjectStatusCount _action = (GetProjectStatusCount) action;
			AsyncCallback<GetProjectStatusCountResult> _callback = (AsyncCallback<GetProjectStatusCountResult>) callback;
			DeferredCommand.addCommand(new DummyGetProjectStatusCountCommand(_action, _callback));
		} else if (action instanceof GetStatusCount) {
			final GetStatusCount _action = (GetStatusCount) action;
			AsyncCallback<GetStatusCountResult> _callback = (AsyncCallback<GetStatusCountResult>) callback;
			DeferredCommand.addCommand(new DummyGetStatusCountCommand(_action, _callback));
		} else if (action instanceof GetTranslationMemory) {
			final GetTranslationMemory _action = (GetTranslationMemory) action;
			AsyncCallback<GetTranslationMemoryResult> _callback = (AsyncCallback<GetTranslationMemoryResult>) callback;
			DeferredCommand.addCommand(new DummyGetTranslationMemoryCommand(_action, _callback));
		} else {
			Log.warn("DummyDispatchAsync: ignoring action of "+action.getClass());
//			callback.onFailure(new RuntimeException());
		}
	}

}
