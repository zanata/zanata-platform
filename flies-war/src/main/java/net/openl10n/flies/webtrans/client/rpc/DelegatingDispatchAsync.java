package net.openl10n.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.webtrans.shared.auth.Identity;
import net.openl10n.flies.webtrans.shared.model.WorkspaceContext;


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
