package net.openl10n.flies.webtrans.shared.rpc;

import java.util.ArrayList;

public class GetTransMemoryDetailsAction extends AbstractWorkspaceAction<TransMemoryDetailsList>
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
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
