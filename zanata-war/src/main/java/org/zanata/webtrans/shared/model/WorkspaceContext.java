package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WorkspaceContext implements IsSerializable
{
   private WorkspaceId workspaceId;
   private String workspaceName;
   private String localeName;
   private boolean readOnly;

   // for GWT
   @SuppressWarnings("unused")
   private WorkspaceContext()
   {
   }

   public WorkspaceContext(WorkspaceId workspaceId, String workspaceName, String localeName, boolean readOnly)
   {
      this.workspaceId = workspaceId;
      this.workspaceName = workspaceName;
      this.localeName = localeName;
      this.readOnly = readOnly;
   }

   @Override
   public String toString()
   {
      return workspaceId.toString();
   }

   public WorkspaceId getWorkspaceId()
   {
      return workspaceId;
   }

   public String getWorkspaceName()
   {
      return workspaceName;
   }

   public String getLocaleName()
   {
      return localeName;
   }

   public boolean isReadOnly()
   {
      return readOnly;
   }

   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }

}