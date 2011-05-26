package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

public interface CachingDispatchAsync extends DispatchAsync
{

   void setWorkspaceContext(WorkspaceContext workspaceContext);

   void setIdentity(Identity identity);

   <A extends Action<R>, R extends Result> void rollback(A action, R result, AsyncCallback<Void> callback);
}
