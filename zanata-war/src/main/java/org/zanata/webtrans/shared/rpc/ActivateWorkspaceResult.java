package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.WorkspaceContext;


public class ActivateWorkspaceResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private WorkspaceContext workspaceContext;
   private Identity identity;

   @SuppressWarnings("unused")
   private ActivateWorkspaceResult()
   {
   }

   public ActivateWorkspaceResult(WorkspaceContext workspaceContext, Identity identity)
   {
      this.workspaceContext = workspaceContext;
      this.identity = identity;
   }

   public WorkspaceContext getWorkspaceContext()
   {
      return workspaceContext;
   }

   public Identity getIdentity()
   {
      return identity;
   }
}
