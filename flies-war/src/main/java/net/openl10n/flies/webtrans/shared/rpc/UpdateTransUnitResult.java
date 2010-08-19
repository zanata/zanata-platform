package net.openl10n.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;

public class UpdateTransUnitResult implements Result
{

   private static final long serialVersionUID = 1L;

   private boolean success;

   @SuppressWarnings("unused")
   private UpdateTransUnitResult()
   {
   }

   public UpdateTransUnitResult(boolean success)
   {
      this.success = success;
   }

   public boolean isSuccess()
   {
      return success;
   }
}
