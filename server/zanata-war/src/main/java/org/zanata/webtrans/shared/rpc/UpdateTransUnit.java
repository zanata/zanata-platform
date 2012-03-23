package org.zanata.webtrans.shared.rpc;

import com.google.common.collect.Lists;
import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;

import java.util.List;


public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult>
{
   private static final long serialVersionUID = 1L;

   private TransUnitId transUnitId;
   private List<String> contents;
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
   private UpdateTransUnit()
   {
   }

   public UpdateTransUnit(TransUnitId transUnitId, List<String> contents, ContentState contentState)
   {
      this.transUnitId = transUnitId;
      //gwt rpc can't handle unmodifiable collection
      this.contents = Lists.newArrayList(contents);
      this.contentState = contentState;
   }

   public List<String> getContents()
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
