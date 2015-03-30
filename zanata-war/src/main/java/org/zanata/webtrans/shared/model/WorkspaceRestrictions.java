package org.zanata.webtrans.shared.model;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class WorkspaceRestrictions implements IsSerializable {
    private boolean isProjectActive;
    private boolean isProjectObsolete;
    private boolean hasEditTranslationAccess;
    private boolean hasReviewAccess;
    private boolean hasGlossaryUpdateAccess;
    private boolean projectRequireReview;

    @SuppressWarnings("unused")
    private WorkspaceRestrictions() {
    }

    public WorkspaceRestrictions(boolean projectActive, boolean projectObsolete,
            boolean hasEditTranslationAccess, boolean hasGlossaryUpdateAccess,
            boolean hasReviewAccess, boolean projectRequireReview) {
        this.isProjectActive = projectActive;
        this.isProjectObsolete = projectObsolete;
        this.hasEditTranslationAccess = hasEditTranslationAccess;
        this.hasGlossaryUpdateAccess = hasGlossaryUpdateAccess;
        this.hasReviewAccess = hasReviewAccess;
        this.projectRequireReview = projectRequireReview;
    }

    public boolean isProjectActive() {
        return isProjectActive;
    }

    public boolean isProjectObsolete() {
        return isProjectObsolete;
    }

    public boolean isHasEditTranslationAccess() {
        return hasEditTranslationAccess;
    }

    public boolean isHasGlossaryUpdateAccess() {
        return hasGlossaryUpdateAccess;
    }

    public boolean isHasReviewAccess() {
        return hasReviewAccess;
    }

    public boolean isProjectRequireReview() {
        return projectRequireReview;
    }

    public WorkspaceRestrictions changeProjectActivity(boolean projectActive) {
        return new WorkspaceRestrictions(projectActive, isProjectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess, projectRequireReview);
    }

    public WorkspaceRestrictions changeProjectObsolescence(boolean projectObsolete) {
        return new WorkspaceRestrictions(isProjectActive, projectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess, projectRequireReview);
    }

    public WorkspaceRestrictions changeEditTranslationAccess(
            boolean hasEditTranslationAccess) {
        return new WorkspaceRestrictions(isProjectActive, isProjectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess, projectRequireReview);
    }

    public WorkspaceRestrictions changeReviewAccess(boolean hasReviewAccess) {
        return new WorkspaceRestrictions(isProjectActive, isProjectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess, projectRequireReview);
    }

    @Override
    public String toString() {
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
