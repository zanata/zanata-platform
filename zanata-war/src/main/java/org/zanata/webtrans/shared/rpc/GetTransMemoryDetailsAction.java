package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import com.google.common.collect.Lists;

public class GetTransMemoryDetailsAction extends AbstractWorkspaceAction<TransMemoryDetailsList>
{

   private static final long serialVersionUID = 1L;
   private ArrayList<Long> transUnitIdList;

   @SuppressWarnings("unused")
   private GetTransMemoryDetailsAction()
   {
   }

   public GetTransMemoryDetailsAction(ArrayList<Long> transUnitIdList)
   {
      this.transUnitIdList = transUnitIdList;
   }

   public GetTransMemoryDetailsAction(Long... ids)
   {
      this.transUnitIdList = Lists.newArrayList(ids);
   }

   public ArrayList<Long> getTransUnitIdList()
   {
      return transUnitIdList;
   }

}
