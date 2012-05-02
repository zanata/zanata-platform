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

   private boolean isRedo = false;

   public boolean isRedo()
   {
      return isRedo;
   }

   public void setRedo(boolean isRedo)
   {
      this.isRedo = isRedo;
   }

   protected UpdateTransUnit()
   {
      updateRequests = new ArrayList<TransUnitUpdateRequest>();
   }

   public UpdateTransUnit(TransUnitUpdateRequest updateRequest)
   {
      this();
      updateRequests.add(updateRequest);
   }

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
      checkForSingleUpdateRequest();
      return updateRequests.get(0).getTransUnitId();
   }

   public List<String> getSingleContents()
   {
      checkForSingleUpdateRequest();
      return updateRequests.get(0).getNewContents();
   }

   public ContentState getSingleContentState()
   {
      checkForSingleUpdateRequest();
      return updateRequests.get(0).getNewContentState();
   }

   public Integer getSingleVerNum()
   {
      checkForSingleUpdateRequest();
      return updateRequests.get(0).getBaseTranslationVersion();
   }

   private void checkForSingleUpdateRequest()
   {
      if (updateRequests.size() != 1)
      {
         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
      }
   }
}
