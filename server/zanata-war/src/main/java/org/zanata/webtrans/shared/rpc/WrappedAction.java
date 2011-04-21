package org.zanata.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WrappedAction<R extends Result> implements Action<R>, IsSerializable
{
   private Action<R> action;
   private String csrfToken;

   public WrappedAction()
   {
   }

   public WrappedAction(Action<R> action, String csrfToken)
   {
      this.action = action;
      this.csrfToken = csrfToken;
   }

   public Action<R> getAction()
   {
      return action;
   }

   public String getCsrfToken()
   {
      return csrfToken;
   }

}
