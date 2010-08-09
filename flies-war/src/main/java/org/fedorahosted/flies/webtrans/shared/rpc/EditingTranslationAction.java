package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;

public class EditingTranslationAction extends AbstractWorkspaceAction<EditingTranslationResult>
{

   private static final long serialVersionUID = 1L;

   private TransUnitId transUnitId;
   private EditState editState;

   @SuppressWarnings("unused")
   private EditingTranslationAction()
   {
   }

   public EditingTranslationAction(TransUnitId transUnitId, EditState editState)
   {
      this.transUnitId = transUnitId;
      this.editState = editState;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public EditState getEditState()
   {
      return editState;
   }

}