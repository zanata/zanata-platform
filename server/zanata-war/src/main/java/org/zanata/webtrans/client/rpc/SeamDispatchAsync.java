package org.zanata.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.shared.DispatchService;
import org.zanata.webtrans.shared.DispatchServiceAsync;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.auth.InvalidTokenError;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;
import org.zanata.webtrans.shared.rpc.WrappedAction;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.inject.Inject;

public class SeamDispatchAsync implements CachingDispatchAsync
{

   private static final DispatchServiceAsync realService;

   static
   {
      realService = GWT.create(DispatchService.class);
   }

   protected WorkspaceContext workspaceContext;
   protected Identity identity;

   private final RpcMessages messages;

   @Inject
   public SeamDispatchAsync()
   {
      this.messages = GWT.create(RpcMessages.class);
      final String endpointURL = Application.getModuleParentBaseUrl() + "seam/resource/gwt";

      ((ServiceDefTarget) realService).setServiceEntryPoint(endpointURL);
   }

   public <A extends Action<R>, R extends Result> void execute(final A action, final AsyncCallback<R> callback)
   {
      if (action instanceof AbstractWorkspaceAction<?>)
      {
         AbstractWorkspaceAction<?> wsAction = (AbstractWorkspaceAction<?>) action;
         if (workspaceContext == null || identity == null)
         {
            callback.onFailure(new AuthorizationError(messages.dispatcherSetupFailed()));
            return;
         }
         wsAction.setSessionId(identity.getSessionId());
         wsAction.setWorkspaceId(workspaceContext.getWorkspaceId());
      }

      String sessionId = Cookies.getCookie("JSESSIONID");
      realService.execute(new WrappedAction<R>(action, sessionId), new AsyncCallback<Result>()
      {

         public void onFailure(final Throwable caught)
         {
            if (caught instanceof AuthenticationError || caught instanceof InvalidTokenError)
            {
               Application.redirectToLogin();
            }
            else if (caught instanceof AuthorizationError)
            {
               Log.info("RCP Authorization Error calling " + action.getClass() + ": " + caught.getMessage());
               callback.onFailure(caught);
            }
            else
            {
               callback.onFailure(caught);
            }
         }

         @SuppressWarnings("unchecked")
         public void onSuccess(final Result result)
         {
            callback.onSuccess((R) result);
         }
      });
   }

   @Override
   public void setWorkspaceContext(WorkspaceContext workspaceContext)
   {
      this.workspaceContext = workspaceContext;
   }

   @Override
   public void setIdentity(Identity identity)
   {
      this.identity = identity;
   }

   @Override
   public <A extends Action<R>, R extends Result> void rollback(final A action, final R result, final AsyncCallback<Void> callback)
   {
      if (action instanceof AbstractWorkspaceAction<?>)
      {
         AbstractWorkspaceAction<?> wsAction = (AbstractWorkspaceAction<?>) action;
         if (workspaceContext == null || identity == null)
         {
            callback.onFailure(new AuthorizationError(messages.dispatcherSetupFailed()));
            return;
         }
         wsAction.setSessionId(identity.getSessionId());
         wsAction.setWorkspaceId(workspaceContext.getWorkspaceId());
      }

      String sessionId = Cookies.getCookie("JSESSIONID");
      realService.rollback(new WrappedAction<R>(action, sessionId), result, new AsyncCallback<Void>()
      {

         public void onFailure(final Throwable caught)
         {
            if (caught instanceof AuthenticationError)
            {
               Application.redirectToLogin();
            }
            else if (caught instanceof AuthorizationError)
            {
               Log.info("RCP Authorization Error calling " + action.getClass() + ": " + caught.getMessage());
               callback.onFailure(caught);
            }
            else
            {
               callback.onFailure(caught);
            }
         }

         @Override
         public void onSuccess(Void result)
         {
         }
      });
   }

}
