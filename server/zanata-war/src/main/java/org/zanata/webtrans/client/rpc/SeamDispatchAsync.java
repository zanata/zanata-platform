package org.zanata.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.RpcMessages;
import org.zanata.webtrans.client.util.JavascriptUtil;
import org.zanata.webtrans.shared.DispatchService;
import org.zanata.webtrans.shared.DispatchServiceAsync;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.auth.InvalidTokenError;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.WrappedAction;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.inject.Inject;

public class SeamDispatchAsync implements CachingDispatchAsync {

    private static final DispatchServiceAsync realService;

    static {
        realService = GWT.create(DispatchService.class);
    }

    protected UserWorkspaceContext userWorkspaceContext;
    protected Identity identity;

    private final RpcMessages messages;
    private EventBus eventBus;

    @Inject
    public SeamDispatchAsync() {
        this.messages = GWT.create(RpcMessages.class);
        final String endpointURL =
                Application.getModuleParentBaseUrl() + "seam/resource/gwt";

        ((ServiceDefTarget) realService).setServiceEntryPoint(endpointURL);
    }

    @Override
    public <A extends Action<R>, R extends Result> void execute(final A action,
            final AsyncCallback<R> callback) {
        // TODO deduplicate: see rollback()
        if (action instanceof AbstractWorkspaceAction<?>) {
            AbstractWorkspaceAction<?> wsAction =
                    (AbstractWorkspaceAction<?>) action;
            if (userWorkspaceContext == null || identity == null) {
                callback.onFailure(new AuthorizationError(messages
                        .dispatcherSetupFailed()));
                return;
            }
            wsAction.setEditorClientId(identity.getEditorClientId());
            wsAction.setWorkspaceId(userWorkspaceContext.getWorkspaceContext()
                    .getWorkspaceId());
        }

        final String sessionId = getSessionId();
        realService.execute(new WrappedAction<R>(action, sessionId),
                new AbstractAsyncCallback<Result>() {

                    public void onFailure(final Throwable caught) {
                        if (caught instanceof com.google.gwt.user.client.rpc.StatusCodeException
                                && ((StatusCodeException) caught)
                                        .getStatusCode() == 0) {
                            if (!(action instanceof ExitWorkspaceAction)) {
                                eventBus.fireEvent(new NotificationEvent(
                                        NotificationEvent.Severity.Error,
                                        messages.noResponseFromServer()));
                            }
                        }
                        if (caught instanceof AuthenticationError) {
                            Log.error("Authentication error.", caught);
                            Application.redirectToLogin();
                        } else if (caught instanceof InvalidTokenError) {
                            Log.error("Invalid Token error ("+ sessionId + ")", caught);
                            Application.redirectToLogin();
                        } else if (caught instanceof AuthorizationError) {
                            Log.info("RCP Authorization Error calling "
                                    + action.getClass() + ": "
                                    + caught.getMessage());
                            callback.onFailure(caught);
                        } else {
                            callback.onFailure(caught);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    public void onSuccess(final Result result) {
                        callback.onSuccess((R) result);
                    }
                });
    }

    private String getSessionId() {
        return JavascriptUtil.getJavascriptValue("zanataSessionId");
    }

    @Override
    public void setUserWorkspaceContext(
            UserWorkspaceContext userWorkspaceContext) {
        this.userWorkspaceContext = userWorkspaceContext;
    }

    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public <A extends Action<R>, R extends Result> void rollback(
            final A action, final R result, final AsyncCallback<Void> callback) {
        // TODO deduplicate: see execute()
        if (action instanceof AbstractWorkspaceAction<?>) {
            AbstractWorkspaceAction<?> wsAction =
                    (AbstractWorkspaceAction<?>) action;
            if (userWorkspaceContext == null || identity == null) {
                callback.onFailure(new AuthorizationError(messages
                        .dispatcherSetupFailed()));
                return;
            }
            wsAction.setEditorClientId(identity.getEditorClientId());
            wsAction.setWorkspaceId(userWorkspaceContext.getWorkspaceContext()
                    .getWorkspaceId());
        }

        String sessionId = getSessionId();
        realService.rollback(new WrappedAction<R>(action, sessionId), result,
                new AsyncCallback<Void>() {

                    public void onFailure(final Throwable caught) {
                        if (caught instanceof AuthenticationError) {
                            Log.error("Authentication error.");
                            Application.redirectToLogin();
                        } else if (caught instanceof AuthorizationError) {
                            Log.info("RCP Authorization Error calling "
                                    + action.getClass() + ": "
                                    + caught.getMessage());
                            callback.onFailure(caught);
                        } else {
                            callback.onFailure(caught);
                        }
                    }

                    @Override
                    public void onSuccess(Void result) {
                    }
                });
    }
}
