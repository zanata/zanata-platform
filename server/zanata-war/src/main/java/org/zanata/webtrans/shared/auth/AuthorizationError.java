package org.zanata.webtrans.shared.auth;

import net.customware.gwt.dispatch.shared.ActionException;

public class AuthorizationError extends ActionException
{

   private static final long serialVersionUID = 1L;

   public AuthorizationError()
   {
   }

   public AuthorizationError(String message)
   {
      super(message);
   }

}
