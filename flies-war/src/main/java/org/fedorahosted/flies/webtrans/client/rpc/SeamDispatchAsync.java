package org.fedorahosted.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.common.DispatchService;
import org.fedorahosted.flies.gwt.common.DispatchServiceAsync;
import org.fedorahosted.flies.webtrans.client.Application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.inject.Inject;

public class SeamDispatchAsync implements CachingDispatchAsync {

	private static final DispatchServiceAsync realService;
	
	static {
		realService = GWT.create(DispatchService.class);
		final String endpointURL = Application.FLIES_BASE_PATH + "seam/resource/gwt";

		((ServiceDefTarget) realService).setServiceEntryPoint(endpointURL);
	}

	private ErrorHandler errorHandler;
	
	@Inject
	public SeamDispatchAsync() {
	}

	public <A extends Action<R>, R extends Result> void execute(final A action,
			final AsyncCallback<R> callback) {
		realService.execute(action, new AsyncCallback<Result>() {

			public void onFailure(final Throwable caught) {
				handleError(action, callback, caught);
			}

			@SuppressWarnings("unchecked")
			public void onSuccess(final Result result) {
				callback.onSuccess((R) result);
			}
		});
	}
	
	private <A extends Action<R>, R extends Result> void handleError(final A action,
			final AsyncCallback<R> callback, final Throwable caught) {

//		if(errorHandler != null){
//			errorHandler.onFailure(action, callback, caught);
//		}
//		else{
//			callback.onFailure(caught);
//		}
		callback.onFailure(caught);

	}
	
	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
}
