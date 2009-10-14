package org.fedorahosted.flies.webtrans.client.rpc;

import org.fedorahosted.flies.gwt.common.DispatchService;
import org.fedorahosted.flies.gwt.common.DispatchServiceAsync;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class SeamDispatchAsync implements DispatchAsync {

	private static final DispatchServiceAsync realService;

	static {
		realService = GWT.create(DispatchService.class);
		final String endpointURL = "/flies/seam/resource/gwt";

		((ServiceDefTarget) realService).setServiceEntryPoint(endpointURL);
	}

	public static String getModuleBaseURL() {
		// Make sure that communication is with the server that served the
		// containing
		// web page and not where the GWT resources came from (which is the case
		// with
		// GWT.getHostPageBaseURL)
		final String url = GWT.getModuleBaseURL();//GWT.getHostPageBaseURL();

		return url;
	}

	public SeamDispatchAsync() {
	}

	public <A extends Action<R>, R extends Result> void execute(final A action,
			final AsyncCallback<R> callback) {
		realService.execute(action, new AsyncCallback<Result>() {

			public void onFailure(final Throwable caught) {
				callback.onFailure(caught);
			}

			@SuppressWarnings("unchecked")
			public void onSuccess(final Result result) {
				callback.onSuccess((R) result);
			}
		});
	}
}
