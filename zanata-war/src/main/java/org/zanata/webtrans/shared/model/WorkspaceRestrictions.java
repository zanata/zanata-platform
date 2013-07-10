package org.zanata.webtrans.shared.model;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class WorkspaceRestrictions implements IsSerializable
{
   private boolean isProjectActive;
   private boolean hasWriteAccess;
   private boolean hasGlossaryUpdateAccess;
   private boolean hasReviewAccess;
   private boolean projectRequireReview;

   @SuppressWarnings("unused")
   private WorkspaceRestrictions()
   {
   }

   public WorkspaceRestrictions(boolean projectActive, boolean hasWriteAccess, boolean hasGlossaryUpdateAccess, boolean hasReviewAccess, boolean projectRequireReview)
   {
      isProjectActive = projectActive;
      this.hasWriteAccess = hasWriteAccess;
      this.hasGlossaryUpdateAccess = hasGlossaryUpdateAccess;
      this.hasReviewAccess = hasReviewAccess;
      this.projectRequireReview = projectRequireReview;
   }

   public boolean isProjectActive()
   {
      return isProjectActive;
   }

   public boolean isHasWriteAccess()
   {
      return hasWriteAccess;
   }

   public boolean isHasGlossaryUpdateAccess()
   {
      return hasGlossaryUpdateAccess;
   }

   public boolean isHasReviewAccess()
   {
      return hasReviewAccess;
   }

   public boolean isProjectRequireReview()
   {
      return projectRequireReview;
   }

   public WorkspaceRestrictions changeProjectActivity(boolean projectActive)
   {
      return new WorkspaceRestrictions(projectActive, hasWriteAccess, hasGlossaryUpdateAccess, hasReviewAccess, projectRequireReview);
   }

   public WorkspaceRestrictions changeWriteAccess(boolean hasWriteAccess)
   {
      return new WorkspaceRestrictions(isProjectActive, hasWriteAccess, hasGlossaryUpdateAccess, hasReviewAccess, projectRequireReview);
   }
   
   public WorkspaceRestrictions changeReviewAccess(boolean hasReviewAccess)
   {
      return new WorkspaceRestrictions(isProjectActive, hasWriteAccess, hasGlossaryUpdateAccess, hasReviewAccess, projectRequireReview);
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("isProjectActive", isProjectActive).
            add("hasWriteAccess", hasWriteAccess).
            add("hasGlossaryUpdateAccess", hasGlossaryUpdateAccess).
            add("hasReviewAccess", hasReviewAccess).
            add("projectRequireReview", projectRequireReview).
            toString();
      // @formatter:on
   }
}
