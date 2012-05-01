package org.zanata.webtrans.shared.rpc;

import org.zanata.common.EditState;
import org.zanata.webtrans.shared.model.TransUnitId;



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