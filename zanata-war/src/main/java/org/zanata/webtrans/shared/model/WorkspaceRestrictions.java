package org.zanata.webtrans.shared.model;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class WorkspaceRestrictions implements IsSerializable
{
   private boolean isProjectActive;
   private boolean hasEditTranslationAccess;
   private boolean hasReviewAccess;
   private boolean hasGlossaryUpdateAccess;
   private boolean projectRequireReview;

   @SuppressWarnings("unused")
   private WorkspaceRestrictions()
   {
   }

   public WorkspaceRestrictions(boolean projectActive, boolean hasEditTranslationAccess, boolean hasGlossaryUpdateAccess, boolean hasReviewAccess, boolean projectRequireReview)
   {
      this.isProjectActive = projectActive;
      this.hasEditTranslationAccess = hasEditTranslationAccess;
      this.hasGlossaryUpdateAccess = hasGlossaryUpdateAccess;
      this.hasReviewAccess = hasReviewAccess;
      this.projectRequireReview = projectRequireReview;
   }

   public boolean isProjectActive()
   {
      return isProjectActive;
   }

   public boolean isHasEditTranslationAccess()
   {
      return hasEditTranslationAccess;
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
      return new WorkspaceRestrictions(projectActive, hasEditTranslationAccess, hasGlossaryUpdateAccess, hasReviewAccess, projectRequireReview);
   }

   public WorkspaceRestrictions changeEditTranslationAccess(boolean hasEditTranslationAccess)
   {
      return new WorkspaceRestrictions(isProjectActive, hasEditTranslationAccess, hasGlossaryUpdateAccess, hasReviewAccess, projectRequireReview);
   }

   public WorkspaceRestrictions changeReviewAccess(boolean hasReviewAccess)
   {
      return new WorkspaceRestrictions(isProjectActive, hasEditTranslationAccess, hasGlossaryUpdateAccess, hasReviewAccess, projectRequireReview);
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("isProjectActive", isProjectActive).
            add("hasEditTranslationAccess", hasEditTranslationAccess).
            add("hasGlossaryUpdateAccess", hasGlossaryUpdateAccess).
            add("hasReviewAccess", hasReviewAccess).
            add("projectRequireReview", projectRequireReview).
            toString();
      // @formatter:on
   }
}
