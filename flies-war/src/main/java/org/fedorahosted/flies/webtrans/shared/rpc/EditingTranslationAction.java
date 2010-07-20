package org.fedorahosted.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.webtrans.shared.auth.SessionId;
import org.fedorahosted.flies.webtrans.shared.model.ProjectIterationId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;
import org.fedorahosted.flies.webtrans.shared.model.WorkspaceId;

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