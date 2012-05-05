package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;

import net.customware.gwt.dispatch.shared.Result;

public class UpdateTransUnitResult implements Result
{

   private static final long serialVersionUID = 1L;

   private List<TransUnitUpdateInfo> tuUpdateInfo;


   public UpdateTransUnitResult()
   {
      tuUpdateInfo = new ArrayList<TransUnitUpdateInfo>();
   }

   public UpdateTransUnitResult(TransUnitUpdateInfo updateInfo)
   {
      this();
      addUpdateResult(updateInfo);
   }

   public void addUpdateResult(TransUnitUpdateInfo updateInfo)
   {
      tuUpdateInfo.add(updateInfo);
   }

   public List<TransUnitUpdateInfo> getUpdateInfoList()
   {
      return tuUpdateInfo;
   }

   public Integer getSingleVersionNum()
   {
      return getSingleUpdateInfo().getTransUnit().getVerNum();
   }

   public boolean isSingleSuccess()
   {
      return getSingleUpdateInfo().isSuccess();
   }

   private TransUnitUpdateInfo getSingleUpdateInfo()
   {
      if (tuUpdateInfo.size() == 1)
      {
         return tuUpdateInfo.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when checking results for a single TransUnit update");
      }
   }
}
