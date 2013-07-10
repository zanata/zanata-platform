package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserWorkspaceContext implements IsSerializable
{
   private WorkspaceRestrictions workspaceRestrictions;
   private DocumentInfo selectedDoc;

   private WorkspaceContext workspaceContext;

   // for GWT
   @SuppressWarnings("unused")
   private UserWorkspaceContext()
   {
   }

   public UserWorkspaceContext(WorkspaceContext workspaceContext, WorkspaceRestrictions workspaceRestrictions)
   {
      this.workspaceContext = workspaceContext;
      this.workspaceRestrictions = workspaceRestrictions;
   }

   public void setProjectActive(boolean isProjectActive)
   {
      workspaceRestrictions = workspaceRestrictions.changeProjectActivity(isProjectActive);
   }

   public void setHasWriteAccess(boolean hasWriteAccess)
   {
      workspaceRestrictions = workspaceRestrictions.changeWriteAccess(hasWriteAccess);
   }
   
   public void setHasReviewAccess(boolean hasReviewAccess)
   {
      workspaceRestrictions = workspaceRestrictions.changeReviewAccess(hasReviewAccess);
   }

   public WorkspaceContext getWorkspaceContext()
   {
      return workspaceContext;
   }

   public boolean hasReadOnlyAccess()
   {
      return (!getWorkspaceRestrictions().isProjectActive() || (!getWorkspaceRestrictions().isHasWriteAccess() && !getWorkspaceRestrictions().isHasReviewAccess()));
   }
   
   public boolean hasWriteAccess()
   {
      return (getWorkspaceRestrictions().isProjectActive() && getWorkspaceRestrictions().isHasWriteAccess());
   }

   public WorkspaceRestrictions getWorkspaceRestrictions()
   {
      return workspaceRestrictions;
   }

   public void setSelectedDoc(DocumentInfo selectedDoc)
   {
      this.selectedDoc = selectedDoc;
   }

   public DocumentInfo getSelectedDoc()
   {
      return selectedDoc;
   }
}
