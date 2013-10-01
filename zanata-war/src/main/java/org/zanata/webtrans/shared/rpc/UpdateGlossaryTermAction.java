package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.GlossaryDetails;

public class UpdateGlossaryTermAction extends
        AbstractWorkspaceAction<UpdateGlossaryTermResult> {
    private static final long serialVersionUID = 1L;

    private GlossaryDetails selectedDetailEntry;
    private String newTargetTerm;
    private List<String> newTargetComment;

    @SuppressWarnings("unused")
    private UpdateGlossaryTermAction() {
        this(null, null, null);
    }

    public UpdateGlossaryTermAction(GlossaryDetails selectedDetailEntry,
            String newTargetTerm, List<String> newTargetComment) {
        this.selectedDetailEntry = selectedDetailEntry;
        this.newTargetComment = newTargetComment;
        this.newTargetTerm = newTargetTerm;
    }

    public GlossaryDetails getSelectedDetailEntry() {
        return selectedDetailEntry;
    }

    public String getNewTargetTerm() {
        return newTargetTerm;
    }

    public List<String> getNewTargetComment() {
        return newTargetComment;
    }
}
