package org.fedorahosted.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;

public class GetTransMemoryDetailsAction extends AbstractWorkspaceAction<TransMemoryDetailsList>
{

   private ArrayList<Long> transUnitIdList;

   @SuppressWarnings("unused")
   private GetTransMemoryDetailsAction()
   {
      this(null);
   }

   public GetTransMemoryDetailsAction(ArrayList<Long> transUnitIdList)
   {
      this.transUnitIdList = transUnitIdList;
   }

   public ArrayList<Long> getTransUnitIdList()
   {
      return transUnitIdList;
   }

}
