package org.fedorahosted.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.webtrans.client.Application;
import org.fedorahosted.flies.webtrans.shared.DispatchService;
import org.fedorahosted.flies.webtrans.shared.DispatchServiceAsync;
import org.fedorahosted.flies.webtrans.shared.auth.AuthenticationError;
import org.fedorahosted.flies.webtrans.shared.auth.AuthorizationError;
import org.fedorahosted.flies.webtrans.shared.auth.Identity;
import org.fedorahosted.flies.webtrans.shared.model.WorkspaceContext;
import org.fedorahosted.flies.webtrans.shared.rpc.WrappedAction;
import org.fedorahosted.flies.webtrans.shared.rpc.AbstractWorkspaceAction;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.inject.Inject;

public class SeamDispatchAsync implements CachingDispatchAsync {

	private static final DispatchServiceAsync realService;
	
	static{
		realService = GWT.create(DispatchService.class);
	}
	
	protected WorkspaceContext workspaceContext;
	protected Identity identity;
	
	@Inject
	public SeamDispatchAsync() {
		final String endpointURL = Application.getModuleParentBaseUrl() + "seam/resource/gwt";

		((ServiceDefTarget) realService).setServiceEntryPoint(endpointURL);
	}
	
	public <A extends Action<R>, R extends Result> void execute(final A action,
			final AsyncCallback<R> callback) {
		if( action instanceof AbstractWorkspaceAction<?> ) {
			AbstractWorkspaceAction<?> wsAction = (AbstractWorkspaceAction<?>) action;
			if(workspaceContext == null || identity == null) {
				callback.onFailure( new AuthorizationError("Dispatcher not set up to delegate WorkspaceContext and Identity") );
				return;
			}
			wsAction.setSessionId(identity.getSessionId());
			wsAction.setWorkspaceId(workspaceContext.getWorkspaceId());
		}
		
		String sessionId = Cookies.getCookie("JSESSIONID");
		realService.execute(new WrappedAction<R>(action, sessionId), new AsyncCallback<Result>() {

			public void onFailure(final Throwable caught) {
				if(caught instanceof AuthenticationError) {
					Application.redirectToLogin();
				}
				else if(caught instanceof AuthorizationError){
					Log.info("RCP Authorization Error calling " + action.getClass() + ": " + caught.getMessage());
					callback.onFailure(caught);
				} 
				else {
					callback.onFailure(caught);
				}
			}

			@SuppressWarnings("unchecked")
			public void onSuccess(final Result result) {
				callback.onSuccess((R) result);
			}
		});
	}
	
	@Override
	public void setWorkspaceContext(WorkspaceContext workspaceContext) {
		this.workspaceContext = workspaceContext;
	}
	
	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	
	
}
