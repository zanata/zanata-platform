package org.fedorahosted.flies.webtrans.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

public class DelegatingDispatchAsync implements DispatchAsync {
	private DispatchAsync delegate;

	public DelegatingDispatchAsync() {
		delegate = GWT.create(SeamDispatchAsync.class);
	}

	@Override
	public <A extends Action<R>, R extends Result> void execute(A action,
			AsyncCallback<R> callback) {
		delegate.execute(action, callback);
	}

}
