package org.zanata.webtrans.shared;

import net.customware.gwt.dispatch.shared.ActionException;

public class NoSuchWorkspaceException extends ActionException
{

   public NoSuchWorkspaceException()
   {
      super();
   }

   public NoSuchWorkspaceException(String message)
   {
      super(message);
   }

}
