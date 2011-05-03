package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.WorkspaceId;

import net.customware.gwt.dispatch.shared.Action;


public class ActivateWorkspaceAction implements Action<ActivateWorkspaceResult>
{

   private static final long serialVersionUID = 1L;

   private WorkspaceId workspaceId;

   @SuppressWarnings("unused")
   private ActivateWorkspaceAction()
   {
   }

   public ActivateWorkspaceAction(WorkspaceId workspaceId)
   {
      this.workspaceId = workspaceId;
   }

   public WorkspaceId getWorkspaceId()
   {
      return workspaceId;
   }
}
