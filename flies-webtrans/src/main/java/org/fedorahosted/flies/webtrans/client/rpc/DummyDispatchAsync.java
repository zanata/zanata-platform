package org.fedorahosted.flies.webtrans.client.rpc;


import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyDispatchAsync extends SeamDispatchAsync {
	public DummyDispatchAsync() {
		Log.info("DummyDispatchAsync()");
	}

	@Override
	public <A extends Action<R>, R extends Result> void execute(A action,
			AsyncCallback<R> callback) {

	}

}
