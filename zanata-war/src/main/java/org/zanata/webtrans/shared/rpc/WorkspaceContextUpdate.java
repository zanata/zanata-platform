package org.zanata.webtrans.shared.rpc;



public class WorkspaceContextUpdate implements SessionEventData, HasWorkspaceContextUpdateData
{

   private static final long serialVersionUID = 1L;

   private boolean isProjectActive;

   @SuppressWarnings("unused")
   private WorkspaceContextUpdate()
   {
   }

   public WorkspaceContextUpdate(boolean isProjectActive)
   {
      this.isProjectActive = isProjectActive;
   }

   @Override
   public boolean isProjectActive()
   {
      return isProjectActive;
   }
}
