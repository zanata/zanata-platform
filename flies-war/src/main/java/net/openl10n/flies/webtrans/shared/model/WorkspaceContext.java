package net.openl10n.flies.webtrans.shared.model;

import java.io.Serializable;

public class WorkspaceContext implements Serializable
{

   private static final long serialVersionUID = 1L;

   private WorkspaceId workspaceId;
   private String workspaceName;
   private String localeName;

   private WorkspaceContext()
   {
   }

   public WorkspaceContext(WorkspaceId workspaceId, String workspaceName, String localeName)
   {
      this.workspaceId = workspaceId;
      this.workspaceName = workspaceName;
      this.localeName = localeName;
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

}