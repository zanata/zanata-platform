package org.zanata.webtrans.client.rpc;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CachingDispatchAsync extends DispatchAsync
{
   void setUserWorkspaceContext(UserWorkspaceContext userWorkspaceContext);
   
   void setIdentity(Identity identity);

   <A extends Action<R>, R extends Result> void rollback(A action, R result, AsyncCallback<Void> callback);

}
