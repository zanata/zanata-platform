package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;



public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult>
{

   private static final long serialVersionUID = 1L;

   private TransUnitId transUnitId;
   private String content;
   private ContentState contentState;

   @SuppressWarnings("unused")
   private UpdateTransUnit()
   {
   }

   public UpdateTransUnit(TransUnitId transUnitId, String content, ContentState contentState)
   {
      this.transUnitId = transUnitId;
      this.content = content;
      this.contentState = contentState;
   }

   public String getContent()
   {
      return content;
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
