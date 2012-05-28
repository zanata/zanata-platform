package org.zanata.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.WorkspaceId;


public abstract class AbstractWorkspaceAction<R extends Result> implements Action<R>
{

   private static final long serialVersionUID = 1L;

   private WorkspaceId workspaceId;
   private EditorClientId editorClientId;


   // this value is set by SeamDispatchAsync
   public final void setWorkspaceId(WorkspaceId workspaceId)
   {
      this.workspaceId = workspaceId;
   }

   // this value is set by SeamDispatchAsync
   public final void setEditorClientId(EditorClientId editorClientId)
   {
      this.editorClientId = editorClientId;
   }

   public final WorkspaceId getWorkspaceId()
   {
      return workspaceId;
   }

   public final EditorClientId getEditorClientId()
   {
      return editorClientId;
   }

}
