package net.openl10n.flies.webtrans.shared;

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
