package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

public class UpdateTransUnitResult implements Result
{

   private static final long serialVersionUID = 1L;

   // FIXME replace these with list of result object
   private List<Boolean> success;
   private List<Integer> currentVersionNum;


   private UpdateTransUnitResult()
   {
      success = new ArrayList<Boolean>();
      currentVersionNum = new ArrayList<Integer>();
   }

   // FIXME this should not be needed after UpdateTransUnitHandler is updated for multiple TUs
   public UpdateTransUnitResult(boolean success, int currentVersionNum)
   {
      this();
      addUpdateResult(success, currentVersionNum);
   }

   public void addUpdateResult(boolean success, int currentVersionNum)
   {
      this.success.add(success);
      this.currentVersionNum.add(currentVersionNum);
   }

   public List<Integer> getVersionNums()
   {
      return currentVersionNum;
   }

   public Integer getSingleVersionNum()
   {
      if (currentVersionNum.size() == 1)
      {
         return currentVersionNum.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when checking results for a single TransUnit update");
      }
   }

   public List<Boolean> getSuccess()
   {
      return success;
   }

   public boolean isSingleSuccess()
   {
      if (success.size() == 1)
      {
         return success.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when checking results for a single TransUnit update");
      }
   }

}
