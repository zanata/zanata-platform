package org.fedorahosted.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ErrorHandler {
	<A extends Action<R>, R extends Result> void onFailure(final A action,
			final AsyncCallback<R> callback, final Throwable caught);
}
