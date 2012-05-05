package org.zanata.webtrans.shared.rpc;



public class WorkspaceContextUpdate implements SessionEventData, HasWorkspaceContextUpdateData
{

   private static final long serialVersionUID = 1L;

   private boolean readOnly;

   @SuppressWarnings("unused")
   private WorkspaceContextUpdate()
   {
   }

   public WorkspaceContextUpdate(boolean readOnly)
   {
      this.readOnly = readOnly;
   }

   @Override
   public boolean isReadOnly()
   {
      return readOnly;
   }
}
