package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;


public class GetGlossaryDetailsAction extends AbstractWorkspaceAction<GetGlossaryDetailsResult>
{
   private static final long serialVersionUID = 1L;
   private ArrayList<Long> sourceIdList;

   @SuppressWarnings("unused")
   private GetGlossaryDetailsAction()
   {
      this(null);
   }

   public GetGlossaryDetailsAction(ArrayList<Long> sourceIdList)
   {
      this.sourceIdList = sourceIdList;
   }

   public ArrayList<Long> getSourceIdList()
   {
      return sourceIdList;
   }
}
