package org.zanata.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DelegatingDispatchAsync implements CachingDispatchAsync
{
   private CachingDispatchAsync delegate;

   public DelegatingDispatchAsync()
   {
      delegate = GWT.create(SeamDispatchAsync.class);
   }

   @Override
   public <A extends Action<R>, R extends Result> void execute(A action, AsyncCallback<R> callback)
   {
      delegate.execute(action, callback);
   }

   @Override
   public <A extends Action<R>, R extends Result> void rollback(A action, R result, AsyncCallback<Void> callback)
   {
      delegate.rollback(action, result, callback);
   }

   @Override
   public void setIdentity(Identity identity)
   {
      delegate.setIdentity(identity);

   }

   @Override
   public void setWorkspaceContext(WorkspaceContext workspaceContext)
   {
      delegate.setWorkspaceContext(workspaceContext);
   }

}
