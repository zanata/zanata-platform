package org.zanata.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;

public class UpdateTransUnitResult implements Result
{

   private static final long serialVersionUID = 1L;

   private boolean success;

   private boolean saved;

   private Integer currentVersionNum;

   public Integer getCurrentVersionNum()
   {
      return currentVersionNum;
   }

   public void setCurrentVersionNum(Integer currentVersionNum)
   {
      this.currentVersionNum = currentVersionNum;
   }

   private UpdateTransUnit previous;

   public UpdateTransUnit getPrevious()
   {
      return previous;
   }

   public void setPrevious(UpdateTransUnit previous)
   {
      this.previous = previous;
   }

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

   public void setSaved(boolean var)
   {
      this.saved = var;
   }

   public boolean isSaved()
   {
      return this.saved;
   }
}
