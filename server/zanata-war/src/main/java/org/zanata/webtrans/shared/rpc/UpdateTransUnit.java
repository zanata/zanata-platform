package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;

import java.util.ArrayList;


public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult>
{
   private static final long serialVersionUID = 1L;

   private TransUnitId transUnitId;
   private ArrayList<String> contents;
   private ContentState contentState;
   private boolean isRedo = false;
   private Integer verNum;

   public Integer getVerNum()
   {
      return verNum;
   }

   public void setVerNum(Integer verNum)
   {
      this.verNum = verNum;
   }

   public boolean isRedo()
   {
      return isRedo;
   }

   public void setRedo(boolean isRedo)
   {
      this.isRedo = isRedo;
   }

   @SuppressWarnings("unused")
   protected UpdateTransUnit()
   {
   }

   public UpdateTransUnit(TransUnitId transUnitId, ArrayList<String> contents, ContentState contentState)
   {
      this.transUnitId = transUnitId;
      this.contents = contents;
      this.contentState = contentState;
   }

   public ArrayList<String> getContents()
   {
      return contents;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public ContentState getContentState()
   {
      return contentState;
   }
}
