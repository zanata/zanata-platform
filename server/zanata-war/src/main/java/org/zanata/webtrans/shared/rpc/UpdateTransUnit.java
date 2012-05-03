package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

import java.util.ArrayList;
import java.util.List;


public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult>
{
   private static final long serialVersionUID = 1L;

   private List<TransUnitUpdateRequest> updateRequests;

//   private boolean isRedo = false;
//
//   public boolean isRedo()
//   {
//      return isRedo;
//   }
//
//   public void setRedo(boolean isRedo)
//   {
//      this.isRedo = isRedo;
//   }

   protected UpdateTransUnit()
   {
      updateRequests = new ArrayList<TransUnitUpdateRequest>();
   }

   public UpdateTransUnit(TransUnitUpdateRequest updateRequest)
   {
      this();
      updateRequests.add(updateRequest);
   }

   // FIXME pass a TransUnitUpdateRequest instead of multiple arguments
   public void addTransUnit(TransUnitId transUnitId, ArrayList<String> contents, ContentState contentState, int verNum)
   {
      this.updateRequests.add(new TransUnitUpdateRequest(transUnitId, contents, contentState, verNum));
   }

   public List<TransUnitUpdateRequest> getUpdateRequests()
   {
      return updateRequests;
   }

   // FIXME replace all these getters with an update request getter
   public TransUnitId getSingleTransUnitId()
   {
      return getSingleUpdateRequest().getTransUnitId();
   }

   public List<String> getSingleContents()
   {
      return getSingleUpdateRequest().getNewContents();
   }

   public ContentState getSingleContentState()
   {
      return getSingleUpdateRequest().getNewContentState();
   }

   public Integer getSingleVerNum()
   {
      return getSingleUpdateRequest().getBaseTranslationVersion();
   }

   public TransUnitUpdateRequest getSingleUpdateRequest()
   {
      if (updateRequests.size() != 1)
      {
         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
      }
      return updateRequests.get(0);
   }
}
