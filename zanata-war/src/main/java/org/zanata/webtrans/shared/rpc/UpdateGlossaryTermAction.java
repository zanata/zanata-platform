package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.GlossaryDetails;

public class UpdateGlossaryTermAction extends
        AbstractWorkspaceAction<UpdateGlossaryTermResult> {
    private static final long serialVersionUID = 1L;

    private GlossaryDetails selectedDetailEntry;
    private String newTargetTerm;
    private String newTargetComment;
    private String newPos;
    private String newDescription;

    @SuppressWarnings("unused")
    private UpdateGlossaryTermAction() {
        this(null, null, null, null, null);
    }

    public UpdateGlossaryTermAction(GlossaryDetails selectedDetailEntry,
            String newTargetTerm, String newTargetComment, String newPos, String newDesc) {
        this.selectedDetailEntry = selectedDetailEntry;
        this.newTargetComment = newTargetComment;
        this.newTargetTerm = newTargetTerm;
        this.newPos = newPos;
        this.newDescription = newDesc;
    }

    public GlossaryDetails getSelectedDetailEntry() {
        return selectedDetailEntry;
    }

    public String getNewTargetTerm() {
        return newTargetTerm;
    }

    public String getNewTargetComment() {
        return newTargetComment;
    }

    public String getNewPos() {
        return newPos;
    }

    public String getNewDescription() {
        return newDescription;
    }
}
