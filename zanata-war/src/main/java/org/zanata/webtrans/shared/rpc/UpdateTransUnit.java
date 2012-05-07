package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

import java.util.ArrayList;
import java.util.List;


public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult>
{
   private static final long serialVersionUID = 1L;

   private List<TransUnitUpdateRequest> updateRequests;

   protected UpdateTransUnit()
   {
      updateRequests = new ArrayList<TransUnitUpdateRequest>();
   }

   public UpdateTransUnit(TransUnitUpdateRequest updateRequest)
   {
      this();
      updateRequests.add(updateRequest);
   }

   public void addTransUnit(TransUnitUpdateRequest updateRequest)
   {
      this.updateRequests.add(updateRequest);
   }

   public List<TransUnitUpdateRequest> getUpdateRequests()
   {
      return updateRequests;
   }

//   public TransUnitUpdateRequest getSingleUpdateRequest()
//   {
//      if (updateRequests.size() != 1)
//      {
//         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
//      }
//      return updateRequests.get(0);
//   }
}
