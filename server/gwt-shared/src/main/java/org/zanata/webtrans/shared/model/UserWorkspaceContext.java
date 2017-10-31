package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.List;

import org.zanata.webtrans.shared.rest.dto.TransReviewCriteria;

public class UserWorkspaceContext implements IsSerializable, Serializable {
    private static final long serialVersionUID = 3954106559378324802L;
    private WorkspaceRestrictions workspaceRestrictions;
    private DocumentInfo selectedDoc;
    private List<TransReviewCriteria> reviewCriteria;

    private WorkspaceContext workspaceContext;

    // for GWT
    @SuppressWarnings("unused")
    private UserWorkspaceContext() {
    }

    public UserWorkspaceContext(WorkspaceContext workspaceContext,
            WorkspaceRestrictions workspaceRestrictions,
            List<TransReviewCriteria> reviewCriteria) {
        this.workspaceContext = workspaceContext;
        this.workspaceRestrictions = workspaceRestrictions;
        this.reviewCriteria = reviewCriteria;
    }

    public void setProjectActive(boolean isProjectActive) {
        workspaceRestrictions =
                workspaceRestrictions.changeProjectActivity(isProjectActive);
    }

    public void setHasEditTranslationAccess(boolean hasEditTranslationAccess) {
        workspaceRestrictions =
                workspaceRestrictions
                        .changeEditTranslationAccess(hasEditTranslationAccess);
    }

    public void setHasReviewAccess(boolean hasReviewAccess) {
        workspaceRestrictions =
                workspaceRestrictions.changeReviewAccess(hasReviewAccess);
    }

    public WorkspaceContext getWorkspaceContext() {
        return workspaceContext;
    }

    public boolean hasReadOnlyAccess() {
        return (!getWorkspaceRestrictions().isProjectActive() || (!getWorkspaceRestrictions()
                .isHasEditTranslationAccess() && !getWorkspaceRestrictions()
                .isHasReviewAccess()));
    }

    public boolean hasEditTranslationAccess() {
        return (getWorkspaceRestrictions().isProjectActive() && getWorkspaceRestrictions()
                .isHasEditTranslationAccess());
    }

    public WorkspaceRestrictions getWorkspaceRestrictions() {
        return workspaceRestrictions;
    }

    public void setSelectedDoc(DocumentInfo selectedDoc) {
        this.selectedDoc = selectedDoc;
    }

    public DocumentInfo getSelectedDoc() {
        return selectedDoc;
    }

    public List<TransReviewCriteria> getReviewCriteria() {
        return reviewCriteria;
    }
}
