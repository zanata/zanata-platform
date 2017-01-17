package org.zanata.webtrans.shared.model;

import com.google.common.base.MoreObjects;
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

    @SuppressWarnings("unused")
    private WorkspaceRestrictions() {
    }

    public WorkspaceRestrictions(boolean projectActive, boolean projectObsolete,
            boolean hasEditTranslationAccess, boolean hasGlossaryUpdateAccess,
            boolean hasReviewAccess) {
        this.isProjectActive = projectActive;
        this.isProjectObsolete = projectObsolete;
        this.hasEditTranslationAccess = hasEditTranslationAccess;
        this.hasGlossaryUpdateAccess = hasGlossaryUpdateAccess;
        this.hasReviewAccess = hasReviewAccess;
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

    public WorkspaceRestrictions changeProjectActivity(boolean projectActive) {
        return new WorkspaceRestrictions(projectActive, isProjectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess);
    }

    public WorkspaceRestrictions changeProjectObsolescence(boolean projectObsolete) {
        return new WorkspaceRestrictions(isProjectActive, projectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess);
    }

    public WorkspaceRestrictions changeEditTranslationAccess(
            boolean hasEditTranslationAccess) {
        return new WorkspaceRestrictions(isProjectActive, isProjectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess);
    }

    public WorkspaceRestrictions changeReviewAccess(boolean hasReviewAccess) {
        return new WorkspaceRestrictions(isProjectActive, isProjectObsolete,
                hasEditTranslationAccess, hasGlossaryUpdateAccess,
                hasReviewAccess);
    }

    @Override
    public String toString() {
        // @formatter:off
      return MoreObjects.toStringHelper(this).
            add("isProjectActive", isProjectActive).
            add("hasEditTranslationAccess", hasEditTranslationAccess).
            add("hasGlossaryUpdateAccess", hasGlossaryUpdateAccess).
            add("hasReviewAccess", hasReviewAccess).
            toString();
      // @formatter:on
    }
}
