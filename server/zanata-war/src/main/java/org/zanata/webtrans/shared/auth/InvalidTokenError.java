package org.zanata.webtrans.shared.auth;

import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Indicates an attempt to execute an action with a csrf token that does not
 * match the current HTTP session's csrf token.
 * 
 * @author damason@redhat.com
 * 
 */
public class InvalidTokenError extends ActionException
{

   private static final long serialVersionUID = 1L;

   public InvalidTokenError()
   {
   }

   public InvalidTokenError(String message)
   {
      super(message);
   }

}
